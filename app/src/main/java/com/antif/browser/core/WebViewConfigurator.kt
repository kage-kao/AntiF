package com.antif.browser.core

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import com.antif.browser.data.BrowserProfile
import java.io.File

object WebViewConfigurator {

    /**
     * Set isolated data directory for profile.
     * MUST be called BEFORE creating any WebView for this profile.
     * This ensures cookies, localStorage, cache are separate per profile.
     */
    fun setProfileDataDirectory(context: Context, profileId: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val dataDir = File(context.dataDir, "webview_profiles/profile_$profileId")
            if (!dataDir.exists()) {
                dataDir.mkdirs()
            }
            WebView.setDataDirectorySuffix("profile_$profileId")
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun configure(webView: WebView, profile: BrowserProfile) {
        val settings = webView.settings

        // Core settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.allowFileAccess = false
        settings.allowContentAccess = false

        // Cache & storage
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.setSupportMultipleWindows(false)

        // Display - CRITICAL for proper website rendering
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.setSupportZoom(true)
        
        // Text size - keep default
        settings.textZoom = 100
        
        // Viewport settings for mobile sites - use NORMAL for proper layout
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        
        // Scrolling
        webView.isVerticalScrollBarEnabled = true
        webView.isHorizontalScrollBarEnabled = false
        webView.overScrollMode = WebView.OVER_SCROLL_NEVER
        
        // Hardware acceleration
        webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null)

        // Media
        settings.mediaPlaybackRequiresUserGesture = false
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        
        // Modern web features
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.loadsImagesAutomatically = true
        settings.blockNetworkImage = false
        settings.blockNetworkLoads = false

        // UserAgent
        settings.userAgentString = if (profile.userAgent.isNotBlank()) {
            profile.userAgent
        } else {
            getDefaultUserAgent(webView)
        }

        // Geolocation disabled
        settings.setGeolocationEnabled(false)

        // Cookies - enabled but isolated per profile via data directory
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        // Debug
        WebView.setWebContentsDebuggingEnabled(true)
        
        // Initial scale
        webView.setInitialScale(0)
    }
    
    private fun getDefaultUserAgent(webView: WebView): String {
        val defaultUA = webView.settings.userAgentString
        return if (defaultUA.contains("Chrome/") && !defaultUA.contains("Chrome/1")) {
            defaultUA
        } else {
            "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}; ${Build.MODEL}) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        }
    }
    
    /**
     * Clear all data for current profile's WebView
     */
    fun clearSessionData(webView: WebView) {
        webView.clearCache(true)
        webView.clearHistory()
        webView.clearFormData()

        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()

        WebStorage.getInstance().deleteAllData()
    }
    
    /**
     * Clear ONLY cookies for current profile
     */
    fun clearCookies() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
    }

    fun getCookiesForUrl(url: String): String {
        val cookieManager = CookieManager.getInstance()
        return cookieManager.getCookie(url) ?: ""
    }

    fun setCookieForUrl(url: String, cookie: String) {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setCookie(url, cookie)
        cookieManager.flush()
    }
    
    /**
     * Delete profile data directory completely
     */
    fun deleteProfileData(context: Context, profileId: Long) {
        val dataDir = File(context.dataDir, "webview_profiles/profile_$profileId")
        if (dataDir.exists()) {
            dataDir.deleteRecursively()
        }
    }
}
