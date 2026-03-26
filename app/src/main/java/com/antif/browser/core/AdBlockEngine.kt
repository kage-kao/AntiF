package com.antif.browser.core

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap

object AdBlockEngine {

    // Stats
    var totalBlocked: Long = 0
        private set

    private val sessionBlocked = ConcurrentHashMap<String, Int>()

    // Main ad/tracking domain blocklist (based on EasyList, EasyPrivacy, uBlock filters)
    private val blockedDomains = hashSetOf(
        // === MAJOR AD NETWORKS ===
        "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "google-analytics.com", "googletagmanager.com", "googletagservices.com",
        "pagead2.googlesyndication.com", "adservice.google.com",
        "facebook.net", "facebook.com/tr", "connect.facebook.net",
        "analytics.twitter.com", "ads-twitter.com", "static.ads-twitter.com",
        "ads.yahoo.com", "analytics.yahoo.com",
        "ads.linkedin.com", "snap.licdn.com",
        "bat.bing.com", "ads.microsoft.com",

        // === AD EXCHANGES ===
        "adnxs.com", "adsrvr.org", "adtech.de", "advertising.com",
        "bidswitch.net", "casalemedia.com", "contextweb.com",
        "criteo.com", "criteo.net", "demdex.net",
        "dotomi.com", "exelator.com", "eyeota.net",
        "flashtalking.com", "indexww.com", "lijit.com",
        "mathtag.com", "media.net", "mediamath.com",
        "moatads.com", "mookie1.com", "myvisualiq.net",
        "openx.net", "outbrain.com", "pippio.com",
        "pubmatic.com", "quantserve.com", "rfihub.com",
        "richrelevance.com", "rlcdn.com", "rubiconproject.com",
        "sascdn.com", "scdn.cxense.com", "serving-sys.com",
        "sharethis.com", "sharethrough.com", "simpli.fi",
        "sitescout.com", "smartadserver.com", "spotxchange.com",
        "taboola.com", "tapad.com", "teads.tv",
        "tidaltv.com", "tremorhub.com", "tribalfusion.com",
        "turn.com", "undertone.com", "yieldmo.com",

        // === TRACKING & ANALYTICS ===
        "hotjar.com", "fullstory.com", "mouseflow.com",
        "crazyegg.com", "luckyorange.com", "inspectlet.com",
        "mixpanel.com", "amplitude.com", "segment.io", "segment.com",
        "heapanalytics.com", "keen.io", "kissmetrics.com",
        "optimizely.com", "abtasty.com", "vwo.com",
        "newrelic.com", "nr-data.net",
        "sentry.io", "bugsnag.com",
        "chartbeat.com", "parsely.com",
        "comscore.com", "scorecardresearch.com",
        "omtrdc.net", "2o7.net", "omniture.com",
        "tealiumiq.com", "tiqcdn.com",
        "krxd.net", "bluekai.com",
        "bounceexchange.com", "bouncex.net",
        "ensighten.com", "everesttech.net",
        "narrative.io", "onetrust.com",

        // === FINGERPRINTING ===
        "fpjs.io", "fpnpmcdn.net", "fpcdn.io",
        "api.fpjs.io", "cdn.fpjs.io",
        "fingerprintjs.com", "botd.fpjs.io",
        "deviceandbrowserinfo.com",
        "creativecdn.com", "iovation.com",
        "threatmetrix.com", "socure.com",
        
        // === POSTHOG (Emergent Analytics) ===
        "us.i.posthog.com", "us-assets.i.posthog.com",
        "app.posthog.com", "eu.posthog.com", "eu.i.posthog.com",
        
        // === EMERGENT SPECIFIC - ONLY FINGERPRINT ENDPOINT ===
        "app.emergent.sh/yYu6hLHLpQEW2ELz",
        // REMOVED: ap.emergent.sh - needed for site to work!
        // REMOVED: api.emergent.sh - breaks everything!
        
        // === REWARDFUL (Affiliate Tracking) ===
        "r.wdfl.co", "rewardful.com",

        // === SOCIAL TRACKERS ===
        "platform.twitter.com/widgets",
        "platform.instagram.com",
        "connect.facebook.net",
        "static.parastorage.com",
        "snap.licdn.com",
        "px.ads.linkedin.com",

        // === POPUP / NOTIFICATION ===
        "onesignal.com", "pushwoosh.com", "pushnami.com",
        "pushcrew.com", "wonderpush.com", "webpushr.com",
        "subscribers.com", "pushly.com",
        "popads.net", "popcash.net", "popunder.net",

        // === MALWARE / SUSPICIOUS ===
        "adf.ly", "sh.st", "bc.vc",
        "coinhive.com", "coinhive.min.js",
        "crypto-loot.com", "cryptoloot.pro",
        "jsecoin.com", "authedmine.com",
        "2mdn.net",

        // === VIDEO ADS ===
        "imasdk.googleapis.com", "s0.2mdn.net",
        "vid.springserve.com", "videoplayerhub.com",
        "innovid.com", "extreme-dm.com",
        "eyeviewads.com", "videoadex.com",

        // === KNOWN AD SERVERS ===
        "ad.doubleclick.net", "ad.atdmt.com",
        "ad.turn.com", "ad.yieldmanager.com",
        "ads.adsonar.com", "ads.adzerk.net",
        "ads.bluelithium.com", "ads.brave.com",
        "ads.creative-serving.com", "ads.gumgum.com",
        "ads.mp.mydas.mobi", "ads.mopub.com",
        "ads.nexage.com", "ads.pandora.com",
        "ads.servenobid.com", "ads.stickyadstv.com",
        "ads.supplyframe.com", "ads.undertone.com",
        "adserver.yahoo.com", "adserver.yadro.ru",

        // === CONSENT / COOKIE WALLS ===
        "cdn.privacy-mgmt.com", "consent.cookiebot.com",
        "consent.cookiefirst.com", "cdn.cookielaw.org",
        "consent-pref.trustarc.com",

        // === ANTI-ADBLOCK DETECTION ===
        "blockadblock.com", "fuckadblock.com",
        "pagefair.com", "pagefair.net",
        "admiral.com", "getadmiral.com",
        "adrecover.com",

        // === ADDITIONAL TRACKERS ===
        "adsymptotic.com", "agkn.com", "atdmt.com",
        "bkrtx.com", "blismedia.com", "bttrack.com",
        "chartboost.com", "clicktale.net", "cnzz.com",
        "coremetrics.com", "crwdcntrl.net", "d5nxst8fruw4z.cloudfront.net",
        "dable.io", "daum.net/analytics", "demandbase.com",
        "dmtry.com", "doubleverify.com", "dpm.demdex.net",
        "effectivemeasure.net", "eloqua.com",
        "eyereturn.com", "fastclick.net",
        "fwmrm.net", "gemius.pl",
        "grapeshot.co.uk", "gssprt.jp",
        "hubspot.com/analytics", "igodigital.com",
        "imrworldwide.com", "intellitxt.com",
        "ipredictive.com", "jivox.com",
        "kontera.com", "legolas-media.com",
        "liadm.com", "lockerdome.com",
        "marchex.io", "marketo.net",
        "mxpnl.com", "nativo.com",
        "nexac.com", "nuggad.net",
        "owneriq.net", "pardot.com",
        "perfectaudience.com", "petametrics.com",
        "piwik.pro", "plista.com",
        "postrelease.com", "pro-market.net",
        "proxistore.com", "pubmine.com",
        "revcontent.com", "revjet.com",
        "rocketfuel.com", "sail-horizon.com",
        "samba.tv", "sekindo.com",
        "shareasale.com", "skimresources.com",
        "sonobi.com", "statcounter.com",
        "steelhousemedia.com", "steepto.com",
        "swrve.com", "taggstar.com",
        "technorati.com", "theadex.com",
        "tidaltv.com", "tradedoubler.com",
        "trafficjunky.net", "tru.am",
        "trustpilot.com/tp", "typekit.net/p.css",
        "undertone.com", "usabilla.com",
        "valueclick.com", "vidible.tv",
        "viglink.com", "visualdna.com",
        "vizu.com", "w55c.net",
        "weborama.com", "webtrekk.net",
        "wistia.com/assets/external",
        "yieldbot.com", "yimg.com/cv",
        "yldbt.com", "zemanta.com",
        "zergnet.com", "zestadz.com"
    )

    // URL patterns to block (simplified EasyList format)
    private val blockedPatterns = listOf(
        "/ad.", "/ads/", "/ads?", "/adv/", "/advert",
        "/banner/", "/banners/", "/popup/", "/popunder/",
        "/tracking/", "/tracker/", "/track?", "/track.",
        "/analytics/", "/analytics?", "/pixel/", "/pixel.",
        "/beacon/", "/beacon?", "/telemetry/",
        "prebid", "outstream", "instream",
        "/pagead/", "/afs/ads", "/adsense/",
        "amazon-adsystem.com", "aax.amazon",
        "_ad_", "-ad-", ".ad.", "=ad&",
        "/sponsor/", "/sponsored/",
        "click.php", "click.cgi", "click?",
        "/impression", "/imp?", "/imp.",
        "adsbygoogle", "googlesyndication",
        "/adserv", "adserver", "adserving",
        "doubleclick", "2mdn.net",
        "servedby.", "deliveryengine",
        "/widgets/ad", "native-ad",
        "outbrain.com/widget", "taboola.com/libtrc",
        "mgid.com", "content-recommendation",
    )

    // Resource types to block
    private val blockedResourceTypes = setOf(
        "sub_frame" // block third-party iframes that are typically ads
    )

    // Whitelist
    private val whitelist = hashSetOf<String>()

    // Custom user rules
    private val customBlockRules = mutableListOf<String>()
    private val customAllowRules = mutableListOf<String>()

    fun shouldBlock(request: WebResourceRequest): Boolean {
        val url = request.url?.toString()?.lowercase() ?: return false
        val host = request.url?.host?.lowercase() ?: return false

        // Check whitelist first
        if (whitelist.any { host.contains(it) }) return false
        if (customAllowRules.any { url.contains(it) }) return false

        // Check custom block rules
        if (customBlockRules.any { url.contains(it) }) {
            recordBlock(host)
            return true
        }

        // Check domain blocklist
        if (isDomainBlocked(host)) {
            recordBlock(host)
            return true
        }

        // Check URL patterns
        if (blockedPatterns.any { url.contains(it) }) {
            // Don't block first-party navigation
            if (request.isForMainFrame) return false
            recordBlock(host)
            return true
        }

        return false
    }

    private fun isDomainBlocked(host: String): Boolean {
        // Direct match
        if (blockedDomains.contains(host)) return true

        // Check if any parent domain is blocked
        // e.g., "sub.ads.example.com" matches "ads.example.com"
        var domain = host
        while (domain.contains('.')) {
            if (blockedDomains.contains(domain)) return true
            domain = domain.substringAfter('.')
        }

        return false
    }

    fun getBlockedResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            ByteArrayInputStream(ByteArray(0))
        )
    }

    private fun recordBlock(host: String) {
        totalBlocked++
        sessionBlocked[host] = (sessionBlocked[host] ?: 0) + 1
    }

    fun getSessionStats(): Map<String, Int> {
        return sessionBlocked.toMap()
    }

    fun getSessionBlockedCount(): Int {
        return sessionBlocked.values.sum()
    }

    fun resetSessionStats() {
        sessionBlocked.clear()
    }

    // Custom rules management
    fun addBlockRule(rule: String) {
        customBlockRules.add(rule.lowercase())
    }

    fun addAllowRule(rule: String) {
        customAllowRules.add(rule.lowercase())
    }

    fun removeBlockRule(rule: String) {
        customBlockRules.remove(rule.lowercase())
    }

    fun removeAllowRule(rule: String) {
        customAllowRules.remove(rule.lowercase())
    }

    fun addWhitelistDomain(domain: String) {
        whitelist.add(domain.lowercase())
    }

    fun removeWhitelistDomain(domain: String) {
        whitelist.remove(domain.lowercase())
    }

    fun getBlockedDomainsCount(): Int = blockedDomains.size
    fun getBlockedPatternsCount(): Int = blockedPatterns.size
}
