package com.antif.browser.core

/**
 * EmergentBypass - BYPASS для Emergent.sh
 * v6.0 - FIXED: fp_dedup & supabase_user_exists detection
 *   - Early injection (onPageStarted) to hook localStorage/fetch BEFORE page JS runs
 *   - Proactive removal of fp_dedup, pending_fingerprint_*, trial keys
 *   - localStorage.getItem interceptor to mask supabase auth tokens
 *   - Improved storage blocking patterns
 */
object EmergentBypass {

    // Stored device/visitor IDs for consistency across page loads
    private var cachedDeviceId: String? = null
    private var cachedVisitorId: String? = null

    fun getOrCreateDeviceId(): String {
        if (cachedDeviceId == null) {
            cachedDeviceId = java.util.UUID.randomUUID().toString()
        }
        return cachedDeviceId!!
    }

    fun getOrCreateVisitorId(): String {
        if (cachedVisitorId == null) {
            cachedVisitorId = "v_" + java.util.UUID.randomUUID().toString().replace("-", "").take(20)
        }
        return cachedVisitorId!!
    }

    fun resetIds() {
        cachedDeviceId = null
        cachedVisitorId = null
    }

    /**
     * EARLY BYPASS SCRIPT - инжектится в onPageStarted
     * Устанавливает хуки ПЕРЕД тем как JS на странице начнёт работать.
     * Это критически важно - без этого fp_dedup записывается до того как хуки встанут.
     */
    fun generateEarlyBypassScript(): String {
        val deviceId = getOrCreateDeviceId()
        val visitorId = getOrCreateVisitorId()

        return """
(function() {
    'use strict';
    if (window._antifEarlyApplied) return;
    window._antifEarlyApplied = true;

    // ═══════════════════════════════════════════════════════════════
    // PHASE 0: PROACTIVE CLEANUP - удаляем ВСЕ trial-маркеры ДО того
    //          как страница их прочитает
    // ═══════════════════════════════════════════════════════════════
    var trialKeys = [];
    for (var i = 0; i < localStorage.length; i++) {
        var k = localStorage.key(i);
        if (!k) continue;
        var kl = k.toLowerCase();
        if (kl === 'fp_dedup' ||
            kl.indexOf('pending_fingerprint') !== -1 ||
            kl.indexOf('first_login') !== -1 ||
            kl.indexOf('first_job') !== -1 ||
            kl.indexOf('hascreatedfirstproject') !== -1 ||
            kl.indexOf('fpjs') !== -1 ||
            kl.indexOf('fingerprint') !== -1 ||
            kl.indexOf('fp_') === 0) {
            trialKeys.push(k);
        }
    }
    trialKeys.forEach(function(k) { localStorage.removeItem(k); });

    // ═══════════════════════════════════════════════════════════════
    // PHASE 1: HOOK localStorage BEFORE any page script runs
    // ═══════════════════════════════════════════════════════════════
    var blockedWritePatterns = [
        'fingerprint', 'visitor', 'fp_', 'fpjs', '_fp', 'dedup',
        'ph_', 'posthog', 'utm_', 'referral', 'affiliate',
        'tracked_query', 'pending_fingerprint', 'first_login',
        'first_job', 'hascreatedfirstproject'
    ];

    // Keys that should return null on getItem (mask supabase auth)
    function isMaskedReadKey(key) {
        if (!key) return false;
        var kl = key.toLowerCase();
        // Mask supabase auth tokens - prevents supabase_user_exists detection
        if (kl.indexOf('sb-') === 0 && kl.indexOf('auth-token') !== -1) return true;
        // Mask trial indicator keys
        if (kl === 'fp_dedup') return true;
        if (kl.indexOf('pending_fingerprint') !== -1) return true;
        if (kl === 'first_login_completed') return true;
        if (kl === 'first_job_timestamp') return true;
        if (kl === 'hascreatedfirstproject') return true;
        return false;
    }

    function shouldBlockWrite(key) {
        if (!key) return false;
        var kl = key.toLowerCase();
        if (kl.indexOf('hasseen') === 0) return false;
        return blockedWritePatterns.some(function(p) { return kl.indexOf(p) !== -1; });
    }

    // Hook setItem
    var origSetItem = localStorage.setItem.bind(localStorage);
    localStorage.setItem = function(key, value) {
        if (shouldBlockWrite(key)) return;
        return origSetItem(key, value);
    };

    // Hook getItem - CRITICAL for supabase_user_exists bypass
    var origGetItem = localStorage.getItem.bind(localStorage);
    localStorage.getItem = function(key) {
        if (isMaskedReadKey(key)) return null;
        return origGetItem(key);
    };

    // Hook sessionStorage setItem
    var origSSSetItem = sessionStorage.setItem.bind(sessionStorage);
    sessionStorage.setItem = function(key, value) {
        if (shouldBlockWrite(key)) return;
        return origSSSetItem(key, value);
    };

    // ═══════════════════════════════════════════════════════════════
    // PHASE 2: NEUTRALIZE FingerprintJS Pro EARLY
    // ═══════════════════════════════════════════════════════════════
    var visitorId = '$visitorId';
    var deviceId = '$deviceId';

    var fakeFP = {
        load: function() {
            return Promise.resolve({
                get: function() {
                    return Promise.resolve({
                        visitorId: visitorId,
                        confidence: { score: 0.9999 },
                        components: {}
                    });
                }
            });
        }
    };

    window.FingerprintJS = fakeFP;
    window.Fingerprint2 = { get: function(cb) { cb([], visitorId); } };
    window.fpPromise = Promise.resolve({
        get: function() { return Promise.resolve({ visitorId: visitorId }); }
    });

    try {
        Object.defineProperty(window, 'FingerprintJS', {
            value: fakeFP, writable: false, configurable: false
        });
    } catch(e) {}

    // ═══════════════════════════════════════════════════════════════
    // PHASE 3: HOOK fetch & XHR EARLY to block tracking requests
    // ═══════════════════════════════════════════════════════════════
    var blockedDomains = [
        'fpjs.io', 'fpnpmcdn.net', 'fpcdn.io', 'fingerprint.com',
        'botd.fpjs.io', 'api.fpjs.io',
        'us.i.posthog.com', 'us-assets.i.posthog.com',
        'app.posthog.com', 'posthog.com',
        'connect.facebook.net', 'facebook.com/tr',
        'analytics.tiktok.com', 'tiktok.com/i18n',
        'cds.taboola.com', 'trc.taboola.com', 'r.wdfl.co',
        'rewardful.com', 'api.rewardful.com',
        'google-analytics.com', 'googletagmanager.com'
    ];

    function isBlocked(url) {
        if (!url) return false;
        var lowerUrl = url.toLowerCase();
        return blockedDomains.some(function(d) { return lowerUrl.indexOf(d) !== -1; });
    }

    var origFetch = window.fetch;
    window.fetch = function(url, opts) {
        var urlStr = (typeof url === 'string') ? url : (url && url.url) || '';
        if (isBlocked(urlStr)) {
            return Promise.resolve(new Response('{}', {
                status: 200, headers: { 'Content-Type': 'application/json' }
            }));
        }
        opts = opts || {};
        opts.headers = opts.headers || {};
        if (opts.headers instanceof Headers) {
            opts.headers.set('X-Device-Id', deviceId);
            opts.headers.delete('X-Fingerprint');
            opts.headers.delete('X-Visitor-Id');
        } else {
            opts.headers['X-Device-Id'] = deviceId;
            delete opts.headers['X-Fingerprint'];
            delete opts.headers['X-Visitor-Id'];
        }
        return origFetch.apply(this, arguments);
    };

    var origXHROpen = XMLHttpRequest.prototype.open;
    var origXHRSend = XMLHttpRequest.prototype.send;
    var origXHRSetHeader = XMLHttpRequest.prototype.setRequestHeader;

    XMLHttpRequest.prototype.open = function(method, url) {
        this._antifUrl = url;
        this._antifBlocked = isBlocked(url);
        return origXHROpen.apply(this, arguments);
    };

    XMLHttpRequest.prototype.setRequestHeader = function(name, value) {
        var nl = name.toLowerCase();
        if (nl === 'x-device-id') return origXHRSetHeader.call(this, name, deviceId);
        if (nl === 'x-fingerprint' || nl === 'x-visitor-id') return;
        return origXHRSetHeader.apply(this, arguments);
    };

    XMLHttpRequest.prototype.send = function(body) {
        if (this._antifBlocked) {
            var self = this;
            setTimeout(function() {
                Object.defineProperty(self, 'readyState', { value: 4, writable: false });
                Object.defineProperty(self, 'status', { value: 200, writable: false });
                Object.defineProperty(self, 'responseText', { value: '{}', writable: false });
                if (self.onreadystatechange) self.onreadystatechange();
                if (self.onload) self.onload();
            }, 0);
            return;
        }
        return origXHRSend.apply(this, arguments);
    };

    // ═══════════════════════════════════════════════════════════════
    // PHASE 4: NEUTRALIZE PostHog EARLY
    // ═══════════════════════════════════════════════════════════════
    var fakePostHog = {
        capture: function(){}, identify: function(){}, alias: function(){},
        people: { set: function(){}, set_once: function(){} },
        register: function(){}, register_once: function(){}, unregister: function(){},
        opt_out_capturing: function(){}, opt_in_capturing: function(){},
        has_opted_out_capturing: function() { return true; },
        reset: function(){},
        get_distinct_id: function() { return visitorId; },
        get_session_id: function() { return 'fake_session_' + Date.now(); },
        getFeatureFlag: function() { return undefined; },
        isFeatureEnabled: function() { return false; },
        onFeatureFlags: function(){},
        reloadFeatureFlags: function(){},
        init: function() { return fakePostHog; },
        __loaded: true, _i: [],
        config: { disable_session_recording: true }
    };
    window.posthog = fakePostHog;
    try {
        Object.defineProperty(window, 'posthog', {
            value: fakePostHog, writable: false, configurable: false
        });
    } catch(e) {}

    // Facebook & TikTok pixels
    window.fbq = function() {};
    window._fbq = window.fbq;
    window.ttq = { track: function(){}, identify: function(){}, page: function(){} };

    window._antifDeviceId = deviceId;
    window._antifVisitorId = visitorId;
})();
        """.trimIndent()
    }

    /**
     * ПОЛНЫЙ СКРИПТ ОБХОДА v6.0 - инжектится в onPageFinished
     * Повторно применяет хуки и добавляет canvas/timing спуфинг
     */
    fun generateFullBypassScript(): String {
        val deviceId = getOrCreateDeviceId()
        val visitorId = getOrCreateVisitorId()
        
        return """
(function() {
    'use strict';

    if (window._antifBypassApplied) return JSON.stringify({status: 'already_applied'});
    window._antifBypassApplied = true;

    var deviceId = '$deviceId';
    var visitorId = '$visitorId';
    window._antifDeviceId = deviceId;
    window._antifVisitorId = visitorId;

    // ═══════════════════════════════════════════════════════════════
    // RE-CLEANUP: удаляем всё что могло проскочить между early и full
    // ═══════════════════════════════════════════════════════════════
    var origGetItemDirect = Object.getOwnPropertyDescriptor(Storage.prototype, 'getItem');
    var rawGetItem = origGetItemDirect ? origGetItemDirect.value.bind(localStorage) : null;

    // Use raw access to check keys, bypassing our own hooks
    var keysToClean = [];
    for (var i = 0; i < localStorage.length; i++) {
        var k = localStorage.key(i);
        if (!k) continue;
        var kl = k.toLowerCase();
        if (kl === 'fp_dedup' ||
            kl.indexOf('pending_fingerprint') !== -1 ||
            kl.indexOf('first_login') !== -1 ||
            kl.indexOf('first_job') !== -1 ||
            kl.indexOf('hascreatedfirstproject') !== -1 ||
            kl.indexOf('fpjs') !== -1) {
            keysToClean.push(k);
        }
    }
    keysToClean.forEach(function(k) { localStorage.removeItem(k); });

    // ═══════════════════════════════════════════════════════════════
    // RE-ENSURE: FingerprintJS, PostHog, fetch hooks
    // (in case early script didn't fire or was overwritten)
    // ═══════════════════════════════════════════════════════════════
    
    // FingerprintJS
    var fakeFP = {
        load: function() {
            return Promise.resolve({
                get: function() {
                    return Promise.resolve({
                        visitorId: visitorId,
                        confidence: { score: 0.9999 },
                        components: {}
                    });
                }
            });
        }
    };
    try {
        Object.defineProperty(window, 'FingerprintJS', {
            value: fakeFP, writable: false, configurable: true
        });
    } catch(e) {
        window.FingerprintJS = fakeFP;
    }
    window.fpPromise = Promise.resolve({
        get: function() { return Promise.resolve({ visitorId: visitorId }); }
    });

    // Ensure localStorage hooks are still active
    // (re-apply getItem mask if it was restored by framework)
    if (!window._antifEarlyApplied) {
        var blockedWritePatterns = [
            'fingerprint', 'visitor', 'fp_', 'fpjs', '_fp', 'dedup',
            'ph_', 'posthog', 'utm_', 'referral', 'affiliate',
            'tracked_query', 'pending_fingerprint', 'first_login',
            'first_job', 'hascreatedfirstproject'
        ];

        function isMaskedReadKey(key) {
            if (!key) return false;
            var kl = key.toLowerCase();
            if (kl.indexOf('sb-') === 0 && kl.indexOf('auth-token') !== -1) return true;
            if (kl === 'fp_dedup') return true;
            if (kl.indexOf('pending_fingerprint') !== -1) return true;
            if (kl === 'first_login_completed') return true;
            if (kl === 'first_job_timestamp') return true;
            if (kl === 'hascreatedfirstproject') return true;
            return false;
        }

        function shouldBlockWrite(key) {
            if (!key) return false;
            var kl = key.toLowerCase();
            if (kl.indexOf('hasseen') === 0) return false;
            return blockedWritePatterns.some(function(p) { return kl.indexOf(p) !== -1; });
        }

        var origSetItem = localStorage.setItem.bind(localStorage);
        localStorage.setItem = function(key, value) {
            if (shouldBlockWrite(key)) return;
            return origSetItem(key, value);
        };

        var origGetItem = localStorage.getItem.bind(localStorage);
        localStorage.getItem = function(key) {
            if (isMaskedReadKey(key)) return null;
            return origGetItem(key);
        };
    }

    // ═══════════════════════════════════════════════════════════════
    // CANVAS FINGERPRINT - subtle Gaussian noise
    // ═══════════════════════════════════════════════════════════════
    var canvasSeed = Math.floor(Math.random() * 255);
    var origToDataURL = HTMLCanvasElement.prototype.toDataURL;
    HTMLCanvasElement.prototype.toDataURL = function(type) {
        try {
            var ctx = this.getContext('2d');
            if (ctx && this.width > 0 && this.height > 0 && this.width < 500) {
                var imageData = ctx.getImageData(0, 0, Math.min(this.width, 32), Math.min(this.height, 32));
                for (var i = 0; i < imageData.data.length; i += 4) {
                    imageData.data[i] ^= (canvasSeed & 1);
                }
                ctx.putImageData(imageData, 0, 0);
            }
        } catch(e) {}
        return origToDataURL.apply(this, arguments);
    };

    // ═══════════════════════════════════════════════════════════════
    // INTERCEPT localStorage.key() to hide masked keys from enumeration
    // This prevents detection via iterating all localStorage keys
    // ═══════════════════════════════════════════════════════════════
    var origKeys = Object.keys;
    var origLocalStorageKey = localStorage.key.bind(localStorage);
    var origLocalStorageLength = Object.getOwnPropertyDescriptor(Storage.prototype, 'length');

    // Override Object.keys for localStorage specifically
    // Not needed if getItem returns null - the keys exist but return null

    // ═══════════════════════════════════════════════════════════════
    // RESULT
    // ═══════════════════════════════════════════════════════════════
    window.antifBypass = {
        version: '6.0',
        deviceId: deviceId,
        visitorId: visitorId,
        status: 'ACTIVE'
    };

    return JSON.stringify(window.antifBypass);
})();
        """.trimIndent()
    }

    /**
     * ПОЛНЫЙ СБРОС - только по явному запросу пользователя!
     * Сбрасывает ВСЕ данные включая модалки
     */
    fun generateFullResetScript(): String {
        val deviceId = getOrCreateDeviceId()
        val visitorId = getOrCreateVisitorId()
        
        return """
(function() {
    'use strict';
    console.log('[AntiF] FULL RESET - Clearing ALL data...');

    var removedCount = 0;
    
    // Remove ALL keys - including supabase auth (user wants full reset)
    var keysToRemove = [];
    for (var i = 0; i < localStorage.length; i++) {
        var key = localStorage.key(i);
        if (key) keysToRemove.push(key);
    }
    
    keysToRemove.forEach(function(k) {
        localStorage.removeItem(k);
        removedCount++;
    });
    
    console.log('[AntiF] localStorage cleared: ' + removedCount + ' keys');

    // SessionStorage
    var ssCount = sessionStorage.length;
    sessionStorage.clear();
    console.log('[AntiF] sessionStorage cleared: ' + ssCount + ' keys');

    // Cookies
    var domains = ['', '.emergent.sh', '.emergentagent.com', '.' + window.location.hostname];
    document.cookie.split(';').forEach(function(c) {
        var name = c.split('=')[0].trim();
        domains.forEach(function(domain) {
            var cookieStr = name + '=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/';
            if (domain) cookieStr += ';domain=' + domain;
            document.cookie = cookieStr;
        });
    });
    console.log('[AntiF] Cookies cleared');

    // IndexedDB
    if (window.indexedDB && indexedDB.databases) {
        indexedDB.databases().then(function(dbs) {
            dbs.forEach(function(db) {
                if (db.name) {
                    indexedDB.deleteDatabase(db.name);
                }
            });
        }).catch(function(){});
    }
    console.log('[AntiF] IndexedDB cleared');

    // Reset bypass flags
    window._antifBypassApplied = false;
    window._antifEarlyApplied = false;
    
    console.log('[AntiF] FULL RESET COMPLETE. Reload page.');
    
    return JSON.stringify({
        success: true,
        localStorageCleared: removedCount,
        sessionStorageCleared: ssCount,
        message: 'Full reset complete. Reload page.'
    });
})();
        """.trimIndent()
    }

    /**
     * Скрипт сброса модалок - ТОЛЬКО по явному запросу
     */
    fun generateResetModalsScript(): String {
        return """
(function() {
    console.log('[AntiF] Resetting modal flags...');
    var cleared = 0;
    var keys = Object.keys(localStorage);
    keys.forEach(function(k) {
        if (k.startsWith('hasSeen') || k.includes('modal_dismiss') || k === 'openclaw-modal-dismiss') {
            localStorage.removeItem(k);
            cleared++;
            console.log('[AntiF] Removed: ' + k);
        }
    });
    console.log('[AntiF] Modal flags reset: ' + cleared + '. Reload page to see modals.');
    return JSON.stringify({cleared: cleared, message: 'Reload page to see modals'});
})();
        """.trimIndent()
    }

    /**
     * Скрипт спуфинга Device-Id
     */
    fun generateDeviceIdSpoofScript(): String {
        val deviceId = getOrCreateDeviceId()
        return """
(function() {
    var id = '$deviceId';
    window._antifDeviceId = id;
    
    var origFetch = window.fetch;
    window.fetch = function(url, opts) {
        opts = opts || {}; opts.headers = opts.headers || {};
        if (opts.headers instanceof Headers) opts.headers.set('X-Device-Id', id);
        else opts.headers['X-Device-Id'] = id;
        return origFetch.apply(this, arguments);
    };
    
    console.log('[AntiF] Device-Id: ' + id);
    return id;
})();
        """.trimIndent()
    }

    /**
     * Скрипт блокировки трекинга
     */
    fun generateTrackingBlockerScript(): String {
        return """
(function() {
    var blocked = ['fpjs.io', 'fpnpmcdn.net', 'fpcdn.io', 'posthog.com', 'connect.facebook.net', 'analytics.tiktok.com'];
    function isBlocked(url) { return url && blocked.some(function(d) { return url.toLowerCase().includes(d); }); }
    
    var origFetch = window.fetch;
    window.fetch = function(url, opts) {
        if (isBlocked(typeof url === 'string' ? url : url.url)) return Promise.reject(new Error('Blocked'));
        return origFetch.apply(this, arguments);
    };
    
    window.posthog = { capture: function(){}, identify: function(){}, init: function(){} };
    window.fbq = function(){};
    window.ttq = { track: function(){}, identify: function(){} };
    
    console.log('[AntiF] Tracking blocker active');
})();
        """.trimIndent()
    }

    /**
     * Скрипт спуфинга Feature Flags
     */
    fun generateFeatureFlagsSpoofScript(): String {
        return """
(function() {
    var flags = {"bundle_id_edit_enabled":true,"enterprise_plan_enabled":true,"ultra_mode_enabled":true,"visual_edit_enabled":true,"quick_topup":true};
    if (window.posthog) {
        window.posthog.isFeatureEnabled = function(f) { return flags[f] || false; };
        window.posthog.getFeatureFlag = function(f) { return flags[f]; };
    }
    console.log('[AntiF] Feature Flags spoofed');
})();
        """.trimIndent()
    }

    /**
     * Скрипт нейтрализации PostHog
     */
    fun generatePostHogNeutralizeScript(): String {
        val visitorId = getOrCreateVisitorId()
        return """
(function() {
    var visitorId = '$visitorId';
    var fake = {
        capture: function(){}, identify: function(){}, alias: function(){},
        people: { set: function(){} }, register: function(){},
        opt_out_capturing: function(){}, has_opted_out_capturing: function() { return true; },
        reset: function(){}, get_distinct_id: function() { return visitorId; },
        getFeatureFlag: function() { return undefined; }, isFeatureEnabled: function() { return false; },
        init: function() { return fake; }, __loaded: true
    };
    try {
        if (typeof window.posthog === 'undefined') window.posthog = fake;
        else for (var key in fake) { try { window.posthog[key] = fake[key]; } catch(e) {} }
    } catch(e) {}
    console.log('[AntiF] PostHog neutralized');
})();
        """.trimIndent()
    }

    /**
     * Скрипт региональных скидок - БЕЗ сброса модалок!
     */
    fun generateRegionalDiscountScript(region: String): String {
        val (tz, lang) = when (region.lowercase()) {
            "turkey", "tr" -> "Europe/Istanbul" to "tr-TR"
            "canada", "ca" -> "America/Toronto" to "en-CA"
            else -> "Europe/Istanbul" to "tr-TR"
        }
        
        return """
(function() {
    console.log('[AntiF] Applying $region regional settings...');
    
    var origDTF = Intl.DateTimeFormat;
    Intl.DateTimeFormat = function(locale, options) {
        options = options || {};
        options.timeZone = '$tz';
        return new origDTF(locale || '$lang', options);
    };
    Intl.DateTimeFormat.prototype = origDTF.prototype;
    
    try {
        Object.defineProperty(navigator, 'language', { get: function() { return '$lang'; }, configurable: true });
        Object.defineProperty(navigator, 'languages', { get: function() { return ['$lang']; }, configurable: true });
    } catch(e) {}
    
    console.log('[AntiF] $region timezone/language applied.');
    console.log('[AntiF] To see regional modals, use "Reset modals" from menu, then reload.');
})();
        """.trimIndent()
    }

    /**
     * Show Emergent stored data
     */
    fun generateShowEmergentDataScript(): String {
        return """
(function() {
    console.log('[AntiF] === Emergent Data ===');
    var keys = Object.keys(localStorage).filter(function(k) {
        return k.startsWith('hasSeen') || k.includes('modal') || k.includes('emergent') || 
               k.includes('openclaw') || k.includes('first_') || k.includes('fp_');
    });
    keys.forEach(function(k) { console.log(k + ' = ' + localStorage.getItem(k)); });
    console.log('[AntiF] Total Emergent keys: ' + keys.length);
    return JSON.stringify({keys: keys, count: keys.length});
})();
        """.trimIndent()
    }

    /**
     * API Monitor script
     */
    fun generateApiMonitorScript(): String {
        return """
(function() {
    window._antifApiLog = [];
    var origFetch = window.fetch;
    window.fetch = function(url, opts) {
        var entry = { url: typeof url === 'string' ? url : url.url, method: (opts && opts.method) || 'GET', time: new Date().toISOString() };
        window._antifApiLog.push(entry);
        console.log('[API] ' + entry.method + ' ' + entry.url);
        return origFetch.apply(this, arguments);
    };
    window.antifApiLog = function() { console.table(window._antifApiLog); };
    console.log('[AntiF] API Monitor enabled. Call antifApiLog() to view log.');
})();
        """.trimIndent()
    }

    /**
     * Show full config
     */
    fun generateShowFullConfigScript(): String {
        return """
(function() {
    console.log('[AntiF] === Full Config ===');
    var all = {};
    for (var i = 0; i < localStorage.length; i++) {
        var k = localStorage.key(i);
        all[k] = localStorage.getItem(k);
    }
    console.log(JSON.stringify(all, null, 2));
    return JSON.stringify(all);
})();
        """.trimIndent()
    }

    /**
     * Скрипт определения использования триалки - ОБНОВЛЁННЫЙ
     * Использует прямой доступ к Storage.prototype для обхода наших хуков
     */
    fun generateTrialDetectionScript(): String {
        return """
(function() {
    'use strict';
    console.log('[AntiF] === TRIAL DETECTION v3.0 (deep scan) ===');
    
    // Use raw Storage access to bypass our own hooks
    var rawGetItem = Storage.prototype.getItem.bind(localStorage);
    
    var result = {
        trialUsed: false,
        indicators: [],
        details: []
    };
    
    // 1. Check EMERGENT-SPECIFIC keys
    var emergentKeys = [
        'first_login_completed',
        'first_job_timestamp', 
        'hasCreatedFirstProject',
        'fp_dedup'
    ];
    
    emergentKeys.forEach(function(k) {
        var val = rawGetItem(k);
        if (val) {
            result.trialUsed = true;
            result.indicators.push(k);
            result.details.push(k + '=' + val);
        }
    });
    
    // 2. Check pending_fingerprint_* keys
    for (var i = 0; i < localStorage.length; i++) {
        var k = localStorage.key(i);
        if (k && k.startsWith('pending_fingerprint_')) {
            var val = rawGetItem(k);
            if (val) {
                result.trialUsed = true;
                result.indicators.push(k);
            }
        }
    }
    
    // 3. Check hasSeen modal flags
    var seenModals = [];
    for (var j = 0; j < localStorage.length; j++) {
        var mk = localStorage.key(j);
        if (mk && mk.startsWith('hasSeen')) {
            var mv = rawGetItem(mk);
            if (mv === 'true') seenModals.push(mk);
        }
    }
    if (seenModals.length > 0) {
        result.details.push('Seen modals: ' + seenModals.length);
    }
    
    // 4. Check Supabase auth token for user info
    for (var si = 0; si < localStorage.length; si++) {
        var sk = localStorage.key(si);
        if (sk && sk.startsWith('sb-') && sk.indexOf('auth-token') !== -1) {
            try {
                var authData = JSON.parse(rawGetItem(sk));
                if (authData && authData.user) {
                    result.details.push('User ID: ' + (authData.user.id || 'unknown'));
                    result.details.push('Email: ' + (authData.user.email || 'unknown'));
                    result.details.push('Created: ' + (authData.user.created_at || 'unknown'));
                    
                    if (authData.user.id) {
                        result.trialUsed = true;
                        result.indicators.push('supabase_user_exists');
                    }
                }
            } catch(e) {}
        }
    }
    
    // 5. Generate summary
    var indicatorCount = result.indicators.length;
    result.message = result.trialUsed 
        ? 'TRIAL DETECTED! Found ' + indicatorCount + ' indicators.'
        : 'No trial usage detected on this device.';
    
    console.log('[AntiF] Result: ' + result.message);
    console.log('[AntiF] Indicators: ' + result.indicators.join(', '));
    console.log('[AntiF] Details: ' + result.details.join('; '));
    
    window._antifTrialResult = result;
    
    return JSON.stringify(result);
})();
        """.trimIndent()
    }

    /**
     * Скрипт сброса триала - ОБНОВЛЁННЫЙ
     * Удаляет ВСЁ включая supabase auth tokens
     */
    fun generateTrialResetScript(): String {
        return """
(function() {
    'use strict';
    console.log('[AntiF] === TRIAL RESET v3.0 ===');
    
    var removed = { localStorage: 0, sessionStorage: 0, cookies: 0 };
    
    // Remove ALL trial and tracking related keys INCLUDING supabase auth
    var keysToRemove = [];
    for (var i = 0; i < localStorage.length; i++) {
        var key = localStorage.key(i);
        if (!key) continue;
        var keyLower = key.toLowerCase();
        
        // Remove trial/tracking related
        if (keyLower.includes('trial') || keyLower.startsWith('hasseen') ||
            keyLower.includes('first_') || keyLower.includes('fp_') ||
            keyLower.includes('modal') || keyLower.includes('dismiss') ||
            keyLower.includes('fingerprint') || keyLower.includes('pending_') ||
            keyLower.includes('onboarding') || keyLower.includes('created') ||
            keyLower.includes('dedup') || keyLower.includes('fpjs') ||
            (keyLower.startsWith('sb-') && keyLower.includes('auth-token')) ||
            keyLower.includes('supabase')) {
            keysToRemove.push(key);
        }
    }
    
    keysToRemove.forEach(function(k) {
        localStorage.removeItem(k);
        removed.localStorage++;
        console.log('[AntiF] Removed: ' + k);
    });
    
    // Clear sessionStorage trial data
    var sKeys = [];
    for (var j = 0; j < sessionStorage.length; j++) {
        var sk = sessionStorage.key(j);
        if (sk && (sk.toLowerCase().includes('trial') || 
            sk.toLowerCase().includes('fingerprint') ||
            sk.toLowerCase().includes('dedup'))) {
            sKeys.push(sk);
        }
    }
    sKeys.forEach(function(sk) {
        sessionStorage.removeItem(sk);
        removed.sessionStorage++;
    });
    
    // Reset bypass flags
    window._antifBypassApplied = false;
    window._antifEarlyApplied = false;
    
    var total = removed.localStorage + removed.sessionStorage;
    console.log('[AntiF] === TRIAL RESET COMPLETE ===');
    console.log('[AntiF] Removed: ' + total + ' items');
    console.log('[AntiF] Reload page to apply changes.');
    
    return JSON.stringify({
        success: true,
        removed: removed,
        total: total,
        message: 'Trial data reset. Reload page.'
    });
})();
        """.trimIndent()
    }

    /**
     * Supabase ANON KEY spoof script
     */
    fun generateSupabaseSpoofScript(): String {
        return """
(function() {
    'use strict';
    console.log('[AntiF] === Supabase ANON KEY Spoof ===');
    
    var origFetch = window.fetch;
    window.fetch = function(url, opts) {
        if (typeof url === 'string' && url.includes('supabase')) {
            opts = opts || {};
            opts.headers = opts.headers || {};
            if (opts.headers['apikey']) {
                console.log('[AntiF] Supabase request intercepted: ' + url);
            }
        }
        return origFetch.apply(this, arguments);
    };
    
    if (window.supabase || window.__SUPABASE_CLIENT__) {
        var client = window.supabase || window.__SUPABASE_CLIENT__;
        console.log('[AntiF] Supabase client found, monitoring...');
    }
    
    console.log('[AntiF] Supabase ANON KEY spoofing enabled');
})();
        """.trimIndent()
    }

    // Aliases for compatibility
    fun generateFullCleanupScript(): String = generateTrackingBlockerScript()
    fun generateFullResetModalsScript(): String = generateResetModalsScript()
}
