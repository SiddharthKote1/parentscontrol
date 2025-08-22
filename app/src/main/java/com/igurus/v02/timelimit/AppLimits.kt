package com.igurus.v02.timelimit

import android.app.usage.UsageStatsManager
import android.content.Context
import com.igurus.v02.ReelsBlockingService.AppSettings
import com.igurus.v02.ReelsBlockingService.appSettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.*

object AppLimits {

    private var settings: AppSettings? = null

    // Load settings once (not reactive)
    fun initialize(context: Context) {
        runBlocking {
            settings = context.appSettingsDataStore.data.first()
        }
    }

    // Get current app time limit for parent or active child
    fun getLimit(packageName: String): Int {
        val config = settings ?: return 0
        return if (config.accountMode == "Parent") {
            config.parentAppTimeLimits[packageName] ?: 0
        } else {
            config.childProfiles.find { it.id == config.activeChildId }
                ?.appTimeLimits?.get(packageName) ?: 0
        }
    }

    // Save limits by writing current settings back to DataStore
    fun saveLimits(context: Context) {
        runBlocking {
            settings?.let { current ->
                context.appSettingsDataStore.updateData {
                    current
                }
            }
        }
    }

    // Remove limit for an app (set to 0)


    // Get all app time limits (Map) depending on mode
    fun getAllLimits(): Map<String, Int> {
        val config = settings ?: return emptyMap()
        return if (config.accountMode == "Parent") {
            config.parentAppTimeLimits
        } else {
            config.childProfiles.find { it.id == config.activeChildId }
                ?.appTimeLimits ?: emptyMap()
        }
    }

    // Check if usage exceeded for given package
    fun isAppLimitExceeded(packageName: String, usageStatsManager: UsageStatsManager): Boolean {
        val limit = getLimit(packageName)
        if (limit <= 0) return false

        val usageMinutes = getTodayUsageMinutes(packageName, usageStatsManager)
        return usageMinutes >= limit
    }

    // Get usage for a single package today
    fun getTodayUsageMinutes(packageName: String, usageStatsManager: UsageStatsManager): Long {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        val appUsage = usageStats.find { it.packageName == packageName }
        return (appUsage?.totalTimeInForeground ?: 0) / (1000 * 60)
    }

    // ✅ NEW: Used in AppUsageScreen and AppLimitsScreen to get all stats
    fun getUsageStats(usageStatsManager: UsageStatsManager): List<AppUsageStats> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        return usageStats.map {
            AppUsageStats(
                packageName = it.packageName,
                totalTimeInForeground = it.totalTimeInForeground
            )
        }
    }

    data class AppUsageStats(
        val packageName: String,
        val totalTimeInForeground: Long
    )
}
