package com.igurus.v02.timelimit

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.igurus.v02.ReelsBlockingService.DataStoreManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class WebsiteBlockAccessibilityService : AccessibilityService() {

    private val blockedWebsites = mutableSetOf<String>()
    private lateinit var dataStoreManager: DataStoreManager
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val browserPackages = setOf(
        "com.android.chrome",
        "com.mozilla.firefox",
        "com.opera.browser",
        "com.microsoft.emmx",
        "org.mozilla.focus",
        "com.brave.browser",
        "com.sec.android.app.sbrowser",
        "com.UCMobile.intl",
        "com.opera.mini.native",
        "com.duckduckgo.mobile.android"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()

        instance = this // <--- Set static instance

        dataStoreManager = DataStoreManager(applicationContext)

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            packageNames = browserPackages.toTypedArray()
        }

        serviceInfo = info

        observeBlockedWebsites()
        Log.d("AccessibilityService", "Website blocking service connected")
    }

    private fun observeBlockedWebsites() {
        serviceScope.launch {
            dataStoreManager.appSettings.collectLatest { settings ->
                val websites = if (settings.accountMode == "Parent") {
                    settings.blockedWebsites
                } else {
                    settings.childProfiles.find { it.id == settings.activeChildId }?.blockedWebsites ?: emptyList()
                }
                blockedWebsites.clear()
                blockedWebsites.addAll(websites)
                Log.d("AccessibilityService", "Live updated blocked websites: $blockedWebsites")
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            val packageName = it.packageName?.toString()
            if (browserPackages.contains(packageName)) {
                handleWebsiteBlocking(it)
            }
        }
    }

    private fun handleWebsiteBlocking(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow ?: return

        try {
            val urlNodes = findUrlNodes(rootNode)

            for (urlNode in urlNodes) {
                val url = urlNode.text?.toString()?.lowercase() ?: continue

                for (blockedSite in blockedWebsites) {
                    if (url.contains(blockedSite)) {
                        Log.d("AccessibilityService", "Blocked website detected: $url")
                        blockWebsite()
                        return
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AccessibilityService", "Error checking website: ${e.message}")
        } finally {
            rootNode.recycle()
        }
    }

    private fun blockWebsite() {
        performGlobalAction(GLOBAL_ACTION_BACK)
        Log.d("AccessibilityService", "Website blocked - back pressed")
    }

    private fun findUrlNodes(rootNode: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val urlNodes = mutableListOf<AccessibilityNodeInfo>()

        val urlBarIds = listOf(
            "com.android.chrome:id/url_bar",
            "com.android.chrome:id/location_bar_status_icon_view",
            "org.mozilla.firefox:id/url_bar_title",
            "com.opera.browser:id/url_field",
            "com.microsoft.emmx:id/url_bar",
            "com.sec.android.app.sbrowser:id/location_bar",
            "com.UCMobile.intl:id/address_bar",
            "com.duckduckgo.mobile.android:id/omnibarTextInput"
        )

        for (urlBarId in urlBarIds) {
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(urlBarId)
            urlNodes.addAll(nodes)
        }

        searchNodesByText(rootNode, urlNodes)

        return urlNodes
    }

    private fun searchNodesByText(node: AccessibilityNodeInfo, urlNodes: MutableList<AccessibilityNodeInfo>) {
        val text = node.text?.toString()?.lowercase()
        val contentDesc = node.contentDescription?.toString()?.lowercase()

        if ((text != null && (text.contains("http") || text.contains("www") || text.contains(".com"))) ||
            (contentDesc != null && (contentDesc.contains("address") || contentDesc.contains("url")))) {
            urlNodes.add(node)
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                searchNodesByText(child, urlNodes)
                child.recycle()
            }
        }
    }

    override fun onInterrupt() {
        Log.d("AccessibilityService", "Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    fun updateBlockedWebsites(newList: Set<String>) {
        blockedWebsites.clear()
        blockedWebsites.addAll(newList)
        Log.d("AccessibilityService", "Blocked websites manually updated: $blockedWebsites")
    }

    companion object {
        private var instance: WebsiteBlockAccessibilityService? = null

        fun updateBlockedWebsitesStatic(context: android.content.Context, websites: Set<String>) {
            instance?.updateBlockedWebsites(websites)
        }
    }
}
