package com.example.v02.screens

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.v02.ReelsBlockingService.MainViewModel
import com.example.v02.timelimit.WebsiteBlockAccessibilityService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebsiteBlockScreen(
    navController: NavController,
    mainViewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var websiteUrl by remember { mutableStateOf("") }
    var isServiceEnabled by remember { mutableStateOf(false) }

    val appSettings by mainViewModel.appSettings.collectAsState()

    val blockedWebsites = remember(appSettings) {
        if (appSettings.accountMode == "Parent") {
            appSettings.blockedWebsites
        } else {
            appSettings.childProfiles.find { it.id == appSettings.activeChildId }?.blockedWebsites ?: emptyList()
        }
    }

    LaunchedEffect(Unit) {
        isServiceEnabled = isAccessibilityServiceEnabled(context)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Website Blocking", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ✅ Service Status Card
            item {
                ServiceStatusCard(context, isServiceEnabled)
            }

            // ✅ Add Website Section
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Add Website to Block", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = websiteUrl,
                            onValueChange = { websiteUrl = it },
                            label = { Text("Website URL") },
                            placeholder = { Text("e.g., facebook.com") },
                            leadingIcon = { Icon(Icons.Default.Web, contentDescription = null) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardController?.hide()
                                val cleaned = cleanWebsiteUrl(websiteUrl)
                                if (cleaned.isNotBlank() && !blockedWebsites.contains(cleaned)) {
                                    mainViewModel.addBlockedWebsite(cleaned)
                                    WebsiteBlockAccessibilityService.updateBlockedWebsitesStatic(context, setOf(cleaned))
                                    websiteUrl = ""
                                }
                            }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                val cleaned = cleanWebsiteUrl(websiteUrl)
                                if (cleaned.isNotBlank() && !blockedWebsites.contains(cleaned)) {
                                    mainViewModel.addBlockedWebsite(cleaned)
                                    WebsiteBlockAccessibilityService.updateBlockedWebsitesStatic(context, setOf(cleaned))
                                    websiteUrl = ""
                                }
                            },
                            enabled = websiteUrl.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Website")
                        }
                    }
                }
            }

            // ✅ Blocked Websites List
            item {
                Text(
                    "Blocked Websites (${blockedWebsites.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (blockedWebsites.isEmpty()) {
                item {
                    Text("No websites blocked yet. Add some above.")
                }
            } else {
                items(blockedWebsites) { website ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(website)
                            IconButton(onClick = {
                                mainViewModel.removeBlockedWebsite(website)
                                WebsiteBlockAccessibilityService.updateBlockedWebsitesStatic(context, blockedWebsites.toSet() - website)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ✅ Service Status Card
@Composable
fun ServiceStatusCard(context: Context, isServiceEnabled: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isServiceEnabled) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFF44336).copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isServiceEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isServiceEnabled) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isServiceEnabled) "Service Active" else "Enable Accessibility Service",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isServiceEnabled) MaterialTheme.colorScheme.primary else Color(0xFFF44336)
                )
            ) {
                Text(if (isServiceEnabled) "Manage Settings" else "Enable Service")
            }
        }
    }
}

// ✅ Utility
private fun cleanWebsiteUrl(url: String): String {
    return url.trim().lowercase()
        .removePrefix("http://")
        .removePrefix("https://")
        .removePrefix("www.")
}

private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
    return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        .any { it.resolveInfo.serviceInfo.name.contains("InstagramBlockAccessibilityService") }
}
