package com.antif.browser.core

import kotlin.random.Random

/**
 * Credit card number generator using Luhn algorithm.
 * For testing purposes only. Works fully offline.
 */
object CardGenerator {

    // Common BIN prefixes by network
    private val BIN_DATABASE = mapOf(
        "visa" to listOf(
            "4154644", "4532015", "4556737", "4916338", "4024007",
            "4485983", "4716108", "4929015", "4539578", "4556142",
            "4916018", "4532919", "4024071", "4844560", "4913478"
        ),
        "mastercard" to listOf(
            "5425233", "5114496", "5191147", "5399834", "5105105",
            "5200828", "5334987", "5455012", "5513670", "5298234",
            "2221001", "2223000", "2720992", "2560001", "2345678"
        ),
        "amex" to listOf(
            "3782822", "3714496", "3787344", "3400000", "3700000",
            "3743020", "3759876", "3411111", "3728024", "3764891"
        ),
        "discover" to listOf(
            "6011111", "6011000", "6011601", "6445644", "6500321",
            "6011234", "6500000", "6445000", "6011567", "6500789"
        ),
        "jcb" to listOf(
            "3528000", "3530111", "3566002", "3537286", "3542983"
        ),
        "unionpay" to listOf(
            "6200000", "6212345", "6250941", "6282000", "6269992"
        )
    )

    // Random first/last names
    private val FIRST_NAMES = listOf(
        "James", "Robert", "John", "Michael", "David", "William", "Richard", "Joseph",
        "Thomas", "Christopher", "Mary", "Patricia", "Jennifer", "Linda", "Barbara",
        "Elizabeth", "Susan", "Jessica", "Sarah", "Karen", "Emily", "Daniel", "Matthew",
        "Anthony", "Mark", "Donald", "Steven", "Andrew", "Paul", "Joshua",
        "Raegan", "Alexis", "Morgan", "Taylor", "Jordan", "Riley", "Casey",
        "Liam", "Noah", "Oliver", "Elijah", "Lucas", "Mason", "Logan",
        "Emma", "Olivia", "Ava", "Isabella", "Sophia", "Mia", "Charlotte"
    )

    private val LAST_NAMES = listOf(
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
        "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez",
        "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin",
        "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark",
        "Cole", "Baker", "Adams", "Nelson", "Hill", "Ramirez", "Campbell",
        "Mitchell", "Roberts", "Carter", "Phillips", "Evans", "Turner", "Torres"
    )

    // US ZIP codes
    private val ZIP_CODES = listOf(
        "00501", "10001", "90210", "33101", "60601", "77001", "85001",
        "02101", "19101", "30301", "48201", "55401", "78201", "97201",
        "20001", "94102", "98101", "80201", "73301", "84101", "89101",
        "23219", "27601", "35203", "39201", "40202", "43215", "46204"
    )

    // Cities
    private val CITIES = listOf(
        "New York", "Los Angeles", "Chicago", "Houston", "Phoenix",
        "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose",
        "Austin", "Jacksonville", "Fort Worth", "Columbus", "Charlotte",
        "Indianapolis", "San Francisco", "Seattle", "Denver", "Boston"
    )

    // States
    private val STATES = listOf(
        "NY", "CA", "IL", "TX", "AZ", "PA", "FL", "OH", "NC", "IN",
        "WA", "CO", "MA", "GA", "MI", "MN", "NJ", "VA", "OR", "NV"
    )

    // Streets
    private val STREETS = listOf(
        "Main St", "Oak Ave", "Maple Dr", "Cedar Ln", "Pine St",
        "Elm St", "Washington Ave", "Park Blvd", "Lake Dr", "Hill Rd",
        "Broadway", "Market St", "Church St", "High St", "Walnut St"
    )

    // ==================== LUHN ALGORITHM ====================

    /**
     * Calculate Luhn check digit for a partial card number
     */
    private fun luhnCheckDigit(partialNumber: String): Int {
        val digits = partialNumber.map { it.digitToInt() }.reversed()
        var sum = 0
        for (i in digits.indices) {
            var d = digits[i]
            if (i % 2 == 0) { // Even indices (0-based from right, so these get doubled)
                d *= 2
                if (d > 9) d -= 9
            }
            sum += d
        }
        return (10 - (sum % 10)) % 10
    }

    /**
     * Validate a card number using Luhn algorithm
     */
    fun isValidLuhn(cardNumber: String): Boolean {
        val clean = cardNumber.replace(" ", "").replace("-", "")
        if (clean.length < 13 || clean.length > 19) return false
        if (!clean.all { it.isDigit() }) return false

        val partial = clean.dropLast(1)
        val checkDigit = clean.last().digitToInt()
        return luhnCheckDigit(partial) == checkDigit
    }

    /**
     * Generate a valid card number from a BIN prefix
     */
    fun generateCardNumber(bin: String, length: Int = 16): String {
        val cleanBin = bin.replace(" ", "").replace("-", "")
        val remaining = length - cleanBin.length - 1 // -1 for check digit

        if (remaining < 0) return cleanBin // BIN too long

        val random = Random(System.nanoTime())
        val partial = cleanBin + (1..remaining).map { random.nextInt(0, 10) }.joinToString("")
        val checkDigit = luhnCheckDigit(partial)

        return partial + checkDigit
    }

    /**
     * Generate multiple card numbers from a BIN
     */
    fun generateBatch(bin: String, count: Int, length: Int = 16): List<String> {
        return (1..count).map { generateCardNumber(bin, length) }
    }

    // ==================== RANDOM DATA GENERATION ====================

    fun randomName(): String {
        val first = FIRST_NAMES.random()
        val last = LAST_NAMES.random()
        return "$first $last"
    }

    fun randomExpiry(): String {
        val random = Random(System.nanoTime())
        val month = random.nextInt(1, 13)
        val year = random.nextInt(25, 32) // 2025-2031
        return "%02d/%02d".format(month, year)
    }

    fun randomCVV(isAmex: Boolean = false): String {
        val random = Random(System.nanoTime())
        return if (isAmex) {
            "%04d".format(random.nextInt(0, 10000))
        } else {
            "%03d".format(random.nextInt(0, 1000))
        }
    }

    fun randomZip(): String = ZIP_CODES.random()

    fun randomCity(): String = CITIES.random()

    fun randomState(): String = STATES.random()

    fun randomAddress(): String {
        val random = Random(System.nanoTime())
        val number = random.nextInt(100, 9999)
        val street = STREETS.random()
        return "$number $street"
    }

    fun randomPhone(): String {
        val random = Random(System.nanoTime())
        val area = random.nextInt(200, 999)
        val mid = random.nextInt(200, 999)
        val last = random.nextInt(1000, 9999)
        return "($area) $mid-$last"
    }

    fun randomEmail(name: String): String {
        val clean = name.lowercase().replace(" ", ".").replace("'", "")
        val random = Random(System.nanoTime())
        val num = random.nextInt(1, 999)
        val domains = listOf("gmail.com", "yahoo.com", "outlook.com", "hotmail.com", "proton.me")
        return "$clean$num@${domains.random()}"
    }

    // ==================== DETECT NETWORK ====================

    fun detectNetwork(bin: String): String {
        val clean = bin.replace(" ", "")
        if (clean.isEmpty()) return "unknown"

        return when {
            clean.startsWith("4") -> "visa"
            clean.startsWith("5") && clean.length >= 2 && clean[1].digitToInt() in 1..5 -> "mastercard"
            clean.startsWith("2") && clean.length >= 4 && clean.substring(0, 4).toIntOrNull()?.let { it in 2221..2720 } == true -> "mastercard"
            clean.startsWith("34") || clean.startsWith("37") -> "amex"
            clean.startsWith("6011") || clean.startsWith("644") || clean.startsWith("65") -> "discover"
            clean.startsWith("35") -> "jcb"
            clean.startsWith("62") -> "unionpay"
            else -> "unknown"
        }
    }

    fun getRandomBin(network: String = "random"): String {
        val net = if (network == "random") {
            listOf("visa", "mastercard", "amex", "discover").random()
        } else network

        val bins = BIN_DATABASE[net] ?: BIN_DATABASE["visa"]!!
        return bins.random()
    }

    // ==================== FULL CARD DATA ====================

    data class CardData(
        val number: String,
        val expiry: String,
        val name: String,
        val cvv: String,
        val zip: String,
        val address: String,
        val city: String,
        val state: String,
        val phone: String,
        val email: String,
        val network: String
    ) {
        fun toTemplate(): String = "$number|$expiry|$name|$zip"

        fun formatted(): String = buildString {
            append("Card: ${number.chunked(4).joinToString(" ")}\n")
            append("Exp: $expiry | CVV: $cvv\n")
            append("Name: $name\n")
            append("Addr: $address\n")
            append("City: $city, $state $zip\n")
            append("Phone: $phone\n")
            append("Email: $email\n")
            append("Network: ${network.uppercase()}")
        }
    }

    fun generateFullCard(bin: String = "", network: String = "random"): CardData {
        val useBin = bin.ifBlank { getRandomBin(network) }
        val net = detectNetwork(useBin)
        val isAmex = net == "amex"
        val length = if (isAmex) 15 else 16
        val cardNumber = generateCardNumber(useBin, length)
        val name = randomName()

        return CardData(
            number = cardNumber,
            expiry = randomExpiry(),
            name = name,
            cvv = randomCVV(isAmex),
            zip = randomZip(),
            address = randomAddress(),
            city = randomCity(),
            state = randomState(),
            phone = randomPhone(),
            email = randomEmail(name),
            network = net
        )
    }

    // ==================== TEMPLATES ====================

    data class Template(
        val name: String,
        val bin: String,
        val expiry: String,
        val holderName: String,
        val zip: String
    )

    val BUILT_IN_TEMPLATES = listOf(
        Template("Visa US Basic", "415464440", "10/28", "Raegan Cole", "00501"),
        Template("Visa US Premium", "453201512", "06/27", "James Wilson", "10001"),
        Template("Mastercard US", "542523334", "03/29", "Emily Davis", "90210"),
        Template("Amex Gold", "378282246", "12/26", "Robert Johnson", "33101"),
        Template("Discover", "601111111", "09/28", "Sarah Martinez", "60601"),
        Template("Visa UK", "455664932", "01/27", "Thomas Brown", "SW1A 1AA"),
        Template("Mastercard EU", "539983422", "11/28", "Marie Dupont", "75001"),
        Template("Visa CA", "491347812", "05/29", "Michael Lee", "M5V 2T6")
    )

    /**
     * Parse template string format: "BIN|MM/YY|Name|ZIP"
     */
    fun parseTemplate(template: String): Template? {
        val parts = template.split("|").map { it.trim() }
        if (parts.size < 4) return null
        return Template(
            name = "Custom",
            bin = parts[0],
            expiry = parts[1],
            holderName = parts[2],
            zip = parts[3]
        )
    }

    fun generateFromTemplate(template: Template): CardData {
        val net = detectNetwork(template.bin)
        val isAmex = net == "amex"
        val length = if (isAmex) 15 else 16
        val cardNumber = generateCardNumber(template.bin, length)

        return CardData(
            number = cardNumber,
            expiry = template.expiry,
            name = template.holderName,
            cvv = randomCVV(isAmex),
            zip = template.zip,
            address = randomAddress(),
            city = randomCity(),
            state = randomState(),
            phone = randomPhone(),
            email = randomEmail(template.holderName),
            network = net
        )
    }
}
