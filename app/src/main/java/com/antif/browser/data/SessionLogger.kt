package com.antif.browser.data

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Global Session Logger - stores logs in memory, can export to file
 */
object SessionLogger {
    
    data class LogEntry(
        val timestamp: Long,
        val type: String,  // REQUEST, RESPONSE, CONSOLE, EVENT, INJECT, BLOCK, ERROR, SETTING, MANUAL, SYSTEM
        val url: String,
        val details: String
    )
    
    private val logs = CopyOnWriteArrayList<LogEntry>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private var profileName: String = "Unknown"
    
    fun setProfileName(name: String) {
        profileName = name
    }
    
    fun log(type: String, url: String, details: String) {
        // Safe check - BypassSettings might not be initialized yet
        val loggingEnabled = try { BypassSettings.fullLoggingEnabled } catch (e: Exception) { false }
        if (loggingEnabled) {
            logs.add(LogEntry(System.currentTimeMillis(), type, url, details))
            android.util.Log.d("AntiF-LOG", "[$type] $url - $details")
        }
    }
    
    fun getLogsCount(): Int = logs.size
    
    fun getLogs(): List<LogEntry> = logs.toList()
    
    fun clearLogs() {
        logs.clear()
    }
    
    fun getLogsAsText(): String {
        val sb = StringBuilder()
        sb.append("═══════════════════════════════════════════════════════════════\n")
        sb.append("  ANTIF BROWSER - FULL SESSION LOG\n")
        sb.append("  Generated: ${dateFormat.format(Date())}\n")
        sb.append("  Profile: $profileName\n")
        sb.append("  Total entries: ${logs.size}\n")
        sb.append("═══════════════════════════════════════════════════════════════\n\n")
        
        sb.append("=== BYPASS SETTINGS ===\n")
        sb.append("Full Bypass: ${BypassSettings.fullBypassEnabled}\n")
        sb.append("Block Tracking: ${BypassSettings.blockTrackingEnabled}\n")
        sb.append("Spoof Feature Flags: ${BypassSettings.spoofFeatureFlagsEnabled}\n")
        sb.append("New Device-Id: ${BypassSettings.newDeviceIdEnabled}\n")
        sb.append("Kill PostHog: ${BypassSettings.killPostHogEnabled}\n")
        sb.append("Turkey Region: ${BypassSettings.turkeyRegionEnabled}\n")
        sb.append("Canada Region: ${BypassSettings.canadaRegionEnabled}\n")
        sb.append("Apply to ALL sites: ${BypassSettings.applyToAllSitesEnabled}\n")
        sb.append("Full Logging: ${BypassSettings.fullLoggingEnabled}\n\n")
        
        sb.append("=== SESSION LOGS ===\n\n")
        
        logs.forEach { entry ->
            val time = dateFormat.format(Date(entry.timestamp))
            sb.append("[$time] [${entry.type}]\n")
            sb.append("URL: ${entry.url}\n")
            sb.append("Details: ${entry.details}\n")
            sb.append("---\n")
        }
        
        sb.append("\n═══════════════════════════════════════════════════════════════\n")
        sb.append("  END OF LOG - ${logs.size} entries\n")
        sb.append("═══════════════════════════════════════════════════════════════\n")
        
        return sb.toString()
    }
    
    fun saveToFile(context: Context): String? {
        return try {
            val fileName = "antif_log_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.txt"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            
            FileWriter(file).use { writer ->
                writer.write(getLogsAsText())
            }
            
            log("SYSTEM", "file://$fileName", "Logs saved successfully (${logs.size} entries)")
            "Downloads/$fileName"
        } catch (e: Exception) {
            // Fallback to app internal storage
            try {
                val fileName = "antif_log_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.txt"
                val file = File(context.filesDir, fileName)
                
                FileWriter(file).use { writer ->
                    writer.write(getLogsAsText())
                }
                
                file.absolutePath
            } catch (e2: Exception) {
                null
            }
        }
    }
}
