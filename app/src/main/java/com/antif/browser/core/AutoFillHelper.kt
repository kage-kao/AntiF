package com.antif.browser.core

/**
 * Auto-fill web forms by injecting JavaScript.
 * Fills payment forms, registration forms, address forms.
 */
object AutoFillHelper {

    /**
     * Generate JS to auto-fill payment/checkout forms with card data.
     * Tries multiple common field selectors used by payment processors.
     */
    fun generateAutoFillScript(card: CardGenerator.CardData): String {
        val nameParts = card.name.split(" ", limit = 2)
        val firstName = nameParts.getOrElse(0) { "" }.replace("'", "\\'")
        val lastName = nameParts.getOrElse(1) { "" }.replace("'", "\\'")
        val fullName = card.name.replace("'", "\\'")
        val expParts = card.expiry.split("/")
        val expMonth = expParts.getOrElse(0) { "01" }
        val expYear = "20${expParts.getOrElse(1) { "28" }}"
        val expYearShort = expParts.getOrElse(1) { "28" }
        val address = card.address.replace("'", "\\'")
        val city = card.city.replace("'", "\\'")
        val state = card.state.replace("'", "\\'")
        val zip = card.zip.replace("'", "\\'")
        val phone = card.phone.replace("'", "\\'")
        val email = card.email.replace("'", "\\'")
        val cardNum = card.number

        return """
(function() {
    'use strict';

    function fillField(selectors, value) {
        for (var i = 0; i < selectors.length; i++) {
            var elements = document.querySelectorAll(selectors[i]);
            for (var j = 0; j < elements.length; j++) {
                var el = elements[j];
                if (el && (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA' || el.tagName === 'SELECT')) {
                    var nativeInputValueSetter = Object.getOwnPropertyDescriptor(
                        window.HTMLInputElement.prototype, 'value'
                    );
                    if (nativeInputValueSetter && nativeInputValueSetter.set) {
                        nativeInputValueSetter.set.call(el, value);
                    } else {
                        el.value = value;
                    }
                    el.dispatchEvent(new Event('input', { bubbles: true }));
                    el.dispatchEvent(new Event('change', { bubbles: true }));
                    el.dispatchEvent(new Event('blur', { bubbles: true }));
                }
            }
        }
    }

    function selectOption(selectors, value) {
        for (var i = 0; i < selectors.length; i++) {
            var elements = document.querySelectorAll(selectors[i]);
            for (var j = 0; j < elements.length; j++) {
                var el = elements[j];
                if (el && el.tagName === 'SELECT') {
                    for (var k = 0; k < el.options.length; k++) {
                        var optVal = el.options[k].value;
                        var optText = el.options[k].text;
                        if (optVal === value || optText === value ||
                            optVal.indexOf(value) !== -1 || optText.indexOf(value) !== -1) {
                            el.selectedIndex = k;
                            el.dispatchEvent(new Event('change', { bubbles: true }));
                            break;
                        }
                    }
                }
            }
        }
    }

    // === CARD NUMBER ===
    fillField([
        'input[name*="card" i][name*="number" i]',
        'input[name*="cardnumber" i]',
        'input[name*="cc-number" i]',
        'input[name*="ccnumber" i]',
        'input[autocomplete="cc-number"]',
        'input[data-stripe="number"]',
        'input[name="number"]',
        'input[id*="card" i][id*="number" i]',
        'input[id*="cardNumber" i]',
        'input[id*="ccnum" i]',
        'input[placeholder*="card number" i]',
        'input[placeholder*="0000 0000" i]',
        'input[aria-label*="card number" i]',
        'input[name="pan"]',
        'input[name="cardNo"]',
        '#cardNumber', '#card-number', '#cc-number'
    ], '${cardNum}');

    // === EXPIRY (combined) ===
    fillField([
        'input[name*="expir" i]',
        'input[name*="exp-date" i]',
        'input[autocomplete="cc-exp"]',
        'input[placeholder*="MM/YY" i]',
        'input[placeholder*="MM / YY" i]',
        'input[id*="expir" i]',
        'input[aria-label*="expir" i]',
        '#expiry', '#cc-exp'
    ], '${expMonth}/${expYearShort}');

    // === EXPIRY MONTH ===
    fillField([
        'input[name*="exp" i][name*="month" i]',
        'input[name*="ccmonth" i]',
        'input[autocomplete="cc-exp-month"]',
        'input[id*="exp" i][id*="month" i]',
        '#cc-exp-month', '#expMonth'
    ], '${expMonth}');
    selectOption([
        'select[name*="exp" i][name*="month" i]',
        'select[name*="ccmonth" i]',
        'select[autocomplete="cc-exp-month"]',
        'select[id*="exp" i][id*="month" i]',
        '#cc-exp-month', '#expMonth'
    ], '${expMonth}');

    // === EXPIRY YEAR ===
    fillField([
        'input[name*="exp" i][name*="year" i]',
        'input[name*="ccyear" i]',
        'input[autocomplete="cc-exp-year"]',
        'input[id*="exp" i][id*="year" i]',
        '#cc-exp-year', '#expYear'
    ], '${expYear}');
    selectOption([
        'select[name*="exp" i][name*="year" i]',
        'select[name*="ccyear" i]',
        'select[autocomplete="cc-exp-year"]',
        'select[id*="exp" i][id*="year" i]',
        '#cc-exp-year', '#expYear'
    ], '${expYear}');
    // Try short year too
    selectOption([
        'select[name*="exp" i][name*="year" i]',
        'select[autocomplete="cc-exp-year"]'
    ], '${expYearShort}');

    // === CVV ===
    fillField([
        'input[name*="cvv" i]',
        'input[name*="cvc" i]',
        'input[name*="csv" i]',
        'input[name*="security" i][name*="code" i]',
        'input[autocomplete="cc-csc"]',
        'input[id*="cvv" i]',
        'input[id*="cvc" i]',
        'input[placeholder*="CVV" i]',
        'input[placeholder*="CVC" i]',
        'input[placeholder*="Security" i]',
        'input[aria-label*="security code" i]',
        '#cvv', '#cvc', '#cc-csc'
    ], '${card.cvv}');

    // === NAME ON CARD ===
    fillField([
        'input[name*="card" i][name*="name" i]',
        'input[name*="ccname" i]',
        'input[name*="cc-name" i]',
        'input[autocomplete="cc-name"]',
        'input[id*="card" i][id*="name" i]',
        'input[placeholder*="name on card" i]',
        'input[placeholder*="cardholder" i]',
        'input[aria-label*="name on card" i]',
        '#ccName', '#cardName', '#cc-name'
    ], '${fullName}');

    // === FIRST NAME ===
    fillField([
        'input[name*="first" i][name*="name" i]',
        'input[name="firstName"]',
        'input[name="fname"]',
        'input[autocomplete="given-name"]',
        'input[id*="first" i][id*="name" i]',
        'input[placeholder*="first name" i]',
        '#firstName', '#fname'
    ], '${firstName}');

    // === LAST NAME ===
    fillField([
        'input[name*="last" i][name*="name" i]',
        'input[name="lastName"]',
        'input[name="lname"]',
        'input[autocomplete="family-name"]',
        'input[id*="last" i][id*="name" i]',
        'input[placeholder*="last name" i]',
        '#lastName', '#lname'
    ], '${lastName}');

    // === EMAIL ===
    fillField([
        'input[type="email"]',
        'input[name*="email" i]',
        'input[autocomplete="email"]',
        'input[id*="email" i]',
        'input[placeholder*="email" i]',
        '#email'
    ], '${email}');

    // === PHONE ===
    fillField([
        'input[type="tel"]',
        'input[name*="phone" i]',
        'input[name*="tel" i]',
        'input[autocomplete="tel"]',
        'input[id*="phone" i]',
        'input[placeholder*="phone" i]',
        '#phone', '#tel'
    ], '${phone}');

    // === ADDRESS ===
    fillField([
        'input[name*="address" i][name*="1" i]',
        'input[name*="street" i]',
        'input[name="address"]',
        'input[name="address1"]',
        'input[autocomplete="address-line1"]',
        'input[autocomplete="street-address"]',
        'input[id*="address" i]',
        'input[placeholder*="address" i]',
        'input[placeholder*="street" i]',
        '#address', '#address1', '#street'
    ], '${address}');

    // === CITY ===
    fillField([
        'input[name*="city" i]',
        'input[autocomplete="address-level2"]',
        'input[id*="city" i]',
        'input[placeholder*="city" i]',
        '#city'
    ], '${city}');

    // === STATE ===
    fillField([
        'input[name*="state" i]',
        'input[name*="province" i]',
        'input[name*="region" i]',
        'input[autocomplete="address-level1"]',
        '#state', '#province'
    ], '${state}');
    selectOption([
        'select[name*="state" i]',
        'select[name*="province" i]',
        'select[name*="region" i]',
        'select[autocomplete="address-level1"]',
        '#state', '#province'
    ], '${state}');

    // === ZIP ===
    fillField([
        'input[name*="zip" i]',
        'input[name*="postal" i]',
        'input[name*="postcode" i]',
        'input[autocomplete="postal-code"]',
        'input[id*="zip" i]',
        'input[id*="postal" i]',
        'input[placeholder*="zip" i]',
        'input[placeholder*="postal" i]',
        '#zip', '#postalCode', '#postcode'
    ], '${zip}');

    // === COUNTRY (default US) ===
    selectOption([
        'select[name*="country" i]',
        'select[autocomplete="country"]',
        '#country'
    ], 'US');
    selectOption([
        'select[name*="country" i]',
        'select[autocomplete="country"]',
        '#country'
    ], 'United States');

    console.log('%c[AntiF AutoFill] Form filled successfully', 'color: #00ff41; font-weight: bold;');
})();
        """.trimIndent()
    }

    /**
     * Generate script to fill ONLY card fields (no personal info)
     */
    fun generateCardOnlyScript(card: CardGenerator.CardData): String {
        val expParts = card.expiry.split("/")
        val expMonth = expParts.getOrElse(0) { "01" }
        val expYearShort = expParts.getOrElse(1) { "28" }
        val fullName = card.name.replace("'", "\\'")

        return """
(function() {
    function fill(sel, val) {
        document.querySelectorAll(sel).forEach(function(el) {
            var s = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value');
            if (s && s.set) s.set.call(el, val); else el.value = val;
            el.dispatchEvent(new Event('input', {bubbles:true}));
            el.dispatchEvent(new Event('change', {bubbles:true}));
        });
    }
    fill('input[autocomplete="cc-number"], input[name*="card" i][name*="number" i], input[data-stripe="number"], input[id*="cardNumber" i]', '${card.number}');
    fill('input[autocomplete="cc-exp"], input[name*="expir" i], input[placeholder*="MM/YY" i]', '${expMonth}/${expYearShort}');
    fill('input[autocomplete="cc-csc"], input[name*="cvv" i], input[name*="cvc" i]', '${card.cvv}');
    fill('input[autocomplete="cc-name"], input[name*="card" i][name*="name" i]', '${fullName}');
    console.log('[AntiF] Card fields filled');
})();
        """.trimIndent()
    }
}
