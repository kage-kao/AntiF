package com.antif.browser.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.antif.browser.R
import com.antif.browser.core.AutoFillHelper
import com.antif.browser.core.CardGenerator

class CardGenActivity : AppCompatActivity() {

    private lateinit var spinnerNetwork: Spinner
    private lateinit var etCount: EditText
    private lateinit var btnGenerate: Button
    private lateinit var btnAutoFill: Button
    private lateinit var recyclerResults: RecyclerView
    private lateinit var tvResultCount: TextView

    // Template views
    private lateinit var etTemplateBin: EditText
    private lateinit var etTemplateExpiry: EditText
    private lateinit var etTemplateName: EditText
    private lateinit var etTemplateZip: EditText
    private lateinit var btnApplyTemplate: Button
    private lateinit var spinnerTemplates: Spinner
    private lateinit var templateSection: LinearLayout

    private val results = mutableListOf<CardGenerator.CardData>()
    private val adapter = CardResultAdapter()

    private val networks = listOf("random", "visa", "mastercard", "amex", "discover", "jcb", "unionpay")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_gen)

        initViews()
        setupSpinners()
        setupListeners()
    }

    private fun initViews() {
        spinnerNetwork = findViewById(R.id.spinnerNetwork)
        etCount = findViewById(R.id.etCount)
        btnGenerate = findViewById(R.id.btnGenerate)
        btnAutoFill = findViewById(R.id.btnAutoFill)
        recyclerResults = findViewById(R.id.recyclerResults)
        tvResultCount = findViewById(R.id.tvResultCount)

        etTemplateBin = findViewById(R.id.etTemplateBin)
        etTemplateExpiry = findViewById(R.id.etTemplateExpiry)
        etTemplateName = findViewById(R.id.etTemplateName)
        etTemplateZip = findViewById(R.id.etTemplateZip)
        btnApplyTemplate = findViewById(R.id.btnApplyTemplate)
        spinnerTemplates = findViewById(R.id.spinnerTemplates)
        templateSection = findViewById(R.id.templateSection)

        recyclerResults.layoutManager = LinearLayoutManager(this)
        recyclerResults.adapter = adapter
    }

    private fun setupSpinners() {
        spinnerNetwork.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, networks.map { it.uppercase() })

        val templateNames = mutableListOf("Custom...") +
            CardGenerator.BUILT_IN_TEMPLATES.map { "${it.name} (${it.bin})" }
        spinnerTemplates.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, templateNames)

        spinnerTemplates.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if (pos > 0) {
                    val tmpl = CardGenerator.BUILT_IN_TEMPLATES[pos - 1]
                    etTemplateBin.setText(tmpl.bin)
                    etTemplateExpiry.setText(tmpl.expiry)
                    etTemplateName.setText(tmpl.holderName)
                    etTemplateZip.setText(tmpl.zip)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupListeners() {
        btnGenerate.setOnClickListener { generateCards() }

        btnApplyTemplate.setOnClickListener { generateFromTemplate() }

        btnAutoFill.setOnClickListener {
            if (results.isEmpty()) {
                Toast.makeText(this, "Generate cards first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Store auto-fill script in command queue for BrowserActivity
            val card = results.first()
            val script = AutoFillHelper.generateAutoFillScript(card)
            ConsoleActivity.CommandQueue.add(script)
            Toast.makeText(this, "AutoFill queued. Switch to browser.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun generateCards() {
        val count = etCount.text.toString().toIntOrNull()?.coerceIn(1, 50) ?: 10
        val network = networks[spinnerNetwork.selectedItemPosition]

        results.clear()
        for (i in 0 until count) {
            val card = CardGenerator.generateFullCard(network = network)
            results.add(card)
        }

        adapter.notifyDataSetChanged()
        tvResultCount.text = "${results.size} cards generated"
    }

    private fun generateFromTemplate() {
        val bin = etTemplateBin.text.toString().trim()
        if (bin.isBlank()) {
            etTemplateBin.error = "BIN required"
            return
        }

        val tmpl = CardGenerator.Template(
            name = "Custom",
            bin = bin,
            expiry = etTemplateExpiry.text.toString().ifBlank { CardGenerator.randomExpiry() },
            holderName = etTemplateName.text.toString().ifBlank { CardGenerator.randomName() },
            zip = etTemplateZip.text.toString().ifBlank { CardGenerator.randomZip() }
        )

        val count = etCount.text.toString().toIntOrNull()?.coerceIn(1, 50) ?: 10
        results.clear()
        for (i in 0 until count) {
            results.add(CardGenerator.generateFromTemplate(tmpl))
        }

        adapter.notifyDataSetChanged()
        tvResultCount.text = "${results.size} cards from template"
    }

    // ==================== ADAPTER ====================

    inner class CardResultAdapter : RecyclerView.Adapter<CardResultAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_result, parent, false)
            return VH(view)
        }

        override fun getItemCount() = results.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(results[position], position)
        }

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            private val tvNumber: TextView = view.findViewById(R.id.tvCardNumber)
            private val tvDetails: TextView = view.findViewById(R.id.tvCardDetails)
            private val tvNetwork: TextView = view.findViewById(R.id.tvCardNetwork)
            private val btnCopy: ImageButton = view.findViewById(R.id.btnCopyCard)
            private val btnFill: ImageButton = view.findViewById(R.id.btnFillCard)

            fun bind(card: CardGenerator.CardData, pos: Int) {
                tvNumber.text = card.number.chunked(4).joinToString(" ")
                tvDetails.text = "${card.expiry} | ${card.cvv} | ${card.name} | ${card.zip}"
                tvNetwork.text = card.network.uppercase()

                val netColor = when (card.network) {
                    "visa" -> "#1A1F71"
                    "mastercard" -> "#EB001B"
                    "amex" -> "#006FCF"
                    "discover" -> "#FF6000"
                    else -> "#00FF41"
                }
                tvNetwork.setTextColor(android.graphics.Color.parseColor(netColor))

                btnCopy.setOnClickListener {
                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("card", card.toTemplate()))
                    Toast.makeText(this@CardGenActivity, "Copied", Toast.LENGTH_SHORT).show()
                }

                btnFill.setOnClickListener {
                    val script = AutoFillHelper.generateAutoFillScript(card)
                    ConsoleActivity.CommandQueue.add(script)
                    Toast.makeText(this@CardGenActivity, "AutoFill queued for card #${pos + 1}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
