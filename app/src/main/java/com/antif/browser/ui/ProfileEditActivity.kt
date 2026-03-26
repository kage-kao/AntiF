package com.antif.browser.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.antif.browser.AntiFApplication
import com.antif.browser.R
import com.antif.browser.data.BrowserProfile
import com.antif.browser.utils.FingerprintGenerator
import com.antif.browser.utils.UserAgentList
import kotlinx.coroutines.launch

class ProfileEditActivity : AppCompatActivity() {

    private val profileDao by lazy { AntiFApplication.instance.database.profileDao() }
    private var editingProfile: BrowserProfile? = null

    // Views
    private lateinit var etName: EditText
    private lateinit var etUserAgent: EditText
    private lateinit var spinnerPlatform: Spinner
    private lateinit var spinnerLanguage: Spinner
    private lateinit var etScreenWidth: EditText
    private lateinit var etScreenHeight: EditText
    private lateinit var etCpuCores: EditText
    private lateinit var etMemory: EditText
    private lateinit var etWebglVendor: EditText
    private lateinit var etWebglRenderer: EditText
    private lateinit var spinnerTimezone: Spinner
    private lateinit var switchWebRTC: Switch
    private lateinit var switchCanvas: Switch
    private lateinit var switchAudio: Switch
    private lateinit var switchAdblock: Switch
    private lateinit var switchCosmeticFilter: Switch
    private lateinit var spinnerProxyType: Spinner
    private lateinit var etProxyHost: EditText
    private lateinit var etProxyPort: EditText
    private lateinit var etProxyUser: EditText
    private lateinit var etProxyPass: EditText
    private lateinit var etHomepage: EditText
    private lateinit var btnSave: Button
    private lateinit var btnRandomize: Button
    private lateinit var btnRandomUA: Button
    private lateinit var proxyLayout: LinearLayout

    private val platforms = listOf("Win32", "Linux x86_64", "MacIntel")
    private val languages = listOf(
        "en-US", "en-GB", "de-DE", "fr-FR", "es-ES", "it-IT",
        "pt-BR", "ja-JP", "ko-KR", "zh-CN", "ru-RU", "nl-NL"
    )
    private val timezones = listOf(
        "America/New_York", "America/Chicago", "America/Denver",
        "America/Los_Angeles", "Europe/London", "Europe/Berlin",
        "Europe/Paris", "Europe/Moscow", "Asia/Tokyo",
        "Asia/Shanghai", "Asia/Kolkata", "Australia/Sydney"
    )
    private val proxyTypes = listOf("none", "http", "socks5")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        initViews()
        setupSpinners()

        val profileId = intent.getLongExtra("profile_id", -1)
        if (profileId != -1L) {
            lifecycleScope.launch {
                editingProfile = profileDao.getProfileById(profileId)
                // Check if activity is still valid before updating UI
                if (!isFinishing && !isDestroyed) {
                    editingProfile?.let { populateFields(it) }
                }
            }
        } else {
            // New profile - set defaults
            val default = FingerprintGenerator.generateRandom("New Profile")
            populateFields(default)
        }

        btnSave.setOnClickListener { saveProfile() }
        btnRandomize.setOnClickListener { randomizeAll() }
        btnRandomUA.setOnClickListener {
            etUserAgent.setText(UserAgentList.getRandomUserAgent())
        }

        spinnerProxyType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, pos: Int, id: Long) {
                proxyLayout.visibility = if (pos == 0) android.view.View.GONE else android.view.View.VISIBLE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun initViews() {
        etName = findViewById(R.id.etProfileName)
        etUserAgent = findViewById(R.id.etUserAgent)
        spinnerPlatform = findViewById(R.id.spinnerPlatform)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        etScreenWidth = findViewById(R.id.etScreenWidth)
        etScreenHeight = findViewById(R.id.etScreenHeight)
        etCpuCores = findViewById(R.id.etCpuCores)
        etMemory = findViewById(R.id.etMemory)
        etWebglVendor = findViewById(R.id.etWebglVendor)
        etWebglRenderer = findViewById(R.id.etWebglRenderer)
        spinnerTimezone = findViewById(R.id.spinnerTimezone)
        switchWebRTC = findViewById(R.id.switchWebRTC)
        switchCanvas = findViewById(R.id.switchCanvas)
        switchAudio = findViewById(R.id.switchAudio)
        switchAdblock = findViewById(R.id.switchAdblock)
        switchCosmeticFilter = findViewById(R.id.switchCosmeticFilter)
        spinnerProxyType = findViewById(R.id.spinnerProxyType)
        etProxyHost = findViewById(R.id.etProxyHost)
        etProxyPort = findViewById(R.id.etProxyPort)
        etProxyUser = findViewById(R.id.etProxyUser)
        etProxyPass = findViewById(R.id.etProxyPass)
        etHomepage = findViewById(R.id.etHomepage)
        btnSave = findViewById(R.id.btnSave)
        btnRandomize = findViewById(R.id.btnRandomize)
        btnRandomUA = findViewById(R.id.btnRandomUA)
        proxyLayout = findViewById(R.id.proxyDetailsLayout)
    }

    private fun setupSpinners() {
        spinnerPlatform.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, platforms)
        spinnerLanguage.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
        spinnerTimezone.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, timezones)
        spinnerProxyType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, proxyTypes)
    }

    private fun populateFields(p: BrowserProfile) {
        // Safety check
        if (isFinishing || isDestroyed) return
        
        try {
            etName.setText(p.name)
            etUserAgent.setText(p.userAgent)
            spinnerPlatform.setSelection(platforms.indexOf(p.platform).coerceAtLeast(0))
            spinnerLanguage.setSelection(languages.indexOf(p.language).coerceAtLeast(0))
            etScreenWidth.setText(p.screenWidth.toString())
            etScreenHeight.setText(p.screenHeight.toString())
            etCpuCores.setText(p.hardwareConcurrency.toString())
            etMemory.setText(p.deviceMemory.toString())
            etWebglVendor.setText(p.webglVendor)
            etWebglRenderer.setText(p.webglRenderer)
            spinnerTimezone.setSelection(timezones.indexOf(p.timezone).coerceAtLeast(0))
            switchWebRTC.isChecked = p.blockWebRTC
            switchCanvas.isChecked = p.blockCanvas
            switchAudio.isChecked = p.blockAudioContext
            switchAdblock.isChecked = p.adblockEnabled
            switchCosmeticFilter.isChecked = p.cosmeticFilterEnabled
            spinnerProxyType.setSelection(proxyTypes.indexOf(p.proxyType).coerceAtLeast(0))
            etProxyHost.setText(p.proxyHost)
            etProxyPort.setText(if (p.proxyPort > 0) p.proxyPort.toString() else "")
            etProxyUser.setText(p.proxyUsername)
            etProxyPass.setText(p.proxyPassword)
            etHomepage.setText(p.homepage)
        } catch (e: Exception) {
            // Ignore UI update errors if activity is being destroyed
        }
    }

    private fun randomizeAll() {
        val name = etName.text.toString().ifBlank { "Profile" }
        val random = FingerprintGenerator.generateRandom(name)
        populateFields(random)
        Toast.makeText(this, "Randomized!", Toast.LENGTH_SHORT).show()
    }

    private fun saveProfile() {
        val name = etName.text.toString().trim()
        if (name.isEmpty()) {
            etName.error = "Name required"
            return
        }

        val lang = languages[spinnerLanguage.selectedItemPosition]
        val tz = timezones[spinnerTimezone.selectedItemPosition]
        val tzOffset = mapOf(
            "America/New_York" to 300, "America/Chicago" to 360,
            "America/Denver" to 420, "America/Los_Angeles" to 480,
            "Europe/London" to 0, "Europe/Berlin" to -60,
            "Europe/Paris" to -60, "Europe/Moscow" to -180,
            "Asia/Tokyo" to -540, "Asia/Shanghai" to -480,
            "Asia/Kolkata" to -330, "Australia/Sydney" to -600
        )

        val profile = BrowserProfile(
            id = editingProfile?.id ?: 0,
            name = name,
            userAgent = etUserAgent.text.toString(),
            platform = platforms[spinnerPlatform.selectedItemPosition],
            language = lang,
            languages = "[\"$lang\",\"${lang.split("-")[0]}\"]",
            screenWidth = etScreenWidth.text.toString().toIntOrNull() ?: 1920,
            screenHeight = etScreenHeight.text.toString().toIntOrNull() ?: 1080,
            colorDepth = 24,
            hardwareConcurrency = etCpuCores.text.toString().toIntOrNull() ?: 8,
            deviceMemory = etMemory.text.toString().toIntOrNull() ?: 8,
            maxTouchPoints = 0,
            canvasNoiseSeed = (1..255).random(),
            webglVendor = etWebglVendor.text.toString(),
            webglRenderer = etWebglRenderer.text.toString(),
            audioNoiseSeed = (1..255).random(),
            timezone = tz,
            timezoneOffset = tzOffset[tz] ?: 0,
            doNotTrack = "1",
            blockWebRTC = switchWebRTC.isChecked,
            blockCanvas = switchCanvas.isChecked,
            blockWebGL = false,
            blockAudioContext = switchAudio.isChecked,
            pluginsJson = "[]",
            proxyType = proxyTypes[spinnerProxyType.selectedItemPosition],
            adblockEnabled = switchAdblock.isChecked,
            cosmeticFilterEnabled = switchCosmeticFilter.isChecked,
            proxyHost = etProxyHost.text.toString(),
            proxyPort = etProxyPort.text.toString().toIntOrNull() ?: 0,
            proxyUsername = etProxyUser.text.toString(),
            proxyPassword = etProxyPass.text.toString(),
            createdAt = editingProfile?.createdAt ?: System.currentTimeMillis(),
            lastUsedAt = editingProfile?.lastUsedAt ?: 0,
            homepage = etHomepage.text.toString().ifBlank { "https://www.google.com" }
        )

        lifecycleScope.launch {
            try {
                profileDao.insertProfile(profile)
                if (!isFinishing) {
                    Toast.makeText(this@ProfileEditActivity, "Profile saved!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                if (!isFinishing) {
                    Toast.makeText(this@ProfileEditActivity, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
