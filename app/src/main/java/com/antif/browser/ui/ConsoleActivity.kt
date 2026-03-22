package com.antif.browser.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.antif.browser.R
import com.antif.browser.core.EmergentBypass
import com.antif.browser.core.FingerprintSpoofer
import java.text.SimpleDateFormat
import java.util.*

class ConsoleActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etCommand: EditText
    private lateinit var btnExecute: Button
    private lateinit var btnClear: ImageButton
    private lateinit var btnPresets: ImageButton

    private val outputAdapter = ConsoleOutputAdapter()
    private val outputLines = mutableListOf<ConsoleLine>()

    data class ConsoleLine(
        val text: String,
        val type: String, // "input", "output", "error", "info"
        val timestamp: Long = System.currentTimeMillis()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_console)

        recyclerView = findViewById(R.id.recyclerConsole)
        etCommand = findViewById(R.id.etCommand)
        btnExecute = findViewById(R.id.btnExecute)
        btnClear = findViewById(R.id.btnClear)
        btnPresets = findViewById(R.id.btnPresets)

        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = outputAdapter

        // Welcome message
        addLine("[AntiF Console v1.0]", "info")
        addLine("Type JavaScript commands or use presets.", "info")

        btnExecute.setOnClickListener { executeCommand() }
        btnClear.setOnClickListener {
            outputLines.clear()
            outputAdapter.notifyDataSetChanged()
            addLine("[Console cleared]", "info")
        }

        etCommand.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_GO) {
                executeCommand()
                true
            } else false
        }

        btnPresets.setOnClickListener { showPresetsMenu(it) }
    }

    private fun executeCommand() {
        val cmd = etCommand.text.toString().trim()
        if (cmd.isEmpty()) return

        addLine("> $cmd", "input")
        etCommand.text.clear()

        // Since we don't have direct access to BrowserActivity's WebView from here,
        // we'll use a broadcast or shared reference pattern.
        // For now, show the command would be executed
        when {
            cmd.startsWith("antif:") -> handleAntifCommand(cmd.removePrefix("antif:").trim())
            else -> {
                addLine("Command queued for execution in browser.", "output")
                addLine("Switch to browser tab to see results.", "info")
                // Store command for BrowserActivity to pick up
                CommandQueue.add(cmd)
            }
        }
    }

    private fun handleAntifCommand(cmd: String) {
        when (cmd) {
            "help" -> {
                addLine("=== AntiF Commands ===", "info")
                addLine("antif:help - Show this help", "info")
                addLine("antif:status - Show protection status", "info")
                addLine("", "info")
                addLine("=== Emergent.sh Commands ===", "info")
                addLine("antif:emergent-reset - Reset ALL Emergent modals", "info")
                addLine("antif:emergent-cleanup - Full Emergent data cleanup", "info")
                addLine("antif:emergent-block - Block all tracking", "info")
                addLine("antif:emergent-show - Show Emergent data", "info")
                addLine("antif:emergent-device - Spoof Device-Id", "info")
                addLine("antif:emergent-full - FULL BYPASS (all methods)", "info")
                addLine("", "info")
                addLine("=== Advanced ===", "info")
                addLine("antif:feature-flags - Spoof Feature Flags", "info")
                addLine("antif:posthog-kill - Neutralize PostHog", "info")
                addLine("antif:api-monitor - Enable API monitoring", "info")
                addLine("antif:show-config - Show full Emergent config", "info")
                addLine("antif:supabase-spoof - Spoof Supabase ANON KEY", "info")
                addLine("", "info")
                addLine("=== Regional Discounts ===", "info")
                addLine("antif:region-turkey - Free Weekend Turkey", "info")
                addLine("antif:region-canada - Free Weekend Canada", "info")
                addLine("antif:region-india - 75% discount India", "info")
                addLine("antif:region-brazil - Pix payment Brazil", "info")
                addLine("antif:region-europe - 95% discount Europe", "info")
                addLine("", "info")
                addLine("=== Storage ===", "info")
                addLine("antif:clear-storage - Clear all storage", "info")
                addLine("antif:reset-modals - Reset all modals", "info")
            }
            "clear-storage" -> {
                CommandQueue.add(FingerprintSpoofer.generateCleanupScript())
                addLine("Storage cleanup command queued.", "output")
            }
            "reset-modals" -> {
                CommandQueue.add(FingerprintSpoofer.generateResetModalsScript())
                addLine("Modal reset command queued.", "output")
            }
            "status" -> {
                addLine("AntiF Protection: ACTIVE", "output")
                addLine("Canvas noise: ON", "output")
                addLine("WebGL spoof: ON", "output")
                addLine("WebRTC block: ON", "output")
                addLine("Audio noise: ON", "output")
                addLine("FP script block: ON", "output")
                addLine("PostHog block: ON", "output")
                addLine("FB Pixel block: ON", "output")
                addLine("TikTok block: ON", "output")
                addLine("Feature Flags: SPOOFED", "output")
            }
            // Emergent specific commands
            "emergent-reset" -> {
                CommandQueue.add(EmergentBypass.generateFullResetModalsScript())
                addLine("Emergent modal reset queued.", "output")
            }
            "emergent-cleanup" -> {
                CommandQueue.add(EmergentBypass.generateFullCleanupScript())
                addLine("Emergent full cleanup queued.", "output")
            }
            "emergent-block" -> {
                CommandQueue.add(EmergentBypass.generateTrackingBlockerScript())
                addLine("Tracking blocker enabled.", "output")
            }
            "emergent-show" -> {
                CommandQueue.add(EmergentBypass.generateShowEmergentDataScript())
                addLine("Emergent data analysis queued.", "output")
            }
            "emergent-device" -> {
                CommandQueue.add(EmergentBypass.generateDeviceIdSpoofScript())
                addLine("Device-Id spoofing enabled.", "output")
            }
            "emergent-full" -> {
                CommandQueue.add(EmergentBypass.generateFullBypassScript())
                addLine("FULL BYPASS activated! All methods applied.", "output")
            }
            // Advanced commands
            "feature-flags" -> {
                CommandQueue.add(EmergentBypass.generateFeatureFlagsSpoofScript())
                addLine("Feature Flags spoofing enabled.", "output")
            }
            "posthog-kill" -> {
                CommandQueue.add(EmergentBypass.generatePostHogNeutralizeScript())
                addLine("PostHog fully neutralized.", "output")
            }
            "api-monitor" -> {
                CommandQueue.add(EmergentBypass.generateApiMonitorScript())
                addLine("API Monitor enabled. Use antifApiLog() to view.", "output")
            }
            "show-config" -> {
                CommandQueue.add(EmergentBypass.generateShowFullConfigScript())
                addLine("Full Emergent config displayed in console.", "output")
            }
            "supabase-spoof" -> {
                CommandQueue.add(EmergentBypass.generateSupabaseSpoofScript())
                addLine("Supabase ANON KEY spoofing enabled.", "output")
            }
            // Regional commands
            "region-turkey" -> {
                CommandQueue.add(EmergentBypass.generateRegionalDiscountScript("turkey"))
                addLine("Turkey regional settings applied.", "output")
            }
            "region-canada" -> {
                CommandQueue.add(EmergentBypass.generateRegionalDiscountScript("canada"))
                addLine("Canada regional settings applied.", "output")
            }
            "region-india" -> {
                CommandQueue.add(EmergentBypass.generateRegionalDiscountScript("india"))
                addLine("India regional settings applied.", "output")
            }
            "region-brazil" -> {
                CommandQueue.add(EmergentBypass.generateRegionalDiscountScript("brazil"))
                addLine("Brazil regional settings applied.", "output")
            }
            "region-europe" -> {
                CommandQueue.add(EmergentBypass.generateRegionalDiscountScript("europe"))
                addLine("Europe regional settings applied.", "output")
            }
            else -> addLine("Unknown command: $cmd. Type antif:help for help.", "error")
        }
    }

    private fun showPresetsMenu(anchor: View) {
        PopupMenu(this, anchor).apply {
            // Basic Commands
            menu.add(0, 1, 0, "🔄 Reset ALL modals (Emergent)")
            menu.add(0, 2, 0, "🗑️ Full Emergent cleanup")
            menu.add(0, 3, 0, "🛡️ Enable tracking blocker")
            menu.add(0, 4, 0, "🔍 Show Emergent data")
            menu.add(0, 5, 0, "🆔 Spoof Device-Id")
            menu.add(0, 6, 0, "⚡ FULL BYPASS (all methods)")
            
            // NEW: Advanced
            menu.add(1, 40, 0, "--- Advanced ---")
            menu.add(1, 41, 0, "🚩 Spoof Feature Flags")
            menu.add(1, 42, 0, "📊 Neutralize PostHog")
            menu.add(1, 43, 0, "🔎 API Monitor")
            menu.add(1, 44, 0, "📋 Show Full Config")
            menu.add(1, 45, 0, "🔑 Supabase ANON KEY Spoof")
            
            // Regional discounts
            menu.add(2, 10, 0, "--- Regional Discounts ---")
            menu.add(2, 11, 0, "🇹🇷 Turkey (Free Weekend)")
            menu.add(2, 12, 0, "🇨🇦 Canada (Free Weekend)")
            menu.add(2, 13, 0, "🇮🇳 India (75% off)")
            menu.add(2, 14, 0, "🇧🇷 Brazil (Pix)")
            menu.add(2, 15, 0, "🇪🇺 Europe (95% off)")
            
            // Storage
            menu.add(3, 20, 0, "--- Storage ---")
            menu.add(3, 21, 0, "localStorage.clear()")
            menu.add(3, 22, 0, "sessionStorage.clear()")
            menu.add(3, 23, 0, "Clear IndexedDB")
            menu.add(3, 24, 0, "Show all localStorage keys")
            
            // Navigator
            menu.add(4, 30, 0, "--- Navigator ---")
            menu.add(4, 31, 0, "navigator.userAgent")
            menu.add(4, 32, 0, "document.cookie")

            setOnMenuItemClickListener { item ->
                val cmd = when (item.itemId) {
                    // Emergent specific
                    1 -> EmergentBypass.generateFullResetModalsScript()
                    2 -> EmergentBypass.generateFullCleanupScript()
                    3 -> EmergentBypass.generateTrackingBlockerScript()
                    4 -> EmergentBypass.generateShowEmergentDataScript()
                    5 -> EmergentBypass.generateDeviceIdSpoofScript()
                    6 -> EmergentBypass.generateFullBypassScript()
                    
                    // Advanced
                    41 -> EmergentBypass.generateFeatureFlagsSpoofScript()
                    42 -> EmergentBypass.generatePostHogNeutralizeScript()
                    43 -> EmergentBypass.generateApiMonitorScript()
                    44 -> EmergentBypass.generateShowFullConfigScript()
                    45 -> EmergentBypass.generateSupabaseSpoofScript()
                    
                    // Regional
                    11 -> EmergentBypass.generateRegionalDiscountScript("turkey")
                    12 -> EmergentBypass.generateRegionalDiscountScript("canada")
                    13 -> EmergentBypass.generateRegionalDiscountScript("india")
                    14 -> EmergentBypass.generateRegionalDiscountScript("brazil")
                    15 -> EmergentBypass.generateRegionalDiscountScript("europe")
                    
                    // Storage
                    21 -> "localStorage.clear()"
                    22 -> "sessionStorage.clear()"
                    23 -> """
                        indexedDB.databases().then(dbs => dbs.forEach(db => indexedDB.deleteDatabase(db.name)));
                        console.log('IndexedDB cleared');
                    """.trimIndent()
                    24 -> "Object.keys(localStorage).join('\\n')"
                    
                    // Navigator
                    31 -> "navigator.userAgent"
                    32 -> "document.cookie"
                    
                    else -> ""
                }
                if (cmd.isNotEmpty()) {
                    etCommand.setText(cmd.take(100) + if (cmd.length > 100) "..." else "")
                    addLine("> Preset: ${item.title}", "input")
                    CommandQueue.add(cmd)
                    addLine("Command queued for execution.", "output")
                }
                true
            }
            show()
        }
    }

    private fun addLine(text: String, type: String) {
        outputLines.add(ConsoleLine(text, type))
        outputAdapter.notifyItemInserted(outputLines.size - 1)
        recyclerView.scrollToPosition(outputLines.size - 1)
    }

    inner class ConsoleOutputAdapter : RecyclerView.Adapter<ConsoleOutputAdapter.VH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_console_output, parent, false)
            return VH(view)
        }

        override fun getItemCount() = outputLines.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(outputLines[position])
        }

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            private val tvLine: TextView = view.findViewById(R.id.tvConsoleLine)
            private val tvTime: TextView = view.findViewById(R.id.tvConsoleTime)

            fun bind(line: ConsoleLine) {
                tvLine.text = line.text
                tvTime.text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(line.timestamp))

                val color = when (line.type) {
                    "input" -> 0xFF00FF41.toInt()  // Green
                    "output" -> 0xFFCCCCCC.toInt() // Light gray
                    "error" -> 0xFFFF4444.toInt()  // Red
                    "info" -> 0xFF6688CC.toInt()   // Blue
                    else -> 0xFFCCCCCC.toInt()
                }
                tvLine.setTextColor(color)
            }
        }
    }

    // Simple command queue for passing commands to BrowserActivity
    object CommandQueue {
        private val queue = mutableListOf<String>()

        fun add(command: String) {
            synchronized(queue) { queue.add(command) }
        }

        fun drain(): List<String> {
            synchronized(queue) {
                val copy = queue.toList()
                queue.clear()
                return copy
            }
        }
    }
}
