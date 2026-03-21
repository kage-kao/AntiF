package com.antif.browser.core

/**
 * Cosmetic filtering — hides ad elements from the DOM via CSS injection.
 * Similar to what uBlock Origin and AdBlocker Ultimate do with ##selectors.
 */
object CosmeticFilter {

    /**
     * Generate CSS injection script to hide common ad containers.
     * This is injected after page load to remove visual ad elements.
     */
    fun generateHideAdsScript(): String {
        return """
(function() {
    'use strict';

    var style = document.createElement('style');
    style.id = 'antif-cosmetic-filter';
    style.textContent = `
        /* === GOOGLE ADS === */
        .adsbygoogle,
        ins.adsbygoogle,
        #google_ads_frame1,
        #google_ads_frame2,
        #google_ads_frame3,
        [id^="google_ads_"],
        [id^="div-gpt-ad"],
        [data-google-query-id],
        .GoogleActiveViewElement,

        /* === GENERIC AD CONTAINERS === */
        [class*="ad-container"],
        [class*="ad-wrapper"],
        [class*="ad-banner"],
        [class*="ad-slot"],
        [class*="ad-unit"],
        [class*="ad-block"],
        [class*="ad-placement"],
        [class*="advert-"],
        [class*="advertisement"],
        [class*="ad_wrapper"],
        [class*="ad_container"],
        [class*="ad_banner"],
        [class*="ad_slot"],
        [id*="ad-container"],
        [id*="ad-wrapper"],
        [id*="ad-banner"],
        [id*="ad_container"],
        [id*="ad_wrapper"],
        [id*="ad_banner"],
        [id*="advertisement"],

        /* === STICKY/OVERLAY ADS === */
        [class*="sticky-ad"],
        [class*="sticky_ad"],
        [class*="fixed-ad"],
        [class*="overlay-ad"],
        [class*="interstitial"],
        [class*="popup-ad"],

        /* === SIDEBAR ADS === */
        [class*="sidebar-ad"],
        [class*="sidebar_ad"],
        [class*="widget-ad"],
        [class*="widget_ad"],

        /* === SPONSORED CONTENT === */
        [class*="sponsored"],
        [class*="promoted-"],
        [class*="native-ad"],
        [class*="content-ad"],
        [class*="recommendation-widget"],

        /* === SOCIAL SHARE TRACKING PIXELS === */
        img[width="1"][height="1"],
        img[width="0"][height="0"],
        img[src*="pixel"],
        img[src*="beacon"],
        img[src*="tracking"],

        /* === COMMON AD NETWORKS === */
        [data-ad],
        [data-ad-slot],
        [data-ad-unit],
        [data-adunit],
        [data-dfp],
        [data-adzone],
        iframe[src*="doubleclick"],
        iframe[src*="googlesyndication"],
        iframe[src*="amazon-adsystem"],

        /* === TABOOLA / OUTBRAIN / MGID === */
        .trc_rbox,
        .trc_related_container,
        #taboola-below-article,
        #taboola-right-rail,
        [id^="taboola-"],
        .OUTBRAIN,
        [data-widget-id*="outbrain"],
        [id^="outbrain_"],
        .mgbox,
        [id^="mgid"],

        /* === COOKIE / CONSENT BANNERS === */
        [class*="cookie-banner"],
        [class*="cookie-consent"],
        [class*="cookie-notice"],
        [class*="cookie-popup"],
        [class*="consent-banner"],
        [class*="gdpr-banner"],
        [class*="privacy-banner"],
        [id*="cookie-banner"],
        [id*="cookie-consent"],
        [id*="cookie-notice"],
        [id*="gdpr"],
        #CybotCookiebotDialog,
        #onetrust-banner-sdk,
        .cc-banner,
        .cc-window,

        /* === NEWSLETTER POPUPS === */
        [class*="newsletter-popup"],
        [class*="newsletter-modal"],
        [class*="subscribe-popup"],
        [class*="email-popup"],
        [class*="signup-popup"],

        /* === NOTIFICATION PROMPTS === */
        [class*="push-notification"],
        [class*="notification-prompt"],
        [class*="web-push"],

        /* === ANTI-ADBLOCK WALLS === */
        [class*="adblock-notice"],
        [class*="adblock-wall"],
        [class*="adblock-overlay"],
        [class*="anti-adblock"],
        [class*="adb-overlay"],
        [id*="adblock-notice"],
        [id*="anti-adblock"]
    {
        display: none !important;
        visibility: hidden !important;
        height: 0 !important;
        max-height: 0 !important;
        overflow: hidden !important;
        opacity: 0 !important;
        pointer-events: none !important;
        position: absolute !important;
        z-index: -9999 !important;
    }
    `;
    document.head.appendChild(style);

    // === MUTATION OBSERVER ===
    // Watch for dynamically inserted ad elements
    var observer = new MutationObserver(function(mutations) {
        mutations.forEach(function(mutation) {
            mutation.addedNodes.forEach(function(node) {
                if (node.nodeType !== 1) return;
                var el = node;

                // Check class/id for ad patterns
                var classAndId = ((el.className || '') + ' ' + (el.id || '')).toLowerCase();
                var adPatterns = ['adsbygoogle', 'ad-container', 'ad-wrapper', 'ad-banner',
                    'ad-slot', 'advertisement', 'sponsored', 'taboola', 'outbrain',
                    'cookie-banner', 'cookie-consent', 'gdpr', 'anti-adblock',
                    'interstitial', 'popup-ad', 'newsletter-popup'];

                for (var i = 0; i < adPatterns.length; i++) {
                    if (classAndId.indexOf(adPatterns[i]) !== -1) {
                        el.style.display = 'none';
                        el.style.visibility = 'hidden';
                        return;
                    }
                }

                // Check iframes
                if (el.tagName === 'IFRAME') {
                    var src = (el.src || '').toLowerCase();
                    if (src.indexOf('doubleclick') !== -1 ||
                        src.indexOf('googlesyndication') !== -1 ||
                        src.indexOf('amazon-adsystem') !== -1 ||
                        src.indexOf('facebook.com/plugins') !== -1) {
                        el.style.display = 'none';
                        return;
                    }
                }
            });
        });
    });

    observer.observe(document.body || document.documentElement, {
        childList: true,
        subtree: true
    });

    // === ANTI-ADBLOCK BYPASS ===
    // Override common adblock detection methods
    try {
        // Fake ad element for detection scripts
        var fakeAd = document.createElement('div');
        fakeAd.className = 'adsbox ad-placement';
        fakeAd.style.cssText = 'position:absolute;left:-9999px;width:1px;height:1px;';
        fakeAd.innerHTML = '&nbsp;';
        document.body.appendChild(fakeAd);
    } catch(e) {}

    // Override common adblock detection variables
    try {
        window.canRunAds = true;
        window.isAdBlockActive = false;
        window.adBlockDetected = false;
        window.adblockEnabled = false;
    } catch(e) {}

    // Restore body overflow (anti-adblock walls often lock scrolling)
    setTimeout(function() {
        if (document.body) {
            document.body.style.overflow = '';
            document.documentElement.style.overflow = '';
        }
    }, 2000);

    console.log('%c[AntiF AdBlock] Cosmetic filters active', 'color: #ff6600; font-weight: bold;');
})();
        """.trimIndent()
    }

    /**
     * Generate script to remove specific elements by CSS selector.
     */
    fun generateRemoveScript(selectors: List<String>): String {
        val selectorStr = selectors.joinToString(", ")
        return """
(function() {
    var elements = document.querySelectorAll('$selectorStr');
    elements.forEach(function(el) { el.remove(); });
    console.log('[AntiF] Removed ' + elements.length + ' elements');
})();
        """.trimIndent()
    }
}
