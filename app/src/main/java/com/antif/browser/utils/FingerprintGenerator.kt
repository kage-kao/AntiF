package com.antif.browser.utils

import com.antif.browser.data.BrowserProfile
import kotlin.random.Random

object FingerprintGenerator {

    // OS-consistent profile configurations
    enum class OSType(val platform: String, val osName: String) {
        WINDOWS("Win32", "windows"),
        MAC("MacIntel", "mac"),
        LINUX("Linux x86_64", "linux")
    }

    // Consistent OS + Browser + Hardware combinations
    data class ConsistentConfig(
        val osType: OSType,
        val userAgentTemplate: String,
        val webglVendor: String,
        val webglRenderer: String,
        val plugins: List<String>
    )

    private val consistentConfigs = listOf(
        // Windows + Chrome + NVIDIA
        ConsistentConfig(
            OSType.WINDOWS,
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/{version}.0.0.0 Safari/537.36",
            "Google Inc. (NVIDIA)",
            "ANGLE (NVIDIA, NVIDIA GeForce GTX 1080 Direct3D11 vs_5_0 ps_5_0, D3D11)",
            listOf("Chrome PDF Plugin", "Chrome PDF Viewer", "Native Client")
        ),
        ConsistentConfig(
            OSType.WINDOWS,
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/{version}.0.0.0 Safari/537.36",
            "Google Inc. (NVIDIA)",
            "ANGLE (NVIDIA, NVIDIA GeForce RTX 3060 Direct3D11 vs_5_0 ps_5_0, D3D11)",
            listOf("Chrome PDF Plugin", "Chrome PDF Viewer", "Native Client")
        ),
        ConsistentConfig(
            OSType.WINDOWS,
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/{version}.0.0.0 Safari/537.36",
            "Google Inc. (NVIDIA)",
            "ANGLE (NVIDIA, NVIDIA GeForce RTX 4070 Direct3D11 vs_5_0 ps_5_0, D3D11)",
            listOf("Chrome PDF Plugin", "Chrome PDF Viewer", "Native Client")
        ),
        // Windows + Chrome + AMD
        ConsistentConfig(
            OSType.WINDOWS,
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/{version}.0.0.0 Safari/537.36",
            "Google Inc. (AMD)",
            "ANGLE (AMD, AMD Radeon RX 580 Direct3D11 vs_5_0 ps_5_0, D3D11)",
            listOf("Chrome PDF Plugin", "Chrome PDF Viewer", "Native Client")
        ),
        ConsistentConfig(
            OSType.WINDOWS,
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/{version}.0.0.0 Safari/537.36",
            "Google Inc. (AMD)",
            "ANGLE (AMD, AMD Radeon RX 6700 XT Direct3D11 vs_5_0 ps_5_0, D3D11)",
            listOf("Chrome PDF Plugin", "Chrome PDF Viewer", "Native Client")
        ),
        // Windows + Chrome + Intel
        ConsistentConfig(
            OSType.WINDOWS,
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/{version}.0.0.0 Safari/537.36",
            "Google Inc. (Intel)",
            "ANGLE (Intel, Intel(R) UHD Graphics 630 Direct3D11 vs_5_0 ps_5_0, D3D11)",
            listOf("Chrome PDF Plugin", "Chrome PDF Viewer", "Native Client")
        ),
        ConsistentConfig(
            OSType.WINDOWS,
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/{version}.0.0.0 Safari/537.36",
            "Google Inc. (Intel)",
            "ANGLE (Intel, Intel(R) Iris(R) Xe Graphics Direct3D11 vs_5_0 ps_5_0, D3D11)",
            listOf("Chrome PDF Plugin", "Chrome PDF Viewer", "Native Client")
        ),
        // Mac + Chrome + Apple Silicon
        ConsistentConfig(
            OSType.MAC,
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/{version}.0.0.0 Safari/537.36",
            "Google Inc. (Apple)",
            "ANGLE (Apple, Apple M1, OpenGL 4.1)",
            listOf("Chrome PDF Plugin", "Chrome PDF Viewer", "Native Client")
        ),
        ConsistentConfig(
            OSType.MAC,
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/{version}.0.0.0 Safari/537.36",
            "Google Inc. (Apple)",
            "ANGLE (Apple, Apple M2, OpenGL 4.1)",
            listOf("Chrome PDF Plugin", "Chrome PDF Viewer", "Native Client")
        ),
        // Linux + Chrome
        ConsistentConfig(
            OSType.LINUX,
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/{version}.0.0.0 Safari/537.36",
            "Google Inc. (NVIDIA)",
            "ANGLE (NVIDIA, NVIDIA GeForce GTX 1080, OpenGL 4.5)",
            emptyList()  // Linux Chrome usually shows empty plugins
        ),
        ConsistentConfig(
            OSType.LINUX,
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/{version}.0.0.0 Safari/537.36",
            "Google Inc. (AMD)",
            "ANGLE (AMD, AMD Radeon RX 580, OpenGL 4.5)",
            emptyList()
        )
    )

    private val chromeVersions = listOf(120, 121, 122, 123, 124, 125)

    private val languages = listOf(
        "en-US", "en-GB", "de-DE", "fr-FR", "es-ES", "it-IT",
        "pt-BR", "ja-JP", "ko-KR", "zh-CN", "ru-RU", "nl-NL",
        "pl-PL", "tr-TR", "sv-SE", "da-DK", "nb-NO", "fi-FI"
    )

    private val timezones = listOf(
        "America/New_York" to 300,
        "America/Chicago" to 360,
        "America/Denver" to 420,
        "America/Los_Angeles" to 480,
        "Europe/London" to 0,
        "Europe/Berlin" to -60,
        "Europe/Paris" to -60,
        "Europe/Moscow" to -180,
        "Asia/Tokyo" to -540,
        "Asia/Shanghai" to -480,
        "Asia/Kolkata" to -330,
        "Australia/Sydney" to -600,
        "Pacific/Auckland" to -720
    )

    private val screenResolutions = listOf(
        Triple(1920, 1080, 1.0),
        Triple(1366, 768, 1.0),
        Triple(1536, 864, 1.25),
        Triple(1440, 900, 1.0),
        Triple(1280, 720, 1.0),
        Triple(2560, 1440, 1.0),
        Triple(1600, 900, 1.0),
        Triple(1280, 1024, 1.0),
        Triple(1680, 1050, 1.0),
        Triple(3840, 2160, 2.0)  // 4K with 2x DPR
    )

    // CPU cores based on OS type for consistency
    private val cpuCoresByOS = mapOf(
        OSType.WINDOWS to listOf(4, 6, 8, 12, 16),
        OSType.MAC to listOf(8, 10, 12),  // Apple Silicon typically 8+
        OSType.LINUX to listOf(4, 6, 8, 12, 16)
    )

    private val memoryValues = listOf(4, 8, 16, 32)
    private val colorDepths = listOf(24, 30, 32)

    fun generateRandom(name: String = "Profile ${Random.nextInt(1000, 9999)}"): BrowserProfile {
        val random = Random(System.nanoTime())

        // Pick a consistent configuration
        val config = consistentConfigs.random(random)
        val chromeVersion = chromeVersions.random(random)
        val ua = config.userAgentTemplate.replace("{version}", chromeVersion.toString())

        val lang = languages.random(random)
        val tz = timezones.random(random)
        val res = screenResolutions.random(random)
        val cores = cpuCoresByOS[config.osType]?.random(random) ?: 8

        // Generate plugins JSON
        val pluginsJson = if (config.plugins.isNotEmpty()) {
            "[" + config.plugins.joinToString(",") { "\"$it\"" } + "]"
        } else {
            "[]"
        }

        return BrowserProfile(
            name = name,
            osType = config.osType.osName,
            userAgent = ua,
            platform = config.osType.platform,
            language = lang,
            languages = "[\"${lang}\",\"${lang.split("-")[0]}\"]",
            screenWidth = res.first,
            screenHeight = res.second,
            devicePixelRatio = res.third,
            colorDepth = colorDepths.random(random),
            hardwareConcurrency = cores,
            deviceMemory = memoryValues.random(random),
            maxTouchPoints = 0,  // Desktop browsers typically 0
            canvasNoiseSeed = random.nextInt(1, 65535),
            webglVendor = config.webglVendor,
            webglRenderer = config.webglRenderer,
            audioNoiseSeed = random.nextInt(1, 65535),
            timezone = tz.first,
            timezoneOffset = tz.second,
            timingSeed = random.nextInt(1, 65535),
            rectNoiseSeed = random.nextInt(1, 65535),
            prefersDark = random.nextBoolean(),
            doNotTrack = if (random.nextBoolean()) "1" else "null",
            blockWebRTC = true,
            blockCanvas = true,
            blockWebGL = false,
            blockAudioContext = true,
            pluginsJson = pluginsJson,
            proxyType = "none",
            adblockEnabled = true,
            cosmeticFilterEnabled = true
        )
    }

    // Generate profile with specific OS type
    fun generateForOS(osType: OSType, name: String = "Profile ${Random.nextInt(1000, 9999)}"): BrowserProfile {
        val random = Random(System.nanoTime())

        // Pick config matching OS
        val matchingConfigs = consistentConfigs.filter { it.osType == osType }
        val config = matchingConfigs.random(random)
        val chromeVersion = chromeVersions.random(random)
        val ua = config.userAgentTemplate.replace("{version}", chromeVersion.toString())

        val lang = languages.random(random)
        val tz = timezones.random(random)
        val res = screenResolutions.random(random)
        val cores = cpuCoresByOS[osType]?.random(random) ?: 8

        val pluginsJson = if (config.plugins.isNotEmpty()) {
            "[" + config.plugins.joinToString(",") { "\"$it\"" } + "]"
        } else {
            "[]"
        }

        return BrowserProfile(
            name = name,
            osType = osType.osName,
            userAgent = ua,
            platform = osType.platform,
            language = lang,
            languages = "[\"${lang}\",\"${lang.split("-")[0]}\"]",
            screenWidth = res.first,
            screenHeight = res.second,
            devicePixelRatio = res.third,
            colorDepth = colorDepths.random(random),
            hardwareConcurrency = cores,
            deviceMemory = memoryValues.random(random),
            maxTouchPoints = 0,
            canvasNoiseSeed = random.nextInt(1, 65535),
            webglVendor = config.webglVendor,
            webglRenderer = config.webglRenderer,
            audioNoiseSeed = random.nextInt(1, 65535),
            timezone = tz.first,
            timezoneOffset = tz.second,
            timingSeed = random.nextInt(1, 65535),
            rectNoiseSeed = random.nextInt(1, 65535),
            prefersDark = random.nextBoolean(),
            doNotTrack = if (random.nextBoolean()) "1" else "null",
            blockWebRTC = true,
            blockCanvas = true,
            blockWebGL = false,
            blockAudioContext = true,
            pluginsJson = pluginsJson,
            proxyType = "none",
            adblockEnabled = true,
            cosmeticFilterEnabled = true
        )
    }
}
