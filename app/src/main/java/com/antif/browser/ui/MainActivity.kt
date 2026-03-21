package com.antif.browser.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.antif.browser.AntiFApplication
import com.antif.browser.R
import com.antif.browser.core.WebViewConfigurator
import com.antif.browser.data.BrowserProfile
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
    
    private val profileDao by lazy { AntiFApplication.instance.database.profileDao() }
    private val adapter = ProfileAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerProfiles)
        emptyView = findViewById(R.id.emptyView)
        fabAdd = findViewById(R.id.fabAddProfile)
        btnClearAllData = findViewById(R.id.btnClearAllData)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fabAdd.setOnClickListener { showAddOptions() }
        btnClearAllData.setOnClickListener { showClearAllDataDialog() }

        lifecycleScope.launch {
            profileDao.getAllProfiles().collectLatest { profiles ->
                adapter.submitList(profiles)
                emptyView.visibility = if (profiles.isEmpty()) View.VISIBLE else View.GONE
                recyclerView.visibility = if (profiles.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun showClearAllDataDialog() {
        AlertDialog.Builder(this, R.style.Theme_AntiF_Dialog)
            .setTitle("Clear ALL App Data")
            .setMessage("This will permanently delete:\n\n• All profiles\n• All cookies\n• All cached data\n• All WebView data\n• All localStorage\n\nThis action cannot be undone!")
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
                    // Delete profile-specific WebView data directories
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
                
                // 4. Clear app cache
                withContext(Dispatchers.IO) {
                    clearAppCache()
                }
                
                // 5. Clear WebView profiles directory
                withContext(Dispatchers.IO) {
                    val webviewDir = File(dataDir, "webview_profiles")
                    if (webviewDir.exists()) {
                        webviewDir.deleteRecursively()
                    }
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity, 
                        "All data cleared successfully!", 
                        Toast.LENGTH_LONG
                    ).show()
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
        lifecycleScope.launch {
            profileDao.updateLastUsed(profile.id, System.currentTimeMillis())
        }
        val intent = Intent(this, BrowserActivity::class.java).apply {
            putExtra("profile_id", profile.id)
        }
        startActivity(intent)
    }

    private fun editProfile(profile: BrowserProfile) {
        val intent = Intent(this, ProfileEditActivity::class.java).apply {
            putExtra("profile_id", profile.id)
        }
        startActivity(intent)
    }

    private fun deleteProfile(profile: BrowserProfile) {
        AlertDialog.Builder(this, R.style.Theme_AntiF_Dialog)
            .setTitle("Delete Profile")
            .setMessage("Delete \"${profile.name}\"?\n\nThis will also delete all cookies and data for this profile.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    // Delete profile data directory
                    WebViewConfigurator.deleteProfileData(this@MainActivity, profile.id)
                    profileDao.deleteProfile(profile)
                    Toast.makeText(this@MainActivity, "Deleted", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    inner class ProfileAdapter : RecyclerView.Adapter<ProfileAdapter.VH>() {
        private var items: List<BrowserProfile> = emptyList()

        fun submitList(list: List<BrowserProfile>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profile, parent, false)
            return VH(view)
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(items[position])
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

                itemView.setOnClickListener { launchBrowser(profile) }

                btnMenu.setOnClickListener { view ->
                    PopupMenu(view.context, view).apply {
                        menu.add(0, 1, 0, "Launch")
                        menu.add(0, 2, 0, "Edit")
                        menu.add(0, 3, 0, "Duplicate")
                        menu.add(0, 5, 0, "Clear profile data")
                        menu.add(0, 4, 0, "Delete")
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                1 -> launchBrowser(profile)
                                2 -> editProfile(profile)
                                3 -> {
                                    lifecycleScope.launch {
                                        val copy = profile.copy(
                                            id = 0,
                                            name = "${profile.name} (copy)",
                                            createdAt = System.currentTimeMillis(),
                                            lastUsedAt = 0
                                        )
                                        profileDao.insertProfile(copy)
                                    }
                                }
                                4 -> deleteProfile(profile)
                                5 -> {
                                    // Clear only this profile's data
                                    lifecycleScope.launch {
                                        WebViewConfigurator.deleteProfileData(this@MainActivity, profile.id)
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
