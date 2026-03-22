package com.antif.browser.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.antif.browser.AntiFApplication
import com.antif.browser.R
import com.antif.browser.core.AdBlockEngine
import com.antif.browser.core.CosmeticFilter
import com.antif.browser.core.EmergentBypass
import com.antif.browser.core.FingerprintSpoofer
import com.antif.browser.core.ProxyManager
import com.antif.browser.core.WebViewConfigurator
import com.antif.browser.data.BrowserProfile
import com.antif.browser.data.BypassSettings
import com.antif.browser.data.SessionLogger
import kotlinx.coroutines.launch

class BrowserActivity : AppCompatActivity() {

    // Views
    private lateinit var tabContainer: LinearLayout
    private lateinit var btnNewTab: ImageButton
    private lateinit var tvTabCount: TextView
    private lateinit var urlBar: EditText
    private lateinit var btnRefresh: ImageButton
    private lateinit var btnMenu: ImageButton
    private lateinit var btnConsole: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var tvBlockedCount: TextView
    private lateinit var webViewContainer: FrameLayout

    // Tab system
    data class Tab(
        val id: Int,
        val webView: WebView,
        var url: String = "",
        var title: String = "New Tab",
        var tabView: View? = null
    )

    private val tabs = mutableListOf<Tab>()
    private var activeTabId = -1
    private var nextTabId = 0

    // Profile
    private var profile: BrowserProfile? = null
    private var spoofScript: String = ""
    private var adblockEnabled = true
    private var cosmeticEnabled = true
    
    // Use global settings
    private val bypassFullEnabled get() = BypassSettings.fullBypassEnabled
    private val bypassBlockTracking get() = BypassSettings.blockTrackingEnabled
    private val bypassSpoofFeatureFlags get() = BypassSettings.spoofFeatureFlagsEnabled
    private val bypassNewDeviceId get() = BypassSettings.newDeviceIdEnabled
    private val bypassKillPostHog get() = BypassSettings.killPostHogEnabled
    private val bypassTurkeyRegion get() = BypassSettings.turkeyRegionEnabled
    private val bypassCanadaRegion get() = BypassSettings.canadaRegionEnabled
    private val bypassApplyToAllSites get() = BypassSettings.applyToAllSitesEnabled
    
    // Logging helper using global SessionLogger
    private fun log(type: String, url: String, details: String) {
        SessionLogger.log(type, url, details)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val profileId = intent.getLongExtra("profile_id", -1)
        if (profileId == -1L) {
            Toast.makeText(this, "Invalid profile", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Initialize global settings
        BypassSettings.init(this)
        
        // CRITICAL: Set isolated data directory BEFORE creating any WebView
        // This ensures cookies, localStorage, cache are completely separate per profile
        WebViewConfigurator.setProfileDataDirectory(this, profileId)
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)

        initViews()
        
        // Check if we have a bookmark URL to open
        val bookmarkUrl = intent.getStringExtra("bookmark_url")

        lifecycleScope.launch {
            val dao = AntiFApplication.instance.database.profileDao()
            profile = dao.getProfileById(profileId)

            profile?.let { p ->
                spoofScript = FingerprintSpoofer.generateSpoofScript(p)
                adblockEnabled = p.adblockEnabled
                cosmeticEnabled = p.cosmeticFilterEnabled
                setupProxy(p)

                tvStatus.text = p.name
                SessionLogger.setProfileName(p.name)

                // Create first tab - use bookmark URL if provided, otherwise homepage
                val startUrl = bookmarkUrl ?: p.homepage
                createNewTab(startUrl)
            } ?: run {
                Toast.makeText(this@BrowserActivity, "Profile not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        setupUrlBar()
        setupNavButtons()
    }

    private fun initViews() {
        tabContainer = findViewById(R.id.tabContainer)
        btnNewTab = findViewById(R.id.btnNewTab)
        tvTabCount = findViewById(R.id.tvTabCount)
        urlBar = findViewById(R.id.etUrl)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnMenu = findViewById(R.id.btnBrowserMenu)
        btnConsole = findViewById(R.id.btnConsole)
        progressBar = findViewById(R.id.progressBar)
        tvStatus = findViewById(R.id.tvStatus)
        tvBlockedCount = findViewById(R.id.tvBlockedCount)
        webViewContainer = findViewById(R.id.webViewContainer)

        btnNewTab.setOnClickListener {
            createNewTab(profile?.homepage ?: "https://www.google.com")
        }
    }

    // ==================== TAB MANAGEMENT ====================

    @SuppressLint("SetJavaScriptEnabled")
    private fun createNewTab(url: String) {
        val p = profile ?: return

        val webView = WebView(this)
        configureWebView(webView, p)

        val tabId = nextTabId++
        val tab = Tab(id = tabId, webView = webView, url = url)
        tabs.add(tab)

        // Add WebView to container (hidden initially)
        webView.visibility = View.GONE
        webViewContainer.addView(webView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        // Create tab UI
        addTabView(tab)

        // Switch to new tab
        switchToTab(tabId)

        // Load URL
        webView.loadUrl(url)

        updateTabCount()
    }

    private fun addTabView(tab: Tab) {
        val tabView = LayoutInflater.from(this).inflate(R.layout.item_tab, tabContainer, false)
        val tvTitle = tabView.findViewById<TextView>(R.id.tvTabTitle)
        val btnClose = tabView.findViewById<ImageButton>(R.id.btnCloseTab)

        tvTitle.text = tab.title

        tabView.setOnClickListener { switchToTab(tab.id) }
        btnClose.setOnClickListener { closeTab(tab.id) }

        tab.tabView = tabView
        tabContainer.addView(tabView)
    }

    private fun switchToTab(tabId: Int) {
        // Hide all WebViews
        tabs.forEach { tab ->
            tab.webView.visibility = View.GONE
            tab.tabView?.setBackgroundColor(Color.TRANSPARENT)
            tab.tabView?.findViewById<TextView>(R.id.tvTabTitle)?.setTextColor(Color.parseColor("#6B6B6B"))
        }

        // Show selected tab
        val tab = tabs.find { it.id == tabId } ?: return
        tab.webView.visibility = View.VISIBLE
        tab.tabView?.setBackgroundColor(Color.parseColor("#1C1C1E"))
        tab.tabView?.findViewById<TextView>(R.id.tvTabTitle)?.setTextColor(Color.parseColor("#00D4FF"))

        activeTabId = tabId

        // Update URL bar
        urlBar.setText(tab.url)
    }

    private fun closeTab(tabId: Int) {
        if (tabs.size <= 1) {
            // Last tab — close browser
            finish()
            return
        }

        val tabIndex = tabs.indexOfFirst { it.id == tabId }
        if (tabIndex == -1) return

        val tab = tabs[tabIndex]

        // Remove WebView
        webViewContainer.removeView(tab.webView)
        tab.webView.destroy()

        // Remove tab view
        tabContainer.removeView(tab.tabView)

        // Remove from list
        tabs.removeAt(tabIndex)

        // Switch to adjacent tab if active tab was closed
        if (activeTabId == tabId) {
            val newIndex = (tabIndex).coerceAtMost(tabs.size - 1).coerceAtLeast(0)
            switchToTab(tabs[newIndex].id)
        }

        updateTabCount()
    }

    private fun updateTabCount() {
        tvTabCount.text = tabs.size.toString()
    }

    private fun updateTabTitle(tabId: Int, title: String) {
        val tab = tabs.find { it.id == tabId } ?: return
        tab.title = title.take(20)
        tab.tabView?.findViewById<TextView>(R.id.tvTabTitle)?.text = tab.title
    }

    private fun updateTabUrl(tabId: Int, url: String) {
        val tab = tabs.find { it.id == tabId } ?: return
        tab.url = url
    }

    private fun getActiveWebView(): WebView? {
        return tabs.find { it.id == activeTabId }?.webView
    }

    // ==================== WEBVIEW CONFIGURATION ====================

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView(webView: WebView, p: BrowserProfile) {
        WebViewConfigurator.configure(webView, p)

        val tabId = nextTabId // capture for closure (will be incremented after)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                log("PAGE_START", url ?: "", "Page loading started")
                if (view?.let { isActiveTab(it) } == true) {
                    progressBar.visibility = View.VISIBLE
                    url?.let {
                        urlBar.setText(it)
                        updateTabUrl(getTabIdForWebView(view), it)
                    }
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                log("PAGE_FINISH", url ?: "", "Page loaded successfully")
                
                if (view?.let { isActiveTab(it) } == true) {
                    progressBar.visibility = View.GONE
                }

                // Inject fingerprint spoofing script
                view?.evaluateJavascript(spoofScript, null)
                log("INJECT", url ?: "", "Fingerprint spoof script injected")

                // Inject cosmetic ad blocking
                if (cosmeticEnabled && adblockEnabled) {
                    view?.evaluateJavascript(CosmeticFilter.generateHideAdsScript(), null)
                    log("INJECT", url ?: "", "Cosmetic ad blocking script injected")
                }
                
                // Check if bypass should be applied
                val currentUrl = url?.lowercase() ?: ""
                val isEmergentSite = currentUrl.contains("emergent.sh") || currentUrl.contains("emergentagent.com")
                val shouldApplyBypass = isEmergentSite || bypassApplyToAllSites
                
                if (shouldApplyBypass) {
                    log("BYPASS", url ?: "", "Applying bypass (AllSites=$bypassApplyToAllSites, Emergent=$isEmergentSite)")
                    
                    // Apply enabled bypass features automatically
                    if (bypassFullEnabled) {
                        view?.evaluateJavascript(EmergentBypass.generateFullBypassScript(), null)
                        log("INJECT", url ?: "", "FULL BYPASS script injected")
                    } else {
                        if (bypassBlockTracking) {
                            view?.evaluateJavascript(EmergentBypass.generateTrackingBlockerScript(), null)
                            log("INJECT", url ?: "", "Block tracking script injected")
                        }
                        if (bypassNewDeviceId) {
                            view?.evaluateJavascript(EmergentBypass.generateDeviceIdSpoofScript(), null)
                            log("INJECT", url ?: "", "Device-Id spoof script injected")
                        }
                        if (bypassSpoofFeatureFlags) {
                            view?.evaluateJavascript(EmergentBypass.generateFeatureFlagsSpoofScript(), null)
                            log("INJECT", url ?: "", "Feature flags spoof script injected")
                        }
                        if (bypassKillPostHog) {
                            view?.evaluateJavascript(EmergentBypass.generatePostHogNeutralizeScript(), null)
                            log("INJECT", url ?: "", "PostHog neutralize script injected")
                        }
                        if (bypassTurkeyRegion) {
                            view?.evaluateJavascript(EmergentBypass.generateRegionalDiscountScript("turkey"), null)
                            log("INJECT", url ?: "", "Turkey regional script injected")
                        }
                        if (bypassCanadaRegion) {
                            view?.evaluateJavascript(EmergentBypass.generateRegionalDiscountScript("canada"), null)
                            log("INJECT", url ?: "", "Canada regional script injected")
                        }
                    }
                }

                // Update blocked count
                updateBlockedCount()

                // Update tab title
                view?.title?.let { title ->
                    val tid = getTabIdForWebView(view)
                    updateTabTitle(tid, title)
                    log("EVENT", url ?: "", "Page title: $title")
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                log("NAVIGATE", url, "Navigation request: method=${request.method}")

                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    try {
                        log("EXTERNAL", url, "Opening external app")
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (_: Exception) {}
                    return true
                }
                return false
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val req = request ?: return null
                val url = req.url?.toString() ?: return null
                val method = req.method ?: "GET"
                val headers = req.requestHeaders?.entries?.joinToString(", ") { "${it.key}=${it.value}" } ?: ""
                
                log("REQUEST", url, "Method: $method, Headers: $headers")

                // AdBlock: network-level blocking
                if (adblockEnabled && AdBlockEngine.shouldBlock(req)) {
                    runOnUiThread { updateBlockedCount() }
                    log("BLOCK", url, "Blocked by AdBlock")
                    return AdBlockEngine.getBlockedResponse()
                }

                // Fingerprint script blocking (always active)
                val fpDomains = listOf("fpjs.io", "fpnpmcdn.net", "fpcdn.io")
                if (fpDomains.any { url.contains(it) }) {
                    log("BLOCK", url, "Blocked fingerprint domain")
                    return WebResourceResponse("text/plain", "UTF-8", "".byteInputStream())
                }

                return null
            }
            
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                val url = request?.url?.toString() ?: ""
                val errorCode = error?.errorCode ?: -1
                val errorDesc = error?.description?.toString() ?: "Unknown"
                log("ERROR", url, "Error $errorCode: $errorDesc")
            }
            
            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                super.onReceivedHttpError(view, request, errorResponse)
                val url = request?.url?.toString() ?: ""
                val statusCode = errorResponse?.statusCode ?: -1
                val reason = errorResponse?.reasonPhrase ?: "Unknown"
                log("HTTP_ERROR", url, "HTTP $statusCode: $reason")
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (view?.let { isActiveTab(it) } == true) {
                    progressBar.progress = newProgress
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                view?.let { wv ->
                    title?.let { t ->
                        updateTabTitle(getTabIdForWebView(wv), t)
                    }
                }
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                consoleMessage?.let { msg ->
                    val level = msg.messageLevel()?.name ?: "LOG"
                    val text = msg.message() ?: ""
                    val line = msg.lineNumber()
                    val source = msg.sourceId() ?: ""
                    log("CONSOLE", source, "[$level:$line] $text")
                }
                return super.onConsoleMessage(consoleMessage)
            }
        }
    }

    private fun isActiveTab(webView: WebView): Boolean {
        return tabs.find { it.id == activeTabId }?.webView == webView
    }

    private fun getTabIdForWebView(webView: WebView): Int {
        return tabs.find { it.webView == webView }?.id ?: -1
    }

    // ==================== ADBLOCK ====================

    private fun updateBlockedCount() {
        val count = AdBlockEngine.getSessionBlockedCount()
        tvBlockedCount.text = if (count > 0) "Blocked: $count" else ""
    }

    // ==================== PROXY ====================

    private fun setupProxy(p: BrowserProfile) {
        ProxyManager.applyProxy(p, mainExecutor) { success ->
            if (!success) {
                runOnUiThread {
                    tvStatus.text = "Proxy failed! Profile: ${p.name}"
                }
            }
        }
    }

    // ==================== URL BAR ====================

    private fun setupUrlBar() {
        urlBar.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                navigateTo(urlBar.text.toString())
                true
            } else false
        }
    }

    private fun navigateTo(input: String) {
        val url = when {
            input.startsWith("http://") || input.startsWith("https://") -> input
            input.contains(".") && !input.contains(" ") -> "https://$input"
            else -> "https://www.google.com/search?q=${Uri.encode(input)}"
        }
        getActiveWebView()?.loadUrl(url)
    }

    // ==================== NAVIGATION ====================

    private fun setupNavButtons() {
        btnRefresh.setOnClickListener { getActiveWebView()?.reload() }

        btnConsole.setOnClickListener {
            val intent = Intent(this, ConsoleActivity::class.java).apply {
                putExtra("profile_id", profile?.id ?: -1)
            }
            startActivity(intent)
        }

        btnMenu.setOnClickListener { view -> showBrowserMenu(view) }
    }
    
    private fun showBrowserMenu(anchor: View) {
        PopupMenu(this, anchor).apply {
            // BYPASS toggle (single switch)
            val bypassStatus = if (bypassFullEnabled) "ON" else "OFF"
            menu.add(0, 100, 0, "BYPASS: $bypassStatus")
            
            // Manual actions
            menu.add(1, 51, 0, "Reset modals")
            menu.add(1, 55, 0, "New Device-Id")
            menu.add(1, 56, 0, "Check trial usage")
            menu.add(1, 57, 0, "Reset trial data")
            
            // Regional
            menu.add(2, 61, 0, "Turkey (Free)")
            menu.add(2, 62, 0, "Canada (Free)")
            
            // AdBlock
            menu.add(3, 4, 0, if (adblockEnabled) "AdBlock: ON" else "AdBlock: OFF")
            menu.add(3, 5, 0, "AdBlock stats")
            
            // Privacy
            menu.add(4, 6, 0, "Clear cookies")
            menu.add(4, 7, 0, "Clear all data")
            menu.add(4, 9, 0, "Cookie manager")
            
            // Tools
            menu.add(5, 15, 0, "Card generator")
            menu.add(5, 12, 0, "View fingerprint")
            menu.add(5, 13, 0, "New identity")
            menu.add(5, 14, 0, "Close browser")

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    // BYPASS toggle
                    100 -> {
                        BypassSettings.fullBypassEnabled = !BypassSettings.fullBypassEnabled
                        val status = if (BypassSettings.fullBypassEnabled) "ON" else "OFF"
                        Toast.makeText(this@BrowserActivity, "BYPASS: $status", Toast.LENGTH_SHORT).show()
                        if (BypassSettings.fullBypassEnabled) {
                            getActiveWebView()?.evaluateJavascript(EmergentBypass.generateFullBypassScript(), null)
                        }
                        getActiveWebView()?.reload()
                    }
                    
                    // Manual actions
                    51 -> {
                        getActiveWebView()?.evaluateJavascript(EmergentBypass.generateResetModalsScript(), null)
                        Toast.makeText(this@BrowserActivity, "Modals reset", Toast.LENGTH_SHORT).show()
                    }
                    55 -> {
                        getActiveWebView()?.evaluateJavascript(EmergentBypass.generateDeviceIdSpoofScript(), null)
                        Toast.makeText(this@BrowserActivity, "New Device-Id", Toast.LENGTH_SHORT).show()
                    }
                    56 -> {
                        // Check trial usage
                        getActiveWebView()?.evaluateJavascript(EmergentBypass.generateTrialDetectionScript()) { result ->
                            try {
                                val json = result?.removeSurrounding("\"")?.replace("\\\"", "\"")
                                    ?.replace("\\n", "\n") ?: "{}"
                                val parsed = org.json.JSONObject(json)
                                val trialUsed = parsed.optBoolean("trialUsed", false)
                                val indicators = parsed.optInt("indicators", 0)
                                val message = parsed.optString("message", "Unknown")
                                
                                runOnUiThread {
                                    android.app.AlertDialog.Builder(this@BrowserActivity)
                                        .setTitle(if (trialUsed) "⚠️ TRIAL USED" else "✅ No Trial")
                                        .setMessage("$message\n\nIndicators found: $indicators\n\nCheck console for details.")
                                        .setPositiveButton("OK", null)
                                        .setNeutralButton("Reset Trial") { _, _ ->
                                            getActiveWebView()?.evaluateJavascript(EmergentBypass.generateTrialResetScript(), null)
                                            Toast.makeText(this@BrowserActivity, "Trial data reset. Reload page.", Toast.LENGTH_LONG).show()
                                        }
                                        .show()
                                }
                            } catch (e: Exception) {
                                runOnUiThread {
                                    Toast.makeText(this@BrowserActivity, "Check console for results", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    57 -> {
                        // Reset trial data
                        getActiveWebView()?.evaluateJavascript(EmergentBypass.generateTrialResetScript()) { result ->
                            runOnUiThread {
                                Toast.makeText(this@BrowserActivity, "Trial data reset. Reload page.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    
                    // Regional
                    61 -> {
                        getActiveWebView()?.evaluateJavascript(EmergentBypass.generateRegionalDiscountScript("turkey"), null)
                        Toast.makeText(this@BrowserActivity, "Turkey applied. Reload page.", Toast.LENGTH_SHORT).show()
                    }
                    62 -> {
                        getActiveWebView()?.evaluateJavascript(EmergentBypass.generateRegionalDiscountScript("canada"), null)
                        Toast.makeText(this@BrowserActivity, "Canada applied. Reload page.", Toast.LENGTH_SHORT).show()
                    }
                    
                    // AdBlock
                    4 -> {
                        adblockEnabled = !adblockEnabled
                        getActiveWebView()?.reload()
                        Toast.makeText(this@BrowserActivity,
                            if (adblockEnabled) "AdBlock ON" else "AdBlock OFF",
                            Toast.LENGTH_SHORT).show()
                    }
                    5 -> {
                        val stats = AdBlockEngine.getSessionStats()
                        val total = AdBlockEngine.getSessionBlockedCount()
                        val top5 = stats.entries.sortedByDescending { it.value }.take(5)
                        val msg = buildString {
                            append("Total blocked: $total\n")
                            append("Domains: ${AdBlockEngine.getBlockedDomainsCount()}\n")
                            append("Patterns: ${AdBlockEngine.getBlockedPatternsCount()}\n\n")
                            if (top5.isNotEmpty()) {
                                append("Top blocked:\n")
                                top5.forEach { append("  ${it.key}: ${it.value}\n") }
                            }
                        }
                        android.app.AlertDialog.Builder(this@BrowserActivity)
                            .setTitle("AdBlock Stats")
                            .setMessage(msg)
                            .setPositiveButton("OK", null)
                            .setNeutralButton("Reset") { _, _ ->
                                AdBlockEngine.resetSessionStats()
                                updateBlockedCount()
                            }
                            .show()
                    }
                    
                    // Privacy
                    6 -> {
                        WebViewConfigurator.clearCookies()
                        Toast.makeText(this@BrowserActivity, "Cookies cleared", Toast.LENGTH_SHORT).show()
                    }
                    7 -> {
                        getActiveWebView()?.let { WebViewConfigurator.clearSessionData(it) }
                        Toast.makeText(this@BrowserActivity, "All data cleared", Toast.LENGTH_SHORT).show()
                    }
                    9 -> {
                        val intent = Intent(this@BrowserActivity, CookieManagerActivity::class.java).apply {
                            putExtra("url", getActiveWebView()?.url ?: "")
                        }
                        startActivity(intent)
                    }
                    
                    // Tools
                    15 -> {
                        startActivity(Intent(this@BrowserActivity, CardGenActivity::class.java))
                    }
                    12 -> getActiveWebView()?.loadUrl("https://browserleaks.com/javascript")
                    13 -> {
                        getActiveWebView()?.let { wv ->
                            wv.evaluateJavascript(FingerprintSpoofer.generateCleanupScript(), null)
                            wv.evaluateJavascript(spoofScript, null)
                            wv.evaluateJavascript(EmergentBypass.generateTrackingBlockerScript(), null)
                        }
                        Toast.makeText(this@BrowserActivity, "New identity applied", Toast.LENGTH_SHORT).show()
                    }
                    14 -> finish()
                }
                true
            }
            show()
        }
    }

    // ==================== EXECUTE JS (from Console) ====================

    fun executeJavaScript(script: String, callback: ((String) -> Unit)? = null) {
        getActiveWebView()?.evaluateJavascript(script) { result ->
            callback?.invoke(result ?: "null")
        }
    }

    // ==================== LIFECYCLE ====================

    override fun onResume() {
        super.onResume()
        // Execute queued commands from Console
        val commands = ConsoleActivity.CommandQueue.drain()
        commands.forEach { cmd ->
            getActiveWebView()?.evaluateJavascript(cmd, null)
        }
    }

    override fun onBackPressed() {
        val wv = getActiveWebView()
        if (wv != null && wv.canGoBack()) {
            wv.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        ProxyManager.clearProxy(mainExecutor) {}
        tabs.forEach { it.webView.destroy() }
        tabs.clear()
        super.onDestroy()
    }
}
