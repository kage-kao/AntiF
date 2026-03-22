package com.antif.browser.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.antif.browser.R
import com.antif.browser.core.WebViewConfigurator

class CookieManagerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etUrl: EditText
    private lateinit var btnLoad: Button
    private lateinit var btnClearAll: Button
    private lateinit var btnAddCookie: Button
    private lateinit var tvInfo: TextView

    private val cookieAdapter = CookieAdapter()
    private val cookies = mutableListOf<CookieItem>()
    private var currentUrl = ""

    data class CookieItem(val name: String, val value: String, val raw: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cookie_manager)

        recyclerView = findViewById(R.id.recyclerCookies)
        etUrl = findViewById(R.id.etCookieUrl)
        btnLoad = findViewById(R.id.btnLoadCookies)
        btnClearAll = findViewById(R.id.btnClearAllCookies)
        btnAddCookie = findViewById(R.id.btnAddCookie)
        tvInfo = findViewById(R.id.tvCookieInfo)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = cookieAdapter

        currentUrl = intent.getStringExtra("url") ?: ""
        if (currentUrl.isNotBlank()) {
            etUrl.setText(currentUrl)
            loadCookies(currentUrl)
        }

        btnLoad.setOnClickListener {
            val url = etUrl.text.toString().trim()
            if (url.isNotBlank()) loadCookies(url)
        }

        btnClearAll.setOnClickListener {
            WebViewConfigurator.clearCookies()
            cookies.clear()
            cookieAdapter.notifyDataSetChanged()
            tvInfo.text = "All cookies cleared"
        }

        btnAddCookie.setOnClickListener { showAddCookieDialog() }
    }

    private fun loadCookies(url: String) {
        currentUrl = url
        val raw = WebViewConfigurator.getCookiesForUrl(url)
        cookies.clear()

        if (raw.isNotBlank()) {
            raw.split(";").forEach { part ->
                val trimmed = part.trim()
                val eqIdx = trimmed.indexOf("=")
                if (eqIdx > 0) {
                    val name = trimmed.substring(0, eqIdx).trim()
                    val value = trimmed.substring(eqIdx + 1).trim()
                    cookies.add(CookieItem(name, value, trimmed))
                }
            }
        }

        cookieAdapter.notifyDataSetChanged()
        tvInfo.text = "${cookies.size} cookies for ${url}"
    }

    private fun showAddCookieDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }
        val etName = EditText(this).apply { hint = "Cookie name" }
        val etValue = EditText(this).apply { hint = "Cookie value" }
        layout.addView(etName)
        layout.addView(etValue)

        android.app.AlertDialog.Builder(this)
            .setTitle("Add Cookie")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString()
                val value = etValue.text.toString()
                if (name.isNotBlank() && currentUrl.isNotBlank()) {
                    WebViewConfigurator.setCookieForUrl(currentUrl, "$name=$value")
                    loadCookies(currentUrl)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    inner class CookieAdapter : RecyclerView.Adapter<CookieAdapter.VH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cookie, parent, false)
            return VH(view)
        }

        override fun getItemCount() = cookies.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(cookies[position])
        }

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            private val tvName: TextView = view.findViewById(R.id.tvCookieName)
            private val tvValue: TextView = view.findViewById(R.id.tvCookieValue)
            private val btnCopy: ImageButton = view.findViewById(R.id.btnCopyCookie)

            fun bind(cookie: CookieItem) {
                tvName.text = cookie.name
                tvValue.text = cookie.value

                btnCopy.setOnClickListener {
                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("cookie", "${cookie.name}=${cookie.value}"))
                    Toast.makeText(this@CookieManagerActivity, "Copied!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
