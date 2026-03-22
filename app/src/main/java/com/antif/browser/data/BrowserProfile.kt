package com.antif.browser.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class BrowserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String = "Default Profile",

    // OS Type for consistent profile (windows, mac, linux)
    val osType: String = "windows",

    // Navigator overrides
    val userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
    val platform: String = "Win32",
    val language: String = "en-US",
    val languages: String = "[\"en-US\",\"en\"]",
    val doNotTrack: String = "1",

    // Screen
    val screenWidth: Int = 1920,
    val screenHeight: Int = 1080,
    val colorDepth: Int = 24,
    val devicePixelRatio: Double = 1.0,

    // Hardware
    val hardwareConcurrency: Int = 8,
    val deviceMemory: Int = 8,
    val maxTouchPoints: Int = 0,

    // Canvas
    val canvasNoiseSeed: Int = 42,

    // WebGL
    val webglVendor: String = "Google Inc. (NVIDIA)",
    val webglRenderer: String = "ANGLE (NVIDIA, NVIDIA GeForce GTX 1080 Direct3D11 vs_5_0 ps_5_0, D3D11)",

    // Audio
    val audioNoiseSeed: Int = 127,

    // Timezone
    val timezone: String = "America/New_York",
    val timezoneOffset: Int = 300,

    // Timing protection seed
    val timingSeed: Int = 12345,
    
    // ClientRects noise seed
    val rectNoiseSeed: Int = 54321,
    
    // Prefers dark mode
    val prefersDark: Boolean = false,

    // Privacy
    val blockWebRTC: Boolean = true,
    val blockCanvas: Boolean = true,
    val blockWebGL: Boolean = false,
    val blockAudioContext: Boolean = true,

    // Plugins (JSON array)
    val pluginsJson: String = "[]",

    // Proxy
    val proxyType: String = "none", // "none", "http", "socks5"
    val proxyHost: String = "",
    val proxyPort: Int = 0,
    val proxyUsername: String = "",
    val proxyPassword: String = "",

    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = 0,

    // AdBlock
    val adblockEnabled: Boolean = true,
    val cosmeticFilterEnabled: Boolean = true,

    // Homepage
    val homepage: String = "https://www.google.com"
)
