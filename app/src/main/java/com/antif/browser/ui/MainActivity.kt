package com.antif.browser.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.antif.browser.AntiFApplication
import com.antif.browser.R
import com.antif.browser.core.WebViewConfigurator
import com.antif.browser.data.BrowserProfile
import com.antif.browser.data.BypassSettings
import com.antif.browser.data.SessionLogger
import com.antif.browser.utils.FingerprintGenerator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: View
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var btnClearAllData: ImageButton
    private lateinit var btnBypassSettings: ImageButton
    private lateinit var btnLogsSettings: ImageButton
    
    private val profileDao by lazy { AntiFApplication.instance.database.profileDao() }
    private val adapter = ProfileAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // BypassSettings already initialized in Application

        recyclerView = findViewById(R.id.recyclerProfiles)
        emptyView = findViewById(R.id.emptyView)
        fabAdd = findViewById(R.id.fabAddProfile)
        btnClearAllData = findViewById(R.id.btnClearAllData)
        btnBypassSettings = findViewById(R.id.btnBypassSettings)
        btnLogsSettings = findViewById(R.id.btnLogsSettings)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fabAdd.setOnClickListener { showAddOptions() }
        btnClearAllData.setOnClickListener { showClearAllDataDialog() }
        btnBypassSettings.setOnClickListener { showBypassSettingsDialog() }
        btnLogsSettings.setOnClickListener { showLogsDialog() }

        lifecycleScope.launch {
            profileDao.getAllProfiles().collectLatest { profiles ->
                adapter.submitList(profiles)
                emptyView.visibility = if (profiles.isEmpty()) View.VISIBLE else View.GONE
                recyclerView.visibility = if (profiles.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }
    
    // ==================== BYPASS SETTINGS DIALOG ====================
    
    private fun showBypassSettingsDialog() {
        val items = arrayOf(
            "Apply to ALL sites (not just Emergent)",
            "FULL BYPASS",
            "Block tracking",
            "Spoof Feature Flags",
            "New Device-Id",
            "Kill PostHog",
            "Turkey (Free)",
            "Canada (Free)"
        )
        
        val checkedItems = booleanArrayOf(
            BypassSettings.applyToAllSitesEnabled,
            BypassSettings.fullBypassEnabled,
            BypassSettings.blockTrackingEnabled,
            BypassSettings.spoofFeatureFlagsEnabled,
            BypassSettings.newDeviceIdEnabled,
            BypassSettings.killPostHogEnabled,
            BypassSettings.turkeyRegionEnabled,
            BypassSettings.canadaRegionEnabled
        )
        
        AlertDialog.Builder(this, R.style.Theme_AntiF_Dialog)
            .setTitle("⚡ Bypass Settings")
            .setMultiChoiceItems(items, checkedItems) { _, which, isChecked ->
                when (which) {
                    0 -> BypassSettings.applyToAllSitesEnabled = isChecked
                    1 -> BypassSettings.fullBypassEnabled = isChecked
                    2 -> BypassSettings.blockTrackingEnabled = isChecked
                    3 -> BypassSettings.spoofFeatureFlagsEnabled = isChecked
                    4 -> BypassSettings.newDeviceIdEnabled = isChecked
                    5 -> BypassSettings.killPostHogEnabled = isChecked
                    6 -> {
                        BypassSettings.turkeyRegionEnabled = isChecked
                        // Disable Canada if Turkey enabled (conflict)
                        if (isChecked) BypassSettings.canadaRegionEnabled = false
                    }
                    7 -> {
                        BypassSettings.canadaRegionEnabled = isChecked
                        // Disable Turkey if Canada enabled (conflict)
                        if (isChecked) BypassSettings.turkeyRegionEnabled = false
                    }
                }
            }
            .setPositiveButton("OK") { _, _ ->
                Toast.makeText(this, "Bypass settings saved!", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Enable All") { dialog, _ ->
                BypassSettings.applyToAllSitesEnabled = true
                BypassSettings.fullBypassEnabled = true
                BypassSettings.blockTrackingEnabled = true
                BypassSettings.spoofFeatureFlagsEnabled = true
                BypassSettings.newDeviceIdEnabled = true
                BypassSettings.killPostHogEnabled = true
                // Only enable one region at a time!
                BypassSettings.turkeyRegionEnabled = true
                BypassSettings.canadaRegionEnabled = false
                Toast.makeText(this, "All bypass features enabled!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .show()
    }
    
    // ==================== LOGS DIALOG ====================
    
    private fun showLogsDialog() {
        val logsCount = SessionLogger.getLogsCount()
        
        val items = arrayOf(
            if (BypassSettings.fullLoggingEnabled) "✅ Logging ENABLED" else "⬜ Logging DISABLED",
            "📋 View Logs ($logsCount entries)",
            "💾 Save Logs to File",
            "🗑️ Clear All Logs"
        )
        
        AlertDialog.Builder(this, R.style.Theme_AntiF_Dialog)
            .setTitle("📋 Logging")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> {
                        BypassSettings.fullLoggingEnabled = !BypassSettings.fullLoggingEnabled
                        val status = if (BypassSettings.fullLoggingEnabled) "ENABLED" else "DISABLED"
                        Toast.makeText(this, "Logging $status", Toast.LENGTH_SHORT).show()
                        if (BypassSettings.fullLoggingEnabled) {
                            SessionLogger.log("SYSTEM", "logging", "Full logging started from MainActivity")
                        }
                    }
                    1 -> showLogsViewDialog()
                    2 -> {
                        if (logsCount == 0) {
                            Toast.makeText(this, "No logs to save", Toast.LENGTH_SHORT).show()
                        } else {
                            val path = SessionLogger.saveToFile(this)
                            if (path != null) {
                                Toast.makeText(this, "Saved to: $path", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this, "Failed to save logs", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    3 -> {
                        val count = SessionLogger.getLogsCount()
                        SessionLogger.clearLogs()
                        Toast.makeText(this, "Cleared $count log entries", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }
    
    private fun showLogsViewDialog() {
        val logsText = SessionLogger.getLogsAsText()
        val scrollView = ScrollView(this)
        val textView = TextView(this).apply {
            text = logsText
            setPadding(32, 32, 32, 32)
            setTextIsSelectable(true)
            textSize = 9f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#1C1C1E"))
            typeface = android.graphics.Typeface.MONOSPACE
        }
        scrollView.addView(textView)
        
        AlertDialog.Builder(this, R.style.Theme_AntiF_Dialog)
            .setTitle("Session Logs (${SessionLogger.getLogsCount()} entries)")
            .setView(scrollView)
            .setPositiveButton("Save to File") { _, _ -> 
                val path = SessionLogger.saveToFile(this)
                if (path != null) {
                    Toast.makeText(this, "Saved to: $path", Toast.LENGTH_LONG).show()
                }
            }
            .setNeutralButton("Clear") { _, _ ->
                SessionLogger.clearLogs()
                Toast.makeText(this, "Logs cleared", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showClearAllDataDialog() {
        AlertDialog.Builder(this, R.style.Theme_AntiF_Dialog)
            .setTitle("⚠️ Clear ALL App Data")
            .setMessage("This will PERMANENTLY delete:\n\n" +
                    "• All profiles\n" +
                    "• All cookies & localStorage\n" +
                    "• All WebView data\n" +
                    "• All cached data\n" +
                    "• All bypass settings\n" +
                    "• Database\n\n" +
                    "App will restart after clearing.\n\n" +
                    "This action cannot be undone!")
            .setPositiveButton("Delete Everything") { _, _ ->
                clearAllAppData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearAllAppData() {
        lifecycleScope.launch {
            try {
                // 1. Delete all profiles from database
                val profiles = profileDao.getAllProfilesSync()
                profiles.forEach { profile ->
                    WebViewConfigurator.deleteProfileData(this@MainActivity, profile.id)
                    profileDao.deleteProfile(profile)
                }
                
                // 2. Clear all cookies
                withContext(Dispatchers.Main) {
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.removeAllCookies(null)
                    cookieManager.flush()
                }
                
                // 3. Clear WebStorage
                WebStorage.getInstance().deleteAllData()
                
                // 4. Clear ALL WebView data directories (FIXED!)
                withContext(Dispatchers.IO) {
                    // Clear webview_profiles directory
                    val webviewDir = File(dataDir, "webview_profiles")
                    if (webviewDir.exists()) {
                        webviewDir.deleteRecursively()
                    }
                    
                    // Clear ALL app_webview_* directories (critical fix!)
                    dataDir.listFiles()?.forEach { file ->
                        if (file.name.startsWith("app_webview") || 
                            file.name.startsWith("webview") ||
                            file.name.contains("WebView")) {
                            file.deleteRecursively()
                        }
                    }
                }
                
                // 5. Clear SharedPreferences (bypass settings)
                withContext(Dispatchers.IO) {
                    getSharedPreferences("antif_bypass_settings", MODE_PRIVATE)
                        .edit().clear().apply()
                }
                
                // 6. Clear app cache
                withContext(Dispatchers.IO) {
                    clearAppCache()
                }
                
                // 7. Delete database
                withContext(Dispatchers.IO) {
                    deleteDatabase("antif_database")
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity, 
                        "All data cleared! Restarting app...", 
                        Toast.LENGTH_LONG
                    ).show()
                    
                    // Restart app
                    val intent = packageManager.getLaunchIntentForPackage(packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finishAffinity()
                    Runtime.getRuntime().exit(0)
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity, 
                        "Error clearing data: ${e.message}", 
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun clearAppCache() {
        try {
            // Clear cache directory
            cacheDir?.deleteRecursively()
            
            // Clear code cache
            codeCacheDir?.deleteRecursively()
            
            // Clear external cache
            externalCacheDir?.deleteRecursively()
            
            // Clear files dir (except important files)
            filesDir?.listFiles()?.forEach { file ->
                if (!file.name.startsWith(".")) {
                    file.deleteRecursively()
                }
            }
            
            // Clear no_backup directory
            val noBackupDir = File(dataDir, "no_backup")
            if (noBackupDir.exists()) {
                noBackupDir.deleteRecursively()
            }
            
            // Clear shared_prefs
            val sharedPrefsDir = File(dataDir, "shared_prefs")
            if (sharedPrefsDir.exists()) {
                sharedPrefsDir.listFiles()?.forEach { file ->
                    // Keep only critical prefs if needed
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showAddOptions() {
        val options = arrayOf(
            "Create new profile", 
            "Generate random (any OS)",
            "Generate Windows profile",
            "Generate Mac profile",
            "Generate Linux profile"
        )
        AlertDialog.Builder(this, R.style.Theme_AntiF_Dialog)
            .setTitle("Add Profile")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(this, ProfileEditActivity::class.java)
                        startActivity(intent)
                    }
                    1 -> {
                        lifecycleScope.launch {
                            val profile = FingerprintGenerator.generateRandom()
                            profileDao.insertProfile(profile)
                            Toast.makeText(this@MainActivity, "Profile created: ${profile.name}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    2 -> {
                        lifecycleScope.launch {
                            val profile = FingerprintGenerator.generateForOS(FingerprintGenerator.OSType.WINDOWS)
                            profileDao.insertProfile(profile)
                            Toast.makeText(this@MainActivity, "Windows profile: ${profile.name}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    3 -> {
                        lifecycleScope.launch {
                            val profile = FingerprintGenerator.generateForOS(FingerprintGenerator.OSType.MAC)
                            profileDao.insertProfile(profile)
                            Toast.makeText(this@MainActivity, "Mac profile: ${profile.name}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    4 -> {
                        lifecycleScope.launch {
                            val profile = FingerprintGenerator.generateForOS(FingerprintGenerator.OSType.LINUX)
                            profileDao.insertProfile(profile)
                            Toast.makeText(this@MainActivity, "Linux profile: ${profile.name}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .show()
    }

    private fun launchBrowser(profile: BrowserProfile) {
        // Validate profile
        if (profile.id <= 0) {
            Toast.makeText(this, "Invalid profile", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                profileDao.updateLastUsed(profile.id, System.currentTimeMillis())
            } catch (e: Exception) {
                // Ignore update errors
            }
        }
        
        try {
            val intent = Intent(this, BrowserActivity::class.java).apply {
                putExtra("profile_id", profile.id)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to launch browser: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editProfile(profile: BrowserProfile) {
        try {
            val intent = Intent(this, ProfileEditActivity::class.java).apply {
                putExtra("profile_id", profile.id)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to open profile: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteProfile(profile: BrowserProfile) {
        AlertDialog.Builder(this, R.style.Theme_AntiF_Dialog)
            .setTitle("Delete Profile")
            .setMessage("Delete \"${profile.name}\"?\n\nThis will also delete all cookies and data for this profile.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    try {
                        // Delete profile data directory
                        WebViewConfigurator.deleteProfileData(this@MainActivity, profile.id)
                        profileDao.deleteProfile(profile)
                        Toast.makeText(this@MainActivity, "Deleted", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    inner class ProfileAdapter : RecyclerView.Adapter<ProfileAdapter.VH>() {
        private var items: List<BrowserProfile> = emptyList()

        fun submitList(list: List<BrowserProfile>) {
            items = list.toList() // Create copy to prevent race conditions
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profile, parent, false)
            return VH(view)
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            if (position < items.size) {
                holder.bind(items[position])
            }
        }

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            private val tvName: TextView = view.findViewById(R.id.tvProfileName)
            private val tvInfo: TextView = view.findViewById(R.id.tvProfileInfo)
            private val tvProxy: TextView = view.findViewById(R.id.tvProxyStatus)
            private val tvLastUsed: TextView = view.findViewById(R.id.tvLastUsed)
            private val btnMenu: ImageButton = view.findViewById(R.id.btnProfileMenu)

            fun bind(profile: BrowserProfile) {
                tvName.text = profile.name
                
                // Show OS type in info
                val osLabel = when(profile.osType) {
                    "windows" -> "Win"
                    "mac" -> "Mac"
                    "linux" -> "Linux"
                    else -> profile.platform
                }
                tvInfo.text = "$osLabel | ${profile.screenWidth}x${profile.screenHeight} | ${profile.language}"

                tvProxy.text = when (profile.proxyType) {
                    "none" -> "No proxy"
                    "http" -> "HTTP: ${profile.proxyHost}:${profile.proxyPort}"
                    "socks5" -> "SOCKS5: ${profile.proxyHost}:${profile.proxyPort}"
                    else -> "No proxy"
                }

                tvLastUsed.text = if (profile.lastUsedAt > 0) {
                    "Last: ${SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(Date(profile.lastUsedAt))}"
                } else {
                    "Never used"
                }

                // Store profile reference safely
                val safeProfile = profile.copy()
                
                itemView.setOnClickListener { launchBrowser(safeProfile) }

                btnMenu.setOnClickListener { view ->
                    PopupMenu(view.context, view).apply {
                        menu.add(0, 1, 0, "Launch")
                        menu.add(0, 2, 0, "Edit")
                        menu.add(0, 3, 0, "Duplicate")
                        menu.add(0, 5, 0, "Clear profile data")
                        menu.add(0, 4, 0, "Delete")
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                1 -> launchBrowser(safeProfile)
                                2 -> editProfile(safeProfile)
                                3 -> {
                                    lifecycleScope.launch {
                                        val copy = safeProfile.copy(
                                            id = 0,
                                            name = "${safeProfile.name} (copy)",
                                            createdAt = System.currentTimeMillis(),
                                            lastUsedAt = 0
                                        )
                                        profileDao.insertProfile(copy)
                                    }
                                }
                                4 -> deleteProfile(safeProfile)
                                5 -> {
                                    lifecycleScope.launch {
                                        WebViewConfigurator.deleteProfileData(this@MainActivity, safeProfile.id)
                                        Toast.makeText(this@MainActivity, "Profile data cleared", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            true
                        }
                        show()
                    }
                }
            }
        }
    }
}
