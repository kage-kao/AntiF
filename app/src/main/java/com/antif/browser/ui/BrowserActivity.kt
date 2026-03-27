package com.antif.browser.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
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

    companion object {
        private const val TAG = "BrowserActivity"
    }

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
    
    // Track bypass application (now handled by JS flags _antifBypassApplied / _antifEarlyApplied)
    
    // Use global settings
    private val bypassFullEnabled get() = BypassSettings.fullBypassEnabled
    private val bypassBlockTracking get() = BypassSettings.blockTrackingEnabled
    private val bypassSpoofFeatureFlags get() = BypassSettings.spoofFeatureFlagsEnabled
    private val bypassNewDeviceId get() = BypassSettings.newDeviceIdEnabled
    private val bypassKillPostHog get() = BypassSettings.killPostHogEnabled
    private val bypassTurkeyRegion get() = BypassSettings.turkeyRegionEnabled
    private val bypassCanadaRegion get() = BypassSettings.canadaRegionEnabled
    private val bypassApplyToAllSites get() = BypassSettings.applyToAllSitesEnabled
    
    // Logging helper
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
        
        // CRITICAL: Set isolated data directory BEFORE super.onCreate()
        try {
            WebViewConfigurator.setProfileDataDirectory(this, profileId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set WebView data directory", e)
            // Continue anyway - will use default directory
        }
        
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

                // Create first tab
                val startUrl = bookmarkUrl ?: p.homepage
                createNewTab(startUrl)
            } ?: run {
                if (!isFinishing) {
                    Toast.makeText(this@BrowserActivity, "Profile not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
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

    // ==================== HELPER: Extract domain ====================
    
    private fun extractDomain(url: String?): String {
        if (url == null) return ""
        return try {
            Uri.parse(url).host ?: ""
        } catch (e: Exception) {
            ""
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

        webView.visibility = View.GONE
        webViewContainer.addView(webView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        addTabView(tab)
        switchToTab(tabId)
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
        tabs.forEach { tab ->
            tab.webView.visibility = View.GONE
            tab.tabView?.setBackgroundColor(Color.TRANSPARENT)
            tab.tabView?.findViewById<TextView>(R.id.tvTabTitle)?.setTextColor(Color.parseColor("#6B6B6B"))
        }

        val tab = tabs.find { it.id == tabId } ?: return
        tab.webView.visibility = View.VISIBLE
        tab.tabView?.setBackgroundColor(Color.parseColor("#1C1C1E"))
        tab.tabView?.findViewById<TextView>(R.id.tvTabTitle)?.setTextColor(Color.parseColor("#00D4FF"))

        activeTabId = tabId
        urlBar.setText(tab.url)
    }

    private fun closeTab(tabId: Int) {
        if (tabs.size <= 1) {
            finish()
            return
        }

        val tabIndex = tabs.indexOfFirst { it.id == tabId }
        if (tabIndex == -1) return

        val tab = tabs[tabIndex]
        webViewContainer.removeView(tab.webView)
        tab.webView.destroy()
        tabContainer.removeView(tab.tabView)
        tabs.removeAt(tabIndex)

        if (activeTabId == tabId) {
            val newIndex = tabIndex.coerceAtMost(tabs.size - 1).coerceAtLeast(0)
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
                
                // ═══════════════════════════════════════════════════════════
                // EARLY INJECTION - КРИТИЧНО: инжектим ДО загрузки JS страницы
                // Это перехватывает localStorage/fetch/FingerprintJS раньше
                // чем страница успеет записать fp_dedup или прочитать sb-auth
                // ═══════════════════════════════════════════════════════════
                val currentUrl = url?.lowercase() ?: ""
                val isEmergentSite = currentUrl.contains("emergent.sh") || currentUrl.contains("emergentagent.com")
                val shouldApplyEarly = isEmergentSite || bypassApplyToAllSites
                
                if (shouldApplyEarly && bypassFullEnabled) {
                    view?.evaluateJavascript(EmergentBypass.generateEarlyBypassScript(), null)
                    log("INJECT_EARLY", url ?: "", "Early bypass hooks injected in onPageStarted")
                }
                
                // Also inject fingerprint spoof early
                if (spoofScript.isNotEmpty()) {
                    view?.evaluateJavascript(spoofScript, null)
                    log("INJECT_EARLY", url ?: "", "Fingerprint spoof injected in onPageStarted")
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                log("PAGE_FINISH", url ?: "", "Page loaded successfully")
                
                if (view?.let { isActiveTab(it) } == true) {
                    progressBar.visibility = View.GONE
                }

                // Re-inject fingerprint spoofing script (reinforcement)
                view?.evaluateJavascript(spoofScript, null)
                log("INJECT", url ?: "", "Fingerprint spoof script re-injected")

                // Inject cosmetic ad blocking
                if (cosmeticEnabled && adblockEnabled) {
                    view?.evaluateJavascript(CosmeticFilter.generateHideAdsScript(), null)
                }
                
                // Check if bypass should be applied
                val currentUrl = url?.lowercase() ?: ""
                val domain = extractDomain(url)
                val isEmergentSite = currentUrl.contains("emergent.sh") || currentUrl.contains("emergentagent.com")
                val shouldApplyBypass = isEmergentSite || bypassApplyToAllSites
                
                // Apply full bypass (de-dup by _antifBypassApplied flag in JS)
                if (shouldApplyBypass && domain.isNotEmpty()) {
                    log("BYPASS", url ?: "", "Applying bypass for domain: $domain")
                    
                    // Apply enabled bypass features
                    if (bypassFullEnabled) {
                        view?.evaluateJavascript(EmergentBypass.generateFullBypassScript(), null)
                        log("INJECT", url ?: "", "FULL BYPASS script injected")
                    } else {
                        if (bypassBlockTracking) {
                            view?.evaluateJavascript(EmergentBypass.generateTrackingBlockerScript(), null)
                        }
                        if (bypassNewDeviceId) {
                            view?.evaluateJavascript(EmergentBypass.generateDeviceIdSpoofScript(), null)
                        }
                        if (bypassSpoofFeatureFlags) {
                            view?.evaluateJavascript(EmergentBypass.generateFeatureFlagsSpoofScript(), null)
                        }
                        if (bypassKillPostHog) {
                            view?.evaluateJavascript(EmergentBypass.generatePostHogNeutralizeScript(), null)
                        }
                    }
                    
                    // Apply regional settings (only one!)
                    if (bypassTurkeyRegion) {
                        view?.evaluateJavascript(EmergentBypass.generateRegionalDiscountScript("turkey"), null)
                    } else if (bypassCanadaRegion) {
                        view?.evaluateJavascript(EmergentBypass.generateRegionalDiscountScript("canada"), null)
                    }
                }

                // Update blocked count
                updateBlockedCount()

                // Update tab title
                view?.title?.let { title ->
                    val tid = getTabIdForWebView(view)
                    updateTabTitle(tid, title)
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                log("NAVIGATE", url, "Navigation request")

                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (_: Exception) {}
                    return true
                }
                return false
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val req = request ?: return null
                val url = req.url?.toString() ?: return null

                // AdBlock
                if (adblockEnabled && AdBlockEngine.shouldBlock(req)) {
                    runOnUiThread { updateBlockedCount() }
                    log("BLOCK", url, "Blocked by AdBlock")
                    return AdBlockEngine.getBlockedResponse()
                }

                // Fingerprint script blocking
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
        btnRefresh.setOnClickListener { 
            // Reset JS bypass flags and reload
            getActiveWebView()?.evaluateJavascript("window._antifBypassApplied=false;window._antifEarlyApplied=false;", null)
            getActiveWebView()?.reload() 
        }

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
            val bypassStatus = if (bypassFullEnabled) "ON" else "OFF"
            menu.add(0, 100, 0, "BYPASS: $bypassStatus")
            
            // Manual actions (EXPLICIT user request)
            menu.add(1, 51, 0, "🔄 Reset modals (show offers)")
            menu.add(1, 52, 0, "🗑️ Full reset (clear all)")
            menu.add(1, 55, 0, "🆔 New Device-Id")
            menu.add(1, 56, 0, "🔍 Check trial usage")
            menu.add(1, 57, 0, "♻️ Reset trial data")
            
            // Regional
            menu.add(2, 61, 0, "🇹🇷 Turkey (Free Weekend)")
            menu.add(2, 62, 0, "🇨🇦 Canada (Free Weekend)")
            
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
                    100 -> {
                        BypassSettings.fullBypassEnabled = !BypassSettings.fullBypassEnabled
                        val status = if (BypassSettings.fullBypassEnabled) "ON" else "OFF"
                        Toast.makeText(this@BrowserActivity, "BYPASS: $status", Toast.LENGTH_SHORT).show()
                        
                        // Reset JS bypass flags to allow re-application
                        EmergentBypass.resetIds()
                        getActiveWebView()?.evaluateJavascript("window._antifBypassApplied=false;window._antifEarlyApplied=false;", null)
                        
                        if (BypassSettings.fullBypassEnabled) {
                            getActiveWebView()?.evaluateJavascript(EmergentBypass.generateFullBypassScript(), null)
                        }
                        getActiveWebView()?.reload()
                    }
                    
                    // Manual actions
                    51 -> {
                        // Reset modals - EXPLICIT user action
                        getActiveWebView()?.evaluateJavascript(EmergentBypass.generateResetModalsScript()) { result ->
                            runOnUiThread {
                                Toast.makeText(this@BrowserActivity, "Modals reset. Reload to see offers.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    52 -> {
                        // Full reset - EXPLICIT user action
                        android.app.AlertDialog.Builder(this@BrowserActivity)
                            .setTitle("Full Reset")
                            .setMessage("This will clear ALL local data (localStorage, sessionStorage, IndexedDB) except auth tokens.\n\nModals will appear again.\n\nContinue?")
                            .setPositiveButton("Reset") { _, _ ->
                                getActiveWebView()?.evaluateJavascript(EmergentBypass.generateFullResetScript()) { result ->
                                    runOnUiThread {
                                        Toast.makeText(this@BrowserActivity, "Full reset complete. Reload page.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                    55 -> {
                        EmergentBypass.resetIds()
                        getActiveWebView()?.evaluateJavascript(EmergentBypass.generateDeviceIdSpoofScript()) { result ->
                            runOnUiThread {
                                Toast.makeText(this@BrowserActivity, "New Device-Id: ${EmergentBypass.getOrCreateDeviceId().take(8)}...", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    56 -> {
                        // Check trial usage - FIXED parsing
                        getActiveWebView()?.evaluateJavascript(EmergentBypass.generateTrialDetectionScript()) { result ->
                            try {
                                // Clean up escaped JSON from WebView
                                val cleanJson = result
                                    ?.trim()
                                    ?.removeSurrounding("\"")
                                    ?.replace("\\\"", "\"")
                                    ?.replace("\\n", "\n")
                                    ?: "{}"
                                
                                val parsed = org.json.JSONObject(cleanJson)
                                val trialUsed = parsed.optBoolean("trialUsed", false)
                                val indicators = parsed.optJSONArray("indicators")?.let { arr ->
                                    (0 until arr.length()).map { arr.getString(it) }
                                } ?: emptyList()
                                val message = parsed.optString("message", "Check console for details")
                                
                                runOnUiThread {
                                    android.app.AlertDialog.Builder(this@BrowserActivity)
                                        .setTitle(if (trialUsed) "⚠️ TRIAL DETECTED" else "✅ No Trial Found")
                                        .setMessage("$message\n\nIndicators: ${indicators.size}\n${indicators.joinToString("\n• ", prefix = "• ")}\n\nNote: Server-side trial data cannot be detected.")
                                        .setPositiveButton("OK", null)
                                        .setNeutralButton("Reset Trial") { _, _ ->
                                            getActiveWebView()?.evaluateJavascript(EmergentBypass.generateTrialResetScript(), null)
                                            Toast.makeText(this@BrowserActivity, "Trial data reset. Reload page.", Toast.LENGTH_LONG).show()
                                        }
                                        .show()
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Trial detection parse error", e)
                                runOnUiThread {
                                    Toast.makeText(this@BrowserActivity, "Check console for results", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    57 -> {
                        getActiveWebView()?.evaluateJavascript(EmergentBypass.generateTrialResetScript()) { _ ->
                            runOnUiThread {
                                Toast.makeText(this@BrowserActivity, "Trial data reset. Reload page.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    
                    // Regional
                    61 -> {
                        // Disable Canada, enable Turkey
                        BypassSettings.canadaRegionEnabled = false
                        BypassSettings.turkeyRegionEnabled = true
                        getActiveWebView()?.evaluateJavascript(EmergentBypass.generateRegionalDiscountScript("turkey"), null)
                        Toast.makeText(this@BrowserActivity, "Turkey region applied.\nUse 'Reset modals' then reload to see Free Weekend offer.", Toast.LENGTH_LONG).show()
                    }
                    62 -> {
                        // Disable Turkey, enable Canada
                        BypassSettings.turkeyRegionEnabled = false
                        BypassSettings.canadaRegionEnabled = true
                        getActiveWebView()?.evaluateJavascript(EmergentBypass.generateRegionalDiscountScript("canada"), null)
                        Toast.makeText(this@BrowserActivity, "Canada region applied.\nUse 'Reset modals' then reload to see Free Weekend offer.", Toast.LENGTH_LONG).show()
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
                            EmergentBypass.resetIds()
                            wv.evaluateJavascript("window._antifBypassApplied=false;window._antifEarlyApplied=false;", null)
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
        tabs.forEach { 
            try {
                it.webView.destroy() 
            } catch (e: Exception) {
                Log.e(TAG, "Error destroying WebView", e)
            }
        }
        tabs.clear()
        super.onDestroy()
    }
}
