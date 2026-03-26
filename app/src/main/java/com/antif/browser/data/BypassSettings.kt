package com.antif.browser.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Global Bypass Settings - stored in SharedPreferences
 * Accessible from MainActivity and BrowserActivity
 */
object BypassSettings {
    private const val PREFS_NAME = "antif_bypass_settings"
    
    // Keys
    private const val KEY_FULL_BYPASS = "full_bypass"
    private const val KEY_BLOCK_TRACKING = "block_tracking"
    private const val KEY_SPOOF_FEATURE_FLAGS = "spoof_feature_flags"
    private const val KEY_NEW_DEVICE_ID = "new_device_id"
    private const val KEY_KILL_POSTHOG = "kill_posthog"
    private const val KEY_TURKEY_REGION = "turkey_region"
    private const val KEY_CANADA_REGION = "canada_region"
    private const val KEY_APPLY_ALL_SITES = "apply_all_sites"
    private const val KEY_FULL_LOGGING = "full_logging"
    
    private lateinit var prefs: SharedPreferences
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    var fullBypassEnabled: Boolean
        get() = prefs.getBoolean(KEY_FULL_BYPASS, true)
        set(value) = prefs.edit().putBoolean(KEY_FULL_BYPASS, value).apply()
    
    var blockTrackingEnabled: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_TRACKING, true)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_TRACKING, value).apply()
    
    var spoofFeatureFlagsEnabled: Boolean
        get() = prefs.getBoolean(KEY_SPOOF_FEATURE_FLAGS, true)
        set(value) = prefs.edit().putBoolean(KEY_SPOOF_FEATURE_FLAGS, value).apply()
    
    var newDeviceIdEnabled: Boolean
        get() = prefs.getBoolean(KEY_NEW_DEVICE_ID, true)
        set(value) = prefs.edit().putBoolean(KEY_NEW_DEVICE_ID, value).apply()
    
    var killPostHogEnabled: Boolean
        get() = prefs.getBoolean(KEY_KILL_POSTHOG, true)
        set(value) = prefs.edit().putBoolean(KEY_KILL_POSTHOG, value).apply()
    
    var turkeyRegionEnabled: Boolean
        get() = prefs.getBoolean(KEY_TURKEY_REGION, true)
        set(value) = prefs.edit().putBoolean(KEY_TURKEY_REGION, value).apply()
    
    var canadaRegionEnabled: Boolean
        get() = prefs.getBoolean(KEY_CANADA_REGION, true)
        set(value) = prefs.edit().putBoolean(KEY_CANADA_REGION, value).apply()
    
    var applyToAllSitesEnabled: Boolean
        get() = prefs.getBoolean(KEY_APPLY_ALL_SITES, false)
        set(value) = prefs.edit().putBoolean(KEY_APPLY_ALL_SITES, value).apply()
    
    var fullLoggingEnabled: Boolean
        get() = prefs.getBoolean(KEY_FULL_LOGGING, false)
        set(value) = prefs.edit().putBoolean(KEY_FULL_LOGGING, value).apply()
    
    fun getStatusSummary(): String {
        val enabled = mutableListOf<String>()
        if (fullBypassEnabled) enabled.add("Full Bypass")
        if (blockTrackingEnabled) enabled.add("Block Tracking")
        if (spoofFeatureFlagsEnabled) enabled.add("Spoof Flags")
        if (newDeviceIdEnabled) enabled.add("New Device-Id")
        if (killPostHogEnabled) enabled.add("Kill PostHog")
        if (turkeyRegionEnabled) enabled.add("Turkey")
        if (canadaRegionEnabled) enabled.add("Canada")
        if (applyToAllSitesEnabled) enabled.add("ALL SITES")
        if (fullLoggingEnabled) enabled.add("Logging")
        
        return if (enabled.isEmpty()) "All bypass features disabled" 
               else "Active: ${enabled.joinToString(", ")}"
    }
}
