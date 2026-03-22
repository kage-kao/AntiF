package com.antif.browser.core

/**
 * EmergentBypass - BYPASS для Emergent.sh
 * v3.0 - Fixed: preserves auth, allows modals
 */
object EmergentBypass {

    /**
     * ПОЛНЫЙ СКРИПТ ОБХОДА v5.0 - ПОЛНЫЙ СБРОС ТРИАЛА
     * На основе полного анализа emergent.sh
     * emergent.sh НЕ СМОЖЕТ определить что триал использован
     */
    fun generateFullBypassScript(): String {
        return """
(function() {
    'use strict';
    console.log('[AntiF] BYPASS v5.0 - FULL TRIAL RESET (Based on Emergent Analysis)');

    // ═══════════════════════════════════════════════════════════════════════
    // 0. КРИТИЧЕСКИЕ КЛЮЧИ КОТОРЫЕ НУЖНО УДАЛИТЬ (из анализа emergent.sh)
    // ═══════════════════════════════════════════════════════════════════════
    
    // Ключи связанные с триалом и идентификацией
    var criticalKeys = [
        // Fingerprint и идентификация
        'fp_dedup',
        'first_login_completed', 
        'first_job_timestamp',
        // Все pending_fingerprint_* будут удалены отдельно
        
        // UTM и отслеживание
        'utm_source', 'utm_medium', 'utm_campaign', 'utm_content', 'utm_term',
        'user_referrer', 'referral_code',
        
        // Модалки и офферы (ВСЕ hasSeenXXX)
        'hasSeenFreeCreditsModal',
        'hasSeenOneDollarModal', 
        'hasSeenFreeStandardPlanModal',
        'hasSeenFreeWeekendTurkeyModal',
        'hasSeenFreeWeekendCanadaModal',
        'hasSeenOpusModal',
        'hasSeenFirstTopupDiscountModal',
        'hasSeenClaudeOpus4Modal',
        'hasSeenClaude45Modal',
        'hasSeenMCPIntroModal',
        'hasSeenUltraThinkingModal',
        'hasSeenFreeTrialModal',
        'hasSeenValentineCreditsModal',
        'hasSeenChristmasGiftingModal',
        'hasSeenWarmupMailModal',
        'hasSeenProjectTourModal',
        'hasSeenForkIntroModal',
        'hasSeenMobileAgentModal',
        'hasSeenE2IntroModal',
        'hasSeenNewYearModal',
        'hasSeenBlackFridayModal',
        'hasSeenCyberMondayModal',
        
        // Состояние приложения
        'hasCreatedFirstProject',
        'commitment_free_checkout',
        'postLoginReturnUrl',
        
        // PostHog 
        'ph_phc_', // префикс PostHog
        
        // Другие ключи отслеживания
        'openclaw-modal-dismiss',
        'tracked_query_params'
    ];
    
    // ═══════════════════════════════════════════════════════════════════════
    // 1. УДАЛЕНИЕ ВСЕХ СЛЕДОВ ИЗ LOCALSTORAGE
    // ═══════════════════════════════════════════════════════════════════════
    console.log('[AntiF] Clearing localStorage trial traces...');
    
    var removedCount = 0;
    var protectedPrefixes = ['sb-', 'supabase.auth'];
    
    function isProtected(key) {
        return protectedPrefixes.some(function(p) { return key.startsWith(p); });
    }
    
    // Удаляем ВСЕ ключи кроме auth
    var keysToRemove = [];
    for (var i = 0; i < localStorage.length; i++) {
        var key = localStorage.key(i);
        if (key && !isProtected(key)) {
            keysToRemove.push(key);
        }
    }
    
    keysToRemove.forEach(function(k) {
        localStorage.removeItem(k);
        removedCount++;
        console.log('[AntiF] Removed: ' + k);
    });
    
    console.log('[AntiF] localStorage cleared: ' + removedCount + ' keys removed');

    // ═══════════════════════════════════════════════════════════════════════
    // 2. ПОЛНАЯ ОЧИСТКА SESSIONSTORAGE
    // ═══════════════════════════════════════════════════════════════════════
    var ssCount = sessionStorage.length;
    sessionStorage.clear();
    console.log('[AntiF] sessionStorage cleared: ' + ssCount + ' keys');

    // ═══════════════════════════════════════════════════════════════════════
    // 3. УДАЛЕНИЕ COOKIES (Cloudflare, PostHog, Tracking pixels)
    // ═══════════════════════════════════════════════════════════════════════
    console.log('[AntiF] Clearing tracking cookies...');
    
    var trackingCookies = [
        '__cf_bm', '__cfduid', // Cloudflare
        'gclid', 'fbclid', 'msclkid', 'ttclid', 'twclid', 'li_fat_id', 'igshid', // Ad IDs
        'utm_source', 'utm_medium', 'utm_campaign', 'utm_content', 'utm_term',
        'via', 'ref', 'referral', 'affiliate', 'action',
        'rewardful_affiliate', 'ps_partner_key', // Affiliate
        'tracked_query_params',
        '_fbp', '_fbc', '_ga', '_gid', '_gat' // Analytics
    ];
    
    var domains = ['', '.emergent.sh', '.emergentagent.com', '.' + window.location.hostname];
    var paths = ['/', ''];
    
    document.cookie.split(';').forEach(function(c) {
        var name = c.split('=')[0].trim();
        // Удаляем все cookies начинающиеся с ph_ (PostHog)
        var shouldDelete = name.startsWith('ph_') || name.startsWith('$') || 
                          trackingCookies.indexOf(name) !== -1 ||
                          name.includes('posthog') || name.includes('fingerprint');
        
        if (shouldDelete || !isProtected(name)) {
            domains.forEach(function(domain) {
                paths.forEach(function(path) {
                    var cookieStr = name + '=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=' + (path || '/');
                    if (domain) cookieStr += ';domain=' + domain;
                    document.cookie = cookieStr;
                });
            });
        }
    });
    
    console.log('[AntiF] Cookies cleared');

    // ═══════════════════════════════════════════════════════════════════════
    // 4. ОЧИСТКА INDEXEDDB
    // ═══════════════════════════════════════════════════════════════════════
    if (window.indexedDB && indexedDB.databases) {
        indexedDB.databases().then(function(dbs) {
            dbs.forEach(function(db) {
                if (db.name && !db.name.includes('supabase')) {
                    indexedDB.deleteDatabase(db.name);
                    console.log('[AntiF] Deleted IndexedDB: ' + db.name);
                }
            });
        }).catch(function(){});
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 5. ГЕНЕРАЦИЯ НОВОГО DEVICE ID И VISITOR ID
    // ═══════════════════════════════════════════════════════════════════════
    function generateUUID() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random() * 16 | 0;
            return (c == 'x' ? r : (r & 0x3 | 0x8)).toString(16);
        });
    }
    
    var newDeviceId = generateUUID();
    var newVisitorId = 'v_' + Math.random().toString(36).substr(2, 20);
    window._antifDeviceId = newDeviceId;
    window._antifVisitorId = newVisitorId;
    console.log('[AntiF] New Device-Id: ' + newDeviceId);
    console.log('[AntiF] New Visitor-Id: ' + newVisitorId);

    // ═══════════════════════════════════════════════════════════════════════
    // 6. БЛОКИРОВКА FINGERPRINT JS PRO (Критически важно!)
    // ═══════════════════════════════════════════════════════════════════════
    console.log('[AntiF] Neutralizing FingerprintJS Pro...');
    
    // Полная подмена FingerprintJS
    var fakeFingerprint = {
        load: function(opts) {
            console.log('[AntiF] FingerprintJS.load() intercepted');
            return Promise.resolve({
                get: function(opts) {
                    console.log('[AntiF] FingerprintJS.get() intercepted');
                    return Promise.resolve({
                        visitorId: newVisitorId,
                        confidence: { score: 0.1 },
                        components: {}
                    });
                }
            });
        }
    };
    
    // Блокируем все варианты FingerprintJS
    window.FingerprintJS = fakeFingerprint;
    window.Fingerprint2 = { get: function(cb) { cb([], newVisitorId); } };
    window.fpPromise = Promise.resolve({ get: function() { return Promise.resolve({ visitorId: newVisitorId }); } });
    
    // Защита от перезаписи
    try {
        Object.defineProperty(window, 'FingerprintJS', { value: fakeFingerprint, writable: false, configurable: false });
    } catch(e) {}

    // ═══════════════════════════════════════════════════════════════════════
    // 7. БЛОКИРОВКА TRACKING DOMAINS
    // ═══════════════════════════════════════════════════════════════════════
    var blockedDomains = [
        // FingerprintJS
        'fpjs.io', 'fpnpmcdn.net', 'fpcdn.io', 'fingerprint.com', 'botd.fpjs.io', 'api.fpjs.io',
        // PostHog  
        'us.i.posthog.com', 'us-assets.i.posthog.com', 'app.posthog.com', 'posthog.com',
        // Facebook
        'connect.facebook.net', 'facebook.com/tr', 'graph.facebook.com',
        // TikTok
        'analytics.tiktok.com', 'tiktok.com/i18n',
        // Other tracking
        'cds.taboola.com', 'trc.taboola.com', 'r.wdfl.co', 
        'rewardful.com', 'api.rewardful.com',
        'google-analytics.com', 'googletagmanager.com'
    ];
    
    function isBlocked(url) {
        if (!url) return false;
        var lowerUrl = url.toLowerCase();
        return blockedDomains.some(function(d) { return lowerUrl.indexOf(d) !== -1; });
    }
    
    // Перехват fetch
    var origFetch = window.fetch;
    window.fetch = function(url, opts) {
        var urlStr = (typeof url === 'string') ? url : (url && url.url) || '';
        
        if (isBlocked(urlStr)) {
            console.log('[AntiF] BLOCKED fetch: ' + urlStr);
            return Promise.resolve(new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } }));
        }
        
        // Подменяем headers
        opts = opts || {};
        opts.headers = opts.headers || {};
        
        if (opts.headers instanceof Headers) {
            opts.headers.set('X-Device-Id', newDeviceId);
            opts.headers.delete('X-Fingerprint');
            opts.headers.delete('X-Visitor-Id');
        } else {
            opts.headers['X-Device-Id'] = newDeviceId;
            delete opts.headers['X-Fingerprint'];
            delete opts.headers['X-Visitor-Id'];
        }
        
        return origFetch.apply(this, arguments);
    };
    
    // Перехват XHR
    var origXHROpen = XMLHttpRequest.prototype.open;
    var origXHRSend = XMLHttpRequest.prototype.send;
    var origXHRSetHeader = XMLHttpRequest.prototype.setRequestHeader;
    
    XMLHttpRequest.prototype.open = function(method, url) {
        this._antifUrl = url;
        this._antifBlocked = isBlocked(url);
        return origXHROpen.apply(this, arguments);
    };
    
    XMLHttpRequest.prototype.setRequestHeader = function(name, value) {
        var nameLower = name.toLowerCase();
        if (nameLower === 'x-device-id') {
            return origXHRSetHeader.call(this, name, newDeviceId);
        }
        if (nameLower === 'x-fingerprint' || nameLower === 'x-visitor-id') {
            return; // Блокируем
        }
        return origXHRSetHeader.apply(this, arguments);
    };
    
    XMLHttpRequest.prototype.send = function(body) {
        if (this._antifBlocked) {
            console.log('[AntiF] BLOCKED XHR: ' + this._antifUrl);
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

    // ═══════════════════════════════════════════════════════════════════════
    // 8. НЕЙТРАЛИЗАЦИЯ POSTHOG (Полная)
    // ═══════════════════════════════════════════════════════════════════════
    console.log('[AntiF] Neutralizing PostHog...');
    
    var fakePostHog = {
        capture: function(){}, identify: function(){}, alias: function(){}, 
        people: { set: function(){}, set_once: function(){} }, 
        register: function(){}, register_once: function(){}, unregister: function(){},
        opt_out_capturing: function(){}, opt_in_capturing: function(){},
        has_opted_out_capturing: function() { return true; },
        reset: function(){}, 
        get_distinct_id: function() { return newVisitorId; },
        get_session_id: function() { return 'fake_session_' + Date.now(); },
        getFeatureFlag: function() { return undefined; }, 
        isFeatureEnabled: function() { return false; },
        onFeatureFlags: function(){},
        reloadFeatureFlags: function(){},
        init: function() { return fakePostHog; }, 
        __loaded: true, 
        _i: [],
        config: { disable_session_recording: true }
    };
    
    window.posthog = fakePostHog;
    try {
        Object.defineProperty(window, 'posthog', { value: fakePostHog, writable: false, configurable: false });
    } catch(e) {}

    // ═══════════════════════════════════════════════════════════════════════
    // 9. НЕЙТРАЛИЗАЦИЯ FACEBOOK & TIKTOK PIXELS
    // ═══════════════════════════════════════════════════════════════════════
    window.fbq = function() { console.log('[AntiF] fbq blocked'); };
    window._fbq = window.fbq;
    window.ttq = { track: function(){}, identify: function(){}, page: function(){} };

    // ═══════════════════════════════════════════════════════════════════════
    // 10. БЛОКИРОВКА ЗАПИСИ TRACKING ДАННЫХ В STORAGE
    // ═══════════════════════════════════════════════════════════════════════
    console.log('[AntiF] Blocking storage writes for tracking data...');
    
    var blockedKeywords = [
        'trial', 'hasseen', 'modal', 'fingerprint', 'visitor', 'device',
        'ph_', 'posthog', 'fp_', 'fpjs', '_fp', 'dedup',
        'onboarding', 'dismiss', 'credits', 'free', 'promo', 'offer',
        'first_login', 'first_job', 'utm_', 'referral', 'affiliate',
        'tracked_query', 'pending_fingerprint'
    ];
    
    function shouldBlock(key) {
        var kl = key.toLowerCase();
        return blockedKeywords.some(function(kw) { return kl.includes(kw); });
    }
    
    var origLSSetItem = localStorage.setItem.bind(localStorage);
    localStorage.setItem = function(key, value) {
        if (shouldBlock(key)) {
            console.log('[AntiF] BLOCKED localStorage.setItem: ' + key);
            return;
        }
        return origLSSetItem(key, value);
    };
    
    var origSSSetItem = sessionStorage.setItem.bind(sessionStorage);
    sessionStorage.setItem = function(key, value) {
        if (shouldBlock(key)) {
            console.log('[AntiF] BLOCKED sessionStorage.setItem: ' + key);
            return;
        }
        return origSSSetItem(key, value);
    };

    // ═══════════════════════════════════════════════════════════════════════
    // 11. СПУФИНГ CANVAS FINGERPRINT
    // ═══════════════════════════════════════════════════════════════════════
    var origToDataURL = HTMLCanvasElement.prototype.toDataURL;
    HTMLCanvasElement.prototype.toDataURL = function(type) {
        var ctx = this.getContext('2d');
        if (ctx) {
            var imageData = ctx.getImageData(0, 0, this.width, this.height);
            for (var i = 0; i < imageData.data.length; i += 4) {
                imageData.data[i] ^= (Math.random() * 2) | 0; // Add noise to R channel
            }
            ctx.putImageData(imageData, 0, 0);
        }
        return origToDataURL.apply(this, arguments);
    };

    // ═══════════════════════════════════════════════════════════════════════
    // РЕЗУЛЬТАТ
    // ═══════════════════════════════════════════════════════════════════════
    console.log('');
    console.log('[AntiF] ╔══════════════════════════════════════════════════════════╗');
    console.log('[AntiF] ║         BYPASS v5.0 COMPLETE - TRIAL RESET               ║');
    console.log('[AntiF] ╠══════════════════════════════════════════════════════════╣');
    console.log('[AntiF] ║ ✓ localStorage cleared: ' + removedCount + ' keys');
    console.log('[AntiF] ║ ✓ sessionStorage cleared: ' + ssCount + ' keys');
    console.log('[AntiF] ║ ✓ Cookies cleared');
    console.log('[AntiF] ║ ✓ IndexedDB cleared');
    console.log('[AntiF] ║ ✓ FingerprintJS Pro neutralized');
    console.log('[AntiF] ║ ✓ PostHog neutralized');
    console.log('[AntiF] ║ ✓ Tracking pixels blocked');
    console.log('[AntiF] ║ ✓ Storage write protection enabled');
    console.log('[AntiF] ║ ✓ New Device-Id: ' + newDeviceId.substr(0,8) + '...');
    console.log('[AntiF] ║ ✓ New Visitor-Id: ' + newVisitorId.substr(0,12) + '...');
    console.log('[AntiF] ╠══════════════════════════════════════════════════════════╣');
    console.log('[AntiF] ║  emergent.sh CANNOT detect previous trial usage!         ║');
    console.log('[AntiF] ╚══════════════════════════════════════════════════════════╝');
    
    window.antifBypass = { 
        version: '5.0',
        deviceId: newDeviceId, 
        visitorId: newVisitorId,
        localStorageCleared: removedCount,
        sessionStorageCleared: ssCount,
        status: 'TRIAL_RESET_COMPLETE'
    };
    
    return JSON.stringify(window.antifBypass);
})();
        """.trimIndent()
    }

    /**
     * Скрипт сброса модалок - просто сбрасывает флаги hasSeen
     */
    fun generateResetModalsScript(): String {
        return """
(function() {
    console.log('[AntiF] Resetting modal flags...');
    var cleared = 0;
    Object.keys(localStorage).forEach(function(k) {
        if (k.startsWith('hasSeen') || k.includes('modal_dismiss') || k === 'openclaw-modal-dismiss') {
            localStorage.removeItem(k);
            cleared++;
        }
    });
    console.log('[AntiF] Modal flags reset: ' + cleared + '. Reload page to see modals.');
})();
        """.trimIndent()
    }

    /**
     * Скрипт спуфинга Device-Id
     */
    fun generateDeviceIdSpoofScript(): String {
        return """
(function() {
    var id = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = Math.random() * 16 | 0;
        return (c == 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
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
        return """
(function() {
    var fake = {
        capture: function(){}, identify: function(){}, alias: function(){},
        people: { set: function(){} }, register: function(){},
        opt_out_capturing: function(){}, has_opted_out_capturing: function() { return true; },
        reset: function(){}, get_distinct_id: function() { return 'anon'; },
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
     * Скрипт региональных скидок
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
    
    localStorage.removeItem('hasSeenFreeWeekendTurkeyModal');
    localStorage.removeItem('hasSeenFreeWeekendCanadaModal');
    
    var origDTF = Intl.DateTimeFormat;
    Intl.DateTimeFormat = function(locale, options) {
        options = options || {};
        options.timeZone = '$tz';
        return new origDTF(locale || '$lang', options);
    };
    Intl.DateTimeFormat.prototype = origDTF.prototype;
    
    Object.defineProperty(navigator, 'language', { get: function() { return '$lang'; } });
    Object.defineProperty(navigator, 'languages', { get: function() { return ['$lang']; } });
    
    console.log('[AntiF] $region applied. Reload page.');
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
        return k.startsWith('hasSeen') || k.includes('modal') || k.includes('emergent') || k.includes('openclaw');
    });
    keys.forEach(function(k) { console.log(k + ' = ' + localStorage.getItem(k)); });
    console.log('[AntiF] Total Emergent keys: ' + keys.length);
    return keys;
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
     * Show full Emergent config
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
    return all;
})();
        """.trimIndent()
    }

    /**
     * Spoof Supabase ANON KEY
     */
    fun generateSupabaseSpoofScript(): String {
        return """
(function() {
    console.log('[AntiF] Supabase ANON KEY spoof active');
    var origFetch = window.fetch;
    window.fetch = function(url, opts) {
        var urlStr = typeof url === 'string' ? url : (url && url.url) || '';
        if (urlStr.includes('supabase')) {
            opts = opts || {};
            opts.headers = opts.headers || {};
            if (opts.headers instanceof Headers) {
                opts.headers.set('apikey', 'spoofed-anon-key');
            } else {
                opts.headers['apikey'] = 'spoofed-anon-key';
            }
        }
        return origFetch.apply(this, arguments);
    };
    console.log('[AntiF] Supabase requests will use spoofed key');
})();
        """.trimIndent()
    }

    // Aliases for compatibility
    fun generateFullResetModalsScript(): String = generateResetModalsScript()
    fun generateFullCleanupScript(): String = generateTrackingBlockerScript()

    /**
     * Скрипт определения использования триалки (Trial Detection)
     * Проверяет localStorage, cookies, sessionStorage на признаки trial
     */
    fun generateTrialDetectionScript(): String {
        return """
(function() {
    'use strict';
    console.log('[AntiF] === TRIAL DETECTION v1.0 ===');
    
    var trialIndicators = {
        found: false,
        details: [],
        localStorage: [],
        sessionStorage: [],
        cookies: [],
        globalVars: []
    };
    
    // ═══════════════════════════════════════════════════════════════════════
    // 1. CHECK LOCALSTORAGE FOR TRIAL INDICATORS
    // ═══════════════════════════════════════════════════════════════════════
    var trialLocalStorageKeys = [
        'trial', 'free_trial', 'freeTrial', 'trial_used', 'trialUsed',
        'trial_started', 'trialStarted', 'trial_end', 'trialEnd',
        'trial_expiry', 'trialExpiry', 'trial_expired', 'trialExpired',
        'has_trial', 'hasTrial', 'used_trial', 'usedTrial',
        'trial_credits', 'trialCredits', 'free_credits', 'freeCredits',
        'trial_active', 'trialActive', 'is_trial', 'isTrial',
        'subscription', 'plan', 'tier', 'credits', 'balance',
        'hasSeenFreeCredits', 'hasSeenOneDollar', 'hasSeenFreeStandardPlan',
        'hasSeenFreeTrial', 'emergent_trial', 'emergent_free',
        'first_use', 'firstUse', 'signup_date', 'signupDate',
        'account_created', 'accountCreated', 'user_tier', 'userTier'
    ];
    
    for (var i = 0; i < localStorage.length; i++) {
        var key = localStorage.key(i);
        var value = localStorage.getItem(key);
        var keyLower = key.toLowerCase();
        
        // Check if key contains trial-related words
        var isTrialKey = trialLocalStorageKeys.some(function(tk) {
            return keyLower.includes(tk.toLowerCase());
        }) || keyLower.includes('trial') || keyLower.includes('free') || 
           keyLower.includes('credit') || keyLower.includes('plan') ||
           keyLower.includes('subscription') || keyLower.includes('tier');
        
        if (isTrialKey) {
            trialIndicators.localStorage.push({key: key, value: value});
            
            // Parse value to detect trial usage
            try {
                var parsed = JSON.parse(value);
                if (parsed && (parsed.trial === true || parsed.used === true || 
                    parsed.active === true || parsed.expired === true)) {
                    trialIndicators.found = true;
                    trialIndicators.details.push('localStorage: ' + key + ' indicates trial usage');
                }
            } catch(e) {
                // Not JSON, check string value
                if (value === 'true' || value === '1' || value === 'used' || 
                    value === 'expired' || value === 'active') {
                    trialIndicators.found = true;
                    trialIndicators.details.push('localStorage: ' + key + '=' + value);
                }
            }
        }
        
        // Check for hasSeen* keys (Emergent specific)
        if (keyLower.startsWith('hasseen')) {
            trialIndicators.localStorage.push({key: key, value: value});
            if (value === 'true' || value === '1') {
                trialIndicators.found = true;
                trialIndicators.details.push('Modal seen: ' + key);
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // 2. CHECK SESSIONSTORAGE
    // ═══════════════════════════════════════════════════════════════════════
    for (var j = 0; j < sessionStorage.length; j++) {
        var sKey = sessionStorage.key(j);
        var sValue = sessionStorage.getItem(sKey);
        var sKeyLower = sKey.toLowerCase();
        
        if (sKeyLower.includes('trial') || sKeyLower.includes('free') || 
            sKeyLower.includes('credit') || sKeyLower.includes('plan')) {
            trialIndicators.sessionStorage.push({key: sKey, value: sValue});
            trialIndicators.found = true;
            trialIndicators.details.push('sessionStorage: ' + sKey);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // 3. CHECK COOKIES
    // ═══════════════════════════════════════════════════════════════════════
    var cookies = document.cookie.split(';');
    for (var c = 0; c < cookies.length; c++) {
        var cookie = cookies[c].trim();
        var cookieName = cookie.split('=')[0].toLowerCase();
        var cookieValue = cookie.split('=')[1] || '';
        
        if (cookieName.includes('trial') || cookieName.includes('free') ||
            cookieName.includes('credit') || cookieName.includes('plan') ||
            cookieName.includes('tier') || cookieName.includes('subscription')) {
            trialIndicators.cookies.push({name: cookieName, value: cookieValue});
            trialIndicators.found = true;
            trialIndicators.details.push('Cookie: ' + cookieName);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // 4. CHECK GLOBAL WINDOW VARIABLES (Emergent specific)
    // ═══════════════════════════════════════════════════════════════════════
    var globalChecks = ['__TRIAL__', '__FREE__', '__PLAN__', '__USER__', '__CREDITS__', 
                        'userPlan', 'userTier', 'trialStatus', 'freeCredits'];
    globalChecks.forEach(function(varName) {
        if (typeof window[varName] !== 'undefined') {
            trialIndicators.globalVars.push({name: varName, value: String(window[varName])});
            trialIndicators.found = true;
            trialIndicators.details.push('Global var: ' + varName);
        }
    });
    
    // ═══════════════════════════════════════════════════════════════════════
    // 5. CHECK SUPABASE AUTH DATA (Emergent uses Supabase)
    // ═══════════════════════════════════════════════════════════════════════
    Object.keys(localStorage).forEach(function(key) {
        if (key.startsWith('sb-') && key.includes('auth-token')) {
            try {
                var authData = JSON.parse(localStorage.getItem(key));
                if (authData && authData.user) {
                    var user = authData.user;
                    var meta = user.user_metadata || {};
                    var appMeta = user.app_metadata || {};
                    
                    trialIndicators.details.push('User ID: ' + (user.id || 'unknown'));
                    trialIndicators.details.push('Email: ' + (user.email || 'unknown'));
                    trialIndicators.details.push('Created: ' + (user.created_at || 'unknown'));
                    
                    if (meta.plan || meta.tier || meta.trial) {
                        trialIndicators.found = true;
                        trialIndicators.details.push('User meta: plan=' + meta.plan + ', tier=' + meta.tier);
                    }
                    if (appMeta.plan || appMeta.subscription) {
                        trialIndicators.found = true;
                        trialIndicators.details.push('App meta: ' + JSON.stringify(appMeta));
                    }
                }
            } catch(e) {}
        }
    });
    
    // ═══════════════════════════════════════════════════════════════════════
    // 6. RESULT
    // ═══════════════════════════════════════════════════════════════════════
    console.log('[AntiF] Trial Detection Result:');
    console.log('[AntiF] TRIAL USED: ' + (trialIndicators.found ? 'YES' : 'NO'));
    console.log('[AntiF] Details: ' + JSON.stringify(trialIndicators.details, null, 2));
    console.log('[AntiF] LocalStorage keys found: ' + trialIndicators.localStorage.length);
    console.log('[AntiF] SessionStorage keys found: ' + trialIndicators.sessionStorage.length);
    console.log('[AntiF] Cookies found: ' + trialIndicators.cookies.length);
    
    // Store result globally
    window._antifTrialDetection = trialIndicators;
    
    // Return summary
    var summary = {
        trialUsed: trialIndicators.found,
        indicators: trialIndicators.details.length,
        localStorageKeys: trialIndicators.localStorage.length,
        sessionStorageKeys: trialIndicators.sessionStorage.length,
        cookies: trialIndicators.cookies.length,
        message: trialIndicators.found ? 
            'TRIAL WAS USED! Found ' + trialIndicators.details.length + ' indicators.' :
            'No trial usage detected.'
    };
    
    console.log('[AntiF] === SUMMARY ===');
    console.log('[AntiF] ' + summary.message);
    
    return JSON.stringify(summary);
})();
        """.trimIndent()
    }

    /**
     * Скрипт сброса триала (Trial Reset)
     * Удаляет все следы использования триала
     */
    fun generateTrialResetScript(): String {
        return """
(function() {
    'use strict';
    console.log('[AntiF] === TRIAL RESET v1.0 ===');
    
    var removed = {
        localStorage: 0,
        sessionStorage: 0,
        cookies: 0
    };
    
    // 1. Remove trial-related localStorage
    var keysToRemove = [];
    for (var i = 0; i < localStorage.length; i++) {
        var key = localStorage.key(i);
        var keyLower = key.toLowerCase();
        if (keyLower.includes('trial') || keyLower.includes('hasseen') ||
            keyLower.includes('credit') || keyLower.includes('plan') ||
            keyLower.includes('modal') || keyLower.includes('dismiss') ||
            keyLower.includes('free') || keyLower.includes('tier') ||
            keyLower.includes('subscription') || keyLower.includes('onboarding')) {
            keysToRemove.push(key);
        }
    }
    keysToRemove.forEach(function(k) {
        // Don't remove auth tokens
        if (!k.startsWith('sb-') && !k.includes('auth') && !k.includes('token')) {
            localStorage.removeItem(k);
            removed.localStorage++;
            console.log('[AntiF] Removed localStorage: ' + k);
        }
    });
    
    // 2. Remove trial-related sessionStorage
    var sKeysToRemove = [];
    for (var j = 0; j < sessionStorage.length; j++) {
        var sKey = sessionStorage.key(j);
        var sKeyLower = sKey.toLowerCase();
        if (sKeyLower.includes('trial') || sKeyLower.includes('free') ||
            sKeyLower.includes('credit') || sKeyLower.includes('modal')) {
            sKeysToRemove.push(sKey);
        }
    }
    sKeysToRemove.forEach(function(sk) {
        sessionStorage.removeItem(sk);
        removed.sessionStorage++;
        console.log('[AntiF] Removed sessionStorage: ' + sk);
    });
    
    // 3. Remove trial-related cookies
    var cookies = document.cookie.split(';');
    cookies.forEach(function(cookie) {
        var name = cookie.split('=')[0].trim();
        var nameLower = name.toLowerCase();
        if (nameLower.includes('trial') || nameLower.includes('free') ||
            nameLower.includes('credit') || nameLower.includes('plan')) {
            document.cookie = name + '=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/';
            document.cookie = name + '=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/;domain=.' + window.location.hostname;
            removed.cookies++;
            console.log('[AntiF] Removed cookie: ' + name);
        }
    });
    
    console.log('[AntiF] === TRIAL RESET COMPLETE ===');
    console.log('[AntiF] Removed localStorage: ' + removed.localStorage);
    console.log('[AntiF] Removed sessionStorage: ' + removed.sessionStorage);
    console.log('[AntiF] Removed cookies: ' + removed.cookies);
    console.log('[AntiF] Total removed: ' + (removed.localStorage + removed.sessionStorage + removed.cookies));
    console.log('[AntiF] Reload page to apply changes.');
    
    return JSON.stringify({
        success: true,
        removed: removed,
        total: removed.localStorage + removed.sessionStorage + removed.cookies,
        message: 'Trial data reset. Reload page.'
    });
})();
        """.trimIndent()
    }
}
