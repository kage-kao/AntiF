package com.antif.browser.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.antif.browser.R
import com.antif.browser.core.AdBlockEngine

class AdBlockRulesActivity : AppCompatActivity() {

    private lateinit var recyclerRules: RecyclerView
    private lateinit var btnAddBlock: Button
    private lateinit var btnAddAllow: Button
    private lateinit var btnAddWhitelist: Button
    private lateinit var tvStats: TextView
    private lateinit var etNewRule: EditText

    data class RuleItem(val rule: String, val type: String) // "block", "allow", "whitelist"
    private val rules = mutableListOf<RuleItem>()
    private val adapter = RulesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adblock_rules)

        recyclerRules = findViewById(R.id.recyclerRules)
        btnAddBlock = findViewById(R.id.btnAddBlockRule)
        btnAddAllow = findViewById(R.id.btnAddAllowRule)
        btnAddWhitelist = findViewById(R.id.btnAddWhitelist)
        tvStats = findViewById(R.id.tvAdBlockStats)
        etNewRule = findViewById(R.id.etNewRule)

        recyclerRules.layoutManager = LinearLayoutManager(this)
        recyclerRules.adapter = adapter

        updateStats()

        btnAddBlock.setOnClickListener { addRule("block") }
        btnAddAllow.setOnClickListener { addRule("allow") }
        btnAddWhitelist.setOnClickListener { addRule("whitelist") }
    }

    private fun addRule(type: String) {
        val rule = etNewRule.text.toString().trim()
        if (rule.isEmpty()) {
            etNewRule.error = "Enter a domain or URL pattern"
            return
        }

        when (type) {
            "block" -> AdBlockEngine.addBlockRule(rule)
            "allow" -> AdBlockEngine.addAllowRule(rule)
            "whitelist" -> AdBlockEngine.addWhitelistDomain(rule)
        }

        rules.add(RuleItem(rule, type))
        adapter.notifyItemInserted(rules.size - 1)
        etNewRule.text.clear()
        updateStats()

        val label = when (type) {
            "block" -> "Block"
            "allow" -> "Allow"
            "whitelist" -> "Whitelist"
            else -> type
        }
        Toast.makeText(this, "$label rule added: $rule", Toast.LENGTH_SHORT).show()
    }

    private fun removeRule(position: Int) {
        val item = rules[position]
        when (item.type) {
            "block" -> AdBlockEngine.removeBlockRule(item.rule)
            "allow" -> AdBlockEngine.removeAllowRule(item.rule)
            "whitelist" -> AdBlockEngine.removeWhitelistDomain(item.rule)
        }
        rules.removeAt(position)
        adapter.notifyItemRemoved(position)
        updateStats()
    }

    private fun updateStats() {
        val blocked = AdBlockEngine.getSessionBlockedCount()
        val domains = AdBlockEngine.getBlockedDomainsCount()
        val patterns = AdBlockEngine.getBlockedPatternsCount()
        tvStats.text = "Built-in: ${domains} domains, ${patterns} patterns | Session blocked: $blocked | Custom rules: ${rules.size}"
    }

    inner class RulesAdapter : RecyclerView.Adapter<RulesAdapter.VH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_adblock_rule, parent, false)
            return VH(view)
        }

        override fun getItemCount() = rules.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(rules[position], position)
        }

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            private val tvRule: TextView = view.findViewById(R.id.tvRuleText)
            private val tvType: TextView = view.findViewById(R.id.tvRuleType)
            private val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteRule)

            fun bind(item: RuleItem, position: Int) {
                tvRule.text = item.rule
                tvType.text = item.type.uppercase()

                val color = when (item.type) {
                    "block" -> "#FF4444"
                    "allow" -> "#00FF41"
                    "whitelist" -> "#FF6600"
                    else -> "#888888"
                }
                tvType.setTextColor(android.graphics.Color.parseColor(color))

                btnDelete.setOnClickListener {
                    AlertDialog.Builder(this@AdBlockRulesActivity, R.style.Theme_AntiF_Dialog)
                        .setTitle("Remove rule?")
                        .setMessage(item.rule)
                        .setPositiveButton("Remove") { _, _ -> removeRule(position) }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        }
    }
}
