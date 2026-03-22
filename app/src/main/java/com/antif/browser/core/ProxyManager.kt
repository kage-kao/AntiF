package com.antif.browser.core

import android.webkit.WebView
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import com.antif.browser.data.BrowserProfile
import java.util.concurrent.Executor

object ProxyManager {

    fun applyProxy(profile: BrowserProfile, executor: Executor, onResult: (Boolean) -> Unit) {
        if (profile.proxyType == "none" || profile.proxyHost.isBlank()) {
            clearProxy(executor) { onResult(true) }
            return
        }

        when (profile.proxyType) {
            "http" -> applyHttpProxy(profile, executor, onResult)
            "socks5" -> applySocks5Proxy(profile, onResult)
            else -> onResult(false)
        }
    }

    private fun applyHttpProxy(profile: BrowserProfile, executor: Executor, onResult: (Boolean) -> Unit) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            try {
                val proxyUrl = if (profile.proxyUsername.isNotBlank()) {
                    "http://${profile.proxyUsername}:${profile.proxyPassword}@${profile.proxyHost}:${profile.proxyPort}"
                } else {
                    "http://${profile.proxyHost}:${profile.proxyPort}"
                }

                val proxyConfig = ProxyConfig.Builder()
                    .addProxyRule(proxyUrl)
                    .addDirect()
                    .build()

                ProxyController.getInstance().setProxyOverride(
                    proxyConfig,
                    executor
                ) { onResult(true) }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        } else {
            // Fallback: use system properties
            System.setProperty("http.proxyHost", profile.proxyHost)
            System.setProperty("http.proxyPort", profile.proxyPort.toString())
            System.setProperty("https.proxyHost", profile.proxyHost)
            System.setProperty("https.proxyPort", profile.proxyPort.toString())
            if (profile.proxyUsername.isNotBlank()) {
                System.setProperty("http.proxyUser", profile.proxyUsername)
                System.setProperty("http.proxyPassword", profile.proxyPassword)
            }
            onResult(true)
        }
    }

    private fun applySocks5Proxy(profile: BrowserProfile, onResult: (Boolean) -> Unit) {
        try {
            System.setProperty("socksProxyHost", profile.proxyHost)
            System.setProperty("socksProxyPort", profile.proxyPort.toString())
            if (profile.proxyUsername.isNotBlank()) {
                System.setProperty("java.net.socks.username", profile.proxyUsername)
                System.setProperty("java.net.socks.password", profile.proxyPassword)
            }
            // Clear HTTP proxy
            System.clearProperty("http.proxyHost")
            System.clearProperty("http.proxyPort")
            System.clearProperty("https.proxyHost")
            System.clearProperty("https.proxyPort")
            onResult(true)
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(false)
        }
    }

    fun clearProxy(executor: Executor, onDone: () -> Unit) {
        // Clear system properties
        System.clearProperty("http.proxyHost")
        System.clearProperty("http.proxyPort")
        System.clearProperty("https.proxyHost")
        System.clearProperty("https.proxyPort")
        System.clearProperty("http.proxyUser")
        System.clearProperty("http.proxyPassword")
        System.clearProperty("socksProxyHost")
        System.clearProperty("socksProxyPort")
        System.clearProperty("java.net.socks.username")
        System.clearProperty("java.net.socks.password")

        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            try {
                ProxyController.getInstance().clearProxyOverride(executor) { onDone() }
            } catch (e: Exception) {
                onDone()
            }
        } else {
            onDone()
        }
    }
}
