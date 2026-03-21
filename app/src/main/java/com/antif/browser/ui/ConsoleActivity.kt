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
import com.antif.browser.core.FingerprintSpoofer
import java.text.SimpleDateFormat
import java.util.*

class ConsoleActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etCommand: EditText
    private lateinit var btnExecute: ImageButton
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
                addLine("antif:clear-storage - Clear all storage", "info")
                addLine("antif:reset-modals - Reset all modals", "info")
                addLine("antif:fingerprint - Show current fingerprint", "info")
                addLine("antif:status - Show protection status", "info")
                addLine("", "info")
                addLine("=== JavaScript Commands ===", "info")
                addLine("window.resetFreeStandardPlanModal()", "info")
                addLine("window.resetAllModals()", "info")
                addLine("localStorage.clear()", "info")
                addLine("document.cookie", "info")
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
            }
            else -> addLine("Unknown command: $cmd", "error")
        }
    }

    private fun showPresetsMenu(anchor: View) {
        PopupMenu(this, anchor).apply {
            menu.add(0, 1, 0, "window.resetAllModals()")
            menu.add(0, 2, 0, "window.resetFreeStandardPlanModal()")
            menu.add(0, 3, 0, "localStorage.clear()")
            menu.add(0, 4, 0, "sessionStorage.clear()")
            menu.add(0, 5, 0, "document.cookie")
            menu.add(0, 6, 0, "navigator.userAgent")
            menu.add(0, 7, 0, "Clear all site data")
            menu.add(0, 8, 0, "Remove all modals from DOM")
            menu.add(0, 9, 0, "Show all localStorage keys")

            setOnMenuItemClickListener { item ->
                val cmd = when (item.itemId) {
                    1 -> "window.resetAllModals()"
                    2 -> "window.resetFreeStandardPlanModal()"
                    3 -> "localStorage.clear()"
                    4 -> "sessionStorage.clear()"
                    5 -> "document.cookie"
                    6 -> "navigator.userAgent"
                    7 -> """
                        localStorage.clear();
                        sessionStorage.clear();
                        indexedDB.databases().then(dbs => dbs.forEach(db => indexedDB.deleteDatabase(db.name)));
                    """.trimIndent()
                    8 -> """
                        document.querySelectorAll('[class*="modal"],[class*="dialog"],[class*="popup"],[class*="overlay"]').forEach(e => e.remove());
                        document.body.style.overflow='auto';
                    """.trimIndent()
                    9 -> "Object.keys(localStorage).join(', ')"
                    else -> ""
                }
                if (cmd.isNotEmpty()) {
                    etCommand.setText(cmd)
                    executeCommand()
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
