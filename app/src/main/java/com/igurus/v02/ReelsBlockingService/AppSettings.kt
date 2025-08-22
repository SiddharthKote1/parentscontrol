package com.igurus.v02.ReelsBlockingService

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class AppSettings(
    val isParentMode: Boolean = true,
    val pinCode: String = "",
    val secretQuestion: String = "",
    val secretAnswer: String = "",
    val accountMode: String = "Parent", // "Parent" or "Child"
    val activeChildId: String = "",
    val childProfiles: List<ChildProfile> = emptyList(),

    val instagram: App = App(),
    val facebook: App = App(),
    val youtube: YouTubeApp = YouTubeApp(),
    val twitter: TwitterApp = TwitterApp(),
    val whatsapp: WhatsAppApp = WhatsAppApp(),
    val snapchat: SnapchatApp = SnapchatApp(),

    val parentAppTimeLimits: Map<String, Int> = emptyMap(),

    val blockedKeywordLists: BlockedKeywordLists = BlockedKeywordLists(),
    val customBlockedKeywords: List<String> = emptyList(),

    // ✅ Parent permanent blocked apps
    val blockedApps: Map<String, App> = emptyMap(),

    // ✅ Parent Category Blocking (e.g., block all Games or Social apps)
    val blockedCategories: Set<String> = emptySet(),
    val blockedWebsites: List<String> = emptyList(),

    // ✅ OLD Parent Bedtime (single range)
    val parentBedtimeStart: String = "",
    val parentBedtimeEnd: String = "",

    // ✅ NEW Parent multiple bedtime blocks
    val bedtimeBlocks: List<BedtimeBlock> = emptyList()
)

@Serializable
data class ChildProfile(
    val name: String,
    val id: String = UUID.randomUUID().toString(),
    val instagram: App = App(),
    val facebook: App = App(),
    val youtube: YouTubeApp = YouTubeApp(),
    val twitter: TwitterApp = TwitterApp(),
    val whatsapp: WhatsAppApp = WhatsAppApp(),
    val snapchat: SnapchatApp = SnapchatApp(),
    val appTimeLimits: Map<String, Int> = emptyMap(),

    // ✅ OLD Per-child Bedtime (single range)
    val bedtimeStart: String = "",
    val bedtimeEnd: String = "",

    // ✅ NEW Per-child multiple bedtime blocks
    val bedtimeBlocks: List<BedtimeBlock> = emptyList(),

    // ✅ Per-child permanent blocked apps
    val blockedApps: Map<String, App> = emptyMap(),

    // ✅ Per-child keyword blocking
    val blockedKeywordLists: BlockedKeywordLists = BlockedKeywordLists(),
    val customBlockedKeywords: List<String> = emptyList(),

    // ✅ Per-child Category Blocking
    val blockedCategories: Set<String> = emptySet(),
    val blockedWebsites: List<String> = emptyList()
)

@Serializable
data class BedtimeBlock(
    val start: String = "", // e.g., "22:00"
    val end: String = ""    // e.g., "07:00"
)

@Serializable
data class App(
    val reelsBlocked: Boolean = false,
    val storiesBlocked: Boolean = false,
    val exploreBlocked: Boolean = false,
    val marketplaceBlocked: Boolean = false,
    val blockedStart: Int = 0,
    val blockedEnd: Int = 1439
)

@Serializable
data class YouTubeApp(
    val shortsBlocked: Boolean = false,
    val commentsBlocked: Boolean = false,
    val searchBlocked: Boolean = false,
    val blockedStart: Int = 0,
    val blockedEnd: Int = 1439
)

@Serializable
data class TwitterApp(
    val exploreBlocked: Boolean = false,
    val blockedStart: Int = 0,
    val blockedEnd: Int = 1439
)

@Serializable
data class WhatsAppApp(
    val statusBlocked: Boolean = false,
    val blockedStart: Int = 0,
    val blockedEnd: Int = 1439
)

@Serializable
data class SnapchatApp(
    val spotlightBlocked: Boolean = false,
    val storiesBlocked: Boolean = false,
    val blockedStart: Int = 0,
    val blockedEnd: Int = 1439
)

@Serializable
data class BlockedKeywordLists(
    val adult: Boolean = false,
    val gambling: Boolean = false,
    val violent: Boolean = false,
    val hate: Boolean = false,
    val drug: Boolean = false,
    val scam: Boolean = false
)