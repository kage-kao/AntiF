package com.antif.browser.core

/**
 * EmergentBypass - ПОЛНЫЙ обход защит Emergent.sh
 * Основано на reverse engineering анализе bundle 4.4 MB (Март 2026)
 * 
 * Включает ВСЕ методы из EMERGENT_FULL_ANALYSIS_COMPLETE.txt:
 * - Раздел 1: FingerprintJS Pro
 * - Раздел 2: Device-Id Header
 * - Раздел 4-5: localStorage/sessionStorage
 * - Раздел 6: Cookies
 * - Раздел 7: PostHog Analytics
 * - Раздел 8: FB/TikTok/Taboola Pixels
 * - Раздел 9: Rewardful
 * - Раздел 10: Regional Discounts
 * - Раздел 11: Supabase
 * - Раздел 12: Paddle
 * - Раздел 13: Feature Flags
 * - Раздел 14: API Endpoints
 * - Раздел 15: Reset Functions
 * - Раздел 16: Credits System
 * - Раздел 17: Cloudflare
 */
object EmergentBypass {

    // ══════════════════════════════════════════════════════════════════════════════
    // РАЗДЕЛ 1: FINGERPRINTJS PRO КОНФИГУРАЦИЯ
    // ══════════════════════════════════════════════════════════════════════════════
    object FingerprintJSConfig {
        const val API_KEY = "bjLV9OY0jn9FyljSQFMn"
        const val ENDPOINT = "https://app.emergent.sh/yYu6hLHLpQEW2ELz/BJnqq7CRgAe18QLk"
        const val VERSION = "FingerprintJS Pro React v3.0.0"
        const val LICENSE = "Copyright (c) FingerprintJS, fingerprint.com"
        
        // Все 55 параметров которые собирает FingerprintJS
        val collectedParams = listOf(
            // Браузер и система
            "userAgent", "platform", "vendor", "language", "languages",
            "timezone", "timezoneOffset", "cookiesEnabled", "doNotTrack",
            // Экран
            "screen.width", "screen.height", "screen.availWidth", "screen.availHeight",
            "screen.colorDepth", "screen.pixelDepth", "devicePixelRatio",
            "resolution", "availableResolution", "colorGamut", "contrast",
            // Hardware
            "hardwareConcurrency", "deviceMemory", "cpuClass", "oscpu",
            // Canvas
            "canvas",
            // WebGL
            "webgl.vendor", "webgl.renderer", "webgl.version",
            "webgl.shadingLanguageVersion", "webgl.extensions", "webgl.parameters",
            // Audio
            "audio",
            // Шрифты
            "fonts",
            // Плагины
            "plugins",
            // Хранилища
            "localStorage", "sessionStorage", "indexedDB", "openDatabase",
            // Accessibility
            "reducedMotion", "hdr", "invertedColors", "forcedColors", "monochrome",
            // Математика
            "math",
            // Touch
            "touchSupport", "pointerType",
            // Детекция автоматизации
            "webdriver", "hasLiedBrowser", "hasLiedOs", "hasLiedLanguages", "hasLiedResolution",
            // Дополнительные
            "adBlock", "incognito", "vendorFlavors", "cookieFlags"
        )
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // РАЗДЕЛ 7: POSTHOG КОНФИГУРАЦИЯ
    // ══════════════════════════════════════════════════════════════════════════════
    object PostHogConfig {
        const val API_KEY = "phc_xAvL2Iq4tFmANRE7kzbKwaSqp1HJjN7x48s3vr0CMjs"
        const val API_HOST = "https://us.i.posthog.com"
        const val PERSISTENCE = "localStorage+cookie"
        const val COOKIE_EXPIRY = 365 // дней
        const val CROSS_SUBDOMAIN = true
        const val SESSION_RECORDING_ENABLED = true
        const val SAMPLE_RATE = 0.05 // 5%
        const val MIN_DURATION = 30000 // 30 секунд
        const val CONSOLE_LOG_RECORDING = true
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // РАЗДЕЛ 11: SUPABASE КОНФИГУРАЦИЯ
    // ══════════════════════════════════════════════════════════════════════════════
    object SupabaseConfig {
        const val PUBLIC_URL = "https://snksxwkyumhdykyrhhch.supabase.co"
        const val AUTH_URL = "https://auth.emergent.sh"
        // ANON KEY действителен до 2040!
        const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNua3N4d2t5dW1oZHlreXJoaGNoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjQ3NzI2NDYsImV4cCI6MjA0MDM0ODY0Nn0.3unO6zdz2NilPL2xdxt7OjvZA19copj3Q7ulIjPVDLQ"
        
        // User metadata поля
        val userMetadataFields = listOf(
            "full_name", "name", "avatar_url", "email_verified",
            "invite_code", "utm_source", "utm_campaign", "utm_medium",
            "utm_content", "utm_term", "user_referrer"
        )
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // РАЗДЕЛ 12: PADDLE КОНФИГУРАЦИЯ
    // ══════════════════════════════════════════════════════════════════════════════
    object PaddleConfig {
        const val CLIENT_TOKEN = "live_b30093d99d7846644b2e678861f"
        const val SDK_URL = "https://cdn.paddle.com/paddle/v2/paddle.js"
        const val ENVIRONMENT = "production"
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // РАЗДЕЛ 16: CREDITS SYSTEM
    // ══════════════════════════════════════════════════════════════════════════════
    object CreditsConfig {
        const val DAILY_CREDITS = 10.0
        const val MONTHLY_CREDITS = 5.0
        const val DAILY_CREDIT_PER_DAY = 10.0
        const val DAILY_CREDIT_MAX_PER_WINDOW = 10.0
        const val TOTAL_FREE_CREDITS = 15.0 // daily + monthly
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // РАЗДЕЛ 13: FEATURE FLAGS
    // ══════════════════════════════════════════════════════════════════════════════
    val featureFlags = mapOf(
        // Включенные features
        "bundle_id_edit_enabled" to true,
        "dynamo_litmus_enabled" to true,
        "enterprise_plan_enabled" to true,
        "fingerprint_skipped_pass_id_enabled" to true,
        "ios_builds_enabled" to true,
        "mobile_preview_enabled" to true,
        "perceived_ux_enabled" to true,
        "phone_auth_enabled" to true,
        "test_mode_enabled" to true,
        "ultra_mode_enabled" to true,
        "visual_edit_enabled" to true,
        // Проверяемые флаги
        "100m_hoarding" to false,
        "preview_panel_exp" to false,
        "quick_topup" to true,
        "quick_upgrade" to true,
        "show_auto_topup" to true,
        "show_prompting_modal" to false,
        "summarize_label" to true,
        "tips_trigger_change" to false,
        "trajectory_history" to true
    )

    // ══════════════════════════════════════════════════════════════════════════════
    // РАЗДЕЛ 14: API ENDPOINTS
    // ══════════════════════════════════════════════════════════════════════════════
    val apiEndpoints = listOf(
        // Chat & Tasks
        "/api/chat", "/api/v0/submit", "/api/v0/stream",
        // Deployment
        "/api/v0/deploy", "/api/v0/deploy/tiers", "/api/v0/deployments", "/api/v0/rollback/cloud/",
        // Domains
        "/api/v0/domains/", "/api/v0/domains/connect", "/api/v0/domains/register",
        "/api/v0/domains/status", "/api/v0/domains/verify", "/api/v1/domains/",
        // iOS & Android
        "/api/v0/ios", "/api/v0/ios/build", "/api/v0/ios/apple/login",
        "/api/v0/ios/apple/2fa", "/api/v0/android",
        // GitHub
        "/api/v0/push_to_github/",
        // Checkout
        "/api/v1/checkout", "/api/v2/paddle",
        // Coupons
        "/coupons/generate",
        // Other
        "/api/v0/budget", "/api/v0/budget/increase", "/api/v0/chat/preview",
        "/api/v0/diff/", "/api/v0/entri/info", "/api/v0/hitl",
        "/api/v0/ref/", "/api/v0/tim/", "/api/v1/apps/live-logs", "/api/v1/files/stream",
        // Early Access
        "/api/early_access_features/", "/api/surveys/", "/api/web_experiments/"
    )

    // ══════════════════════════════════════════════════════════════════════════════
    // РАЗДЕЛ 7: POSTHOG СОБЫТИЯ (500+)
    // ══════════════════════════════════════════════════════════════════════════════
    val postHogEvents = listOf(
        // Системные
        "\$pageview", "\$pageleave", "\$autocapture", "\$identify", "\$set",
        "\$set_once_init", "\$create_alias", "\$groupidentify", "\$feature_flag_called",
        "\$feature_enrollment_update", "\$exception", "\$web_vitals",
        "\$ai_feedback", "\$ai_metric", "\$\$heatmap", "\$\$client_ingestion_warning",
        // Аутентификация
        "login_attempt", "login_success", "login_failure",
        "signup_attempt", "signup_success", "signup_failure",
        "first_login_completed", "logout",
        // Подписка и платежи
        "subscription_payment_success", "subscription_payment_failed",
        "subscription_payment_cancelled", "subscription_success",
        "plan_upgrade_button_clicked", "tier_upgrade_button_clicked",
        "buy_credits_button_clicked", "buy_credits_clicked",
        "credit_bundle_purchase_clicked", "credit_bundle_selected",
        "credit_topup_payment_success", "credit_topup_payment_failed",
        "credit_topup_payment_cancelled",
        // Trial
        "trial_modal_cta_clicked", "trial_modal_dismissed",
        "trial_modal_explore_plans", "free_credits_modal", "first_time_offer",
        // Купоны
        "coupon_applied", "coupon_apply_failed", "coupon_removed",
        "coupon_input_shown", "coupon_verify_error", "coupon_generate_error",
        // Fingerprint
        "fingerprint_triggered", "fingerprint_skipped_pass_id_enabled",
        "fingerprint_sdk_call_error", "fingerprint_sync_error",
        "fingerprint_identify_api_error", "fingerprint_trigger_error",
        // Модалки
        "modal_opened", "modal_closed", "buy_credits_modal_opened", "buy_credits_modal_closed",
        "upgrade_tier_modal_closed", "commitment_free_plan_modal_cta_clicked",
        "commitment_free_plan_modal_dismissed", "commitment_free_plan_modal_shown",
        "free_pro_plan_modal_dismissed", "annual_upgrade_modal_dismissed",
        "bonus_credits_offer_modal_shown", "bonus_credits_offer_modal_dismissed",
        "first_topup_discount_modal_shown", "first_topup_discount_modal_dismissed",
        "topup_offer_modal_shown", "topup_offer_modal_dismissed",
        // Deployment
        "deployment_success_modal_shown", "deployment_footer_start_deployment_clicked",
        "deployment_footer_redeploy_clicked", "deployment_footer_shutdown_clicked",
        "deployment_failure_fix_with_agent_clicked", "deployment_failure_logs_clicked",
        "deployment_live_fix_with_agent_clicked", "deployment_live_logs_clicked",
        "deployment_live_contact_support_clicked", "deployment_recording_started",
        "deployment_rollback_clicked",
        // Проекты
        "job_created", "job_deleted", "job_name_updated", "first_job_created",
        "create_new_project_clicked", "create_project_modal_opened",
        "create_project_submit_clicked", "project_settings_clicked", "project_switch_clicked"
    )

    // ══════════════════════════════════════════════════════════════════════════════
    // РАЗДЕЛ 4: LOCALSTORAGE КЛЮЧИ (50+)
    // ══════════════════════════════════════════════════════════════════════════════
    val localStorageKeys = listOf(
        // Идентификация и Trial
        "first_login_completed", "first_job_timestamp", "fp_dedup", "pending_fingerprint_",
        // Промо
        "promoCouponCode", "promoCouponError", "africanPromoCouponCode", "africanPromoCouponError",
        // UTM
        "utm_source", "utm_medium", "utm_campaign", "utm_content", "utm_term",
        "user_referrer", "referral_code", "referral_modal_data",
        // Настройки
        "userSelectedLanguage", "preferred_currency", "i18nextLng", "pro_mode_status", "landingPageTask",
        // Модалки
        "hasSeenFreeCreditsModal", "hasSeenOneDollarModal", "hasSeenFreeStandardPlanModal",
        "hasSeenFreeWeekendTurkeyModal", "hasSeenFreeWeekendCanadaModal",
        "hasSeenOpusModal", "hasSeenOpus46Modal", "hasSeenGFFINModal",
        "hasSeenValentineCreditsModalV2", "hasSeenFirstTopupDiscountModal",
        "hasSeenChristmasGiftingModal", "hasSeenWarmupMailGiftingModal",
        "hasSeenMCPIntroductionModal", "hasSeenBroModeIntroductionModal",
        "hasSeenUltraThinkingModal", "hasSeenTestModeModal", "hasSeenVisualEditsModalNew",
        "hasSeenPromptingPromoModal", "hasSeenUniversalKeyIntroTooltip",
        "hasSeenUniversalKeyIntroModal", "hasSeenProjectOnboardingTour",
        "hasSeenFeatureModal", "hasSeenForkIntro", "hasSeenMobileAgentIntro",
        "hasSeenProIntroductionModal", "hasSeenStandardDiscountModal",
        "hasSeenLockinPriceModal", "hasSeenAnnualUpgradeModal",
        "hasSeenBonusCreditsOfferModal", "hasSeenTopupOfferModal",
        "hasSeenAutoTopupIntroModal", "hasSeenClaude45AnnouncementModal",
        "hasSeenPaddleEuropeModal", "hasSeenPagBrasilModal",
        "hasSeenInfiniteChatPaywallModal", "hasSeenTestModeOnboarding",
        "hasSeenOpus46Nudge", "hasSeenFreeProPlanPromoModal", "hasSeenGFFModal",
        // Состояние
        "hasCreatedFirstProject", "commitment_free_checkout", "free_user_deploy_attempted",
        "buy_credits_button_clicked", "mobile_app_promo", "cancel_subscription_reason",
        "cancel_subscription_variant", "rollback_retry_attempt_job_id_map",
        "postLoginReturnUrl", "pending_invitation", "giftingDetails", "showThankYou",
        "embeddedTaskTabId", "lastSeenSurveyDate", "apiBaseUrl", "openclaw-modal-dismiss"
    )

    // ══════════════════════════════════════════════════════════════════════════════
    // РАЗДЕЛ 5: SESSIONSTORAGE КЛЮЧИ
    // ══════════════════════════════════════════════════════════════════════════════
    val sessionStorageKeys = listOf(
        "persist:tabs", "oauth_login_pending", "oauth_provider",
        "payment_details_", "meta_checkout_", "pixel_event_",
        "fb_pixel_event_", "tiktok_event_"
    )

    // ══════════════════════════════════════════════════════════════════════════════
    // РАЗДЕЛ 6: COOKIES
    // ══════════════════════════════════════════════════════════════════════════════
    val cookies = listOf(
        // Cloudflare
        "__cf_bm", "__cfduid",
        // Facebook
        "_fbc", "_fbp",
        // UTM
        "utm_source", "utm_medium", "utm_campaign", "utm_content", "utm_term",
        // Рекламные ID
        "gclid", "fbclid", "msclkid", "ttclid", "twclid", "li_fat_id", "igshid",
        // Партнеры
        "ps_partner_key", "rewardful_affiliate", "via", "ref", "referral", "affiliate", "action",
        // PostHog
        "ph_", "\$sesid", "\$device_id", "\$distinct_id",
        "\$enabled_feature_flags", "\$feature_flag_payloads", "posthog_cookieless"
    )

    // ══════════════════════════════════════════════════════════════════════════════
    // РАЗДЕЛ 8: TRACKING ДОМЕНЫ (ТОЛЬКО РЕАЛЬНЫЙ TRACKING, НЕ ЛОМАЮЩИЙ САЙТ)
    // ══════════════════════════════════════════════════════════════════════════════
    val trackingDomains = listOf(
        // FingerprintJS Pro - BLOCK (fingerprinting)
        "fpjs.io", "fpnpmcdn.net", "fpcdn.io", "api.fpjs.io", "cdn.fpjs.io",
        "fingerprint.com", "botd.fpjs.io",
        // Emergent FingerprintJS endpoint - BLOCK
        "app.emergent.sh/yYu6hLHLpQEW2ELz",
        // PostHog - BLOCK (analytics)
        "us.i.posthog.com", "us-assets.i.posthog.com", "app.posthog.com",
        "eu.posthog.com", "eu.i.posthog.com",
        // Facebook Pixel - BLOCK
        "connect.facebook.net", "facebook.com/tr",
        // TikTok - BLOCK
        "analytics.tiktok.com",
        // Taboola - BLOCK
        "cds.taboola.com", "trc.taboola.com",
        // Rewardful - BLOCK (affiliate tracking)
        "r.wdfl.co"
    )
    
    // Домены для ПОДМЕНЫ данных (не блокировка, а спуфинг)
    val spoofDomains = listOf(
        "ap.emergent.sh"  // Перехватываем и подменяем device-id в запросах
    )

    // ══════════════════════════════════════════════════════════════════════════════
    // РАЗДЕЛ 10: РЕГИОНАЛЬНЫЕ СКИДКИ
    // ══════════════════════════════════════════════════════════════════════════════
    data class RegionalDiscount(
        val countryCode: String,
        val countryName: String,
        val discount: String,
        val modal: String,
        val timezone: String,
        val language: String,
        val paymentSystem: String
    )

    val regionalDiscounts = listOf(
        RegionalDiscount("TR", "Turkey", "100% (FREE WEEKEND)", "FreeWeekendTurkeyModal", "Europe/Istanbul", "tr-TR", "Paddle"),
        RegionalDiscount("CA", "Canada", "100% (FREE WEEKEND)", "FreeWeekendCanadaModal", "America/Toronto", "en-CA", "Paddle")
    )

    // ══════════════════════════════════════════════════════════════════════════════
    // РАЗДЕЛ 15: RESET FUNCTIONS (50+)
    // ══════════════════════════════════════════════════════════════════════════════
    val resetFunctions = listOf(
        // Главная
        "resetAllModals",
        // Подписка и кредиты
        "resetFreeCreditsModal", "resetOneDollarModal", "resetFreeStandardPlanModal",
        "resetFirstTopupDiscountModal", "resetBuyCreditsAutoPopup", "resetLockinPriceModal",
        // Региональные
        "resetFreeWeekendTurkeyModal", "resetFreeWeekendCanadaModal",
        // Промо
        "resetPromotionalModal", "resetValentineCreditsModal", "resetChristmasGiftingModal",
        "resetWarmupMailGiftingModal", "resetPromptingPromoModal", "resetReactivationPromoModal",
        "resetGFFINModal", "resetGFFModal",
        // Feature
        "resetFeatureModal", "resetProIntroductionModal", "resetStandardDiscountModal",
        "resetOpusModal", "resetOpus46Modal", "resetUltraThinkingModal",
        "resetMCPIntroductionModal", "resetBroModeIntroductionModal", "resetTestModeModal",
        "resetVisualEditsModal", "resetUniversalKeyIntroTooltip", "resetModelSelectorTooltip",
        "resetFundingIntroductionModal", "resetBetaProgram",
        // Onboarding
        "resetProjectOnboardingTour", "resetFirstProjectCreated", "resetForkIntro",
        "resetMobileAgentIntro", "resetOneMillionModal",
        // Storage
        "clearStorageSafely", "clearAllStorageSafely", "preserveTrackingData", "restoreTrackingData"
    )

    // ══════════════════════════════════════════════════════════════════════════════
    // СКРИПТЫ ОБХОДА
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * ПОЛНЫЙ СКРИПТ ОБХОДА - ВСЕ МЕТОДЫ ВМЕСТЕ
     */
    fun generateFullBypassScript(): String {
        return """
(function() {
    'use strict';
    console.log('%c╔═══════════════════════════════════════════════════════════╗', 'color: #ff0000; font-weight: bold;');
    console.log('%c║  ANTIF FULL EMERGENT BYPASS v2.0 - ALL METHODS ACTIVE    ║', 'color: #ff0000; font-weight: bold;');
    console.log('%c╚═══════════════════════════════════════════════════════════╝', 'color: #ff0000; font-weight: bold;');

    // ═══════════════════════════════════════════════════════════════════════
    // 1. TRACKING BLOCKER (FingerprintJS, PostHog, FB, TikTok, Taboola)
    // ═══════════════════════════════════════════════════════════════════════
    var blockedDomains = ${trackingDomains.joinToString(",", "[", "]") { "\"$it\"" }};
    var blockedCount = 0;
    
    function isBlocked(url) {
        if (!url) return false;
        var lowerUrl = url.toLowerCase();
        for (var i = 0; i < blockedDomains.length; i++) {
            if (lowerUrl.indexOf(blockedDomains[i]) !== -1) return true;
        }
        return false;
    }
    
    var origFetch = window.fetch;
    window.fetch = function(url, opts) {
        var urlStr = (typeof url === 'string') ? url : (url && url.url) || '';
        
        // BLOCK tracking domains completely
        if (isBlocked(urlStr)) {
            blockedCount++;
            console.log('%c[BLOCKED] ' + urlStr, 'color: #ff4444;');
            return Promise.resolve(new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } }));
        }
        
        // SPOOF ap.emergent.sh requests - let them through but with fake device-id
        if (urlStr.includes('ap.emergent.sh')) {
            console.log('%c[SPOOF] ap.emergent.sh request intercepted', 'color: #ffaa00;');
            // Modify headers to send fake device-id
            opts = opts || {};
            opts.headers = opts.headers || {};
            if (opts.headers instanceof Headers) {
                opts.headers.set('X-Device-Id', window._antifDeviceId || 'spoof-' + Math.random().toString(36).substr(2,16));
            } else {
                opts.headers['X-Device-Id'] = window._antifDeviceId || 'spoof-' + Math.random().toString(36).substr(2,16);
            }
            // Modify body if it contains device identifiers
            if (opts.body && typeof opts.body === 'string') {
                try {
                    var bodyObj = JSON.parse(opts.body);
                    if (bodyObj.distinct_id) bodyObj.distinct_id = window._antifDeviceId;
                    if (bodyObj.device_id) bodyObj.device_id = window._antifDeviceId;
                    if (bodyObj['${'$'}device_id']) bodyObj['${'$'}device_id'] = window._antifDeviceId;
                    if (bodyObj.properties) {
                        bodyObj.properties['${'$'}device_id'] = window._antifDeviceId;
                        bodyObj.properties.distinct_id = window._antifDeviceId;
                    }
                    opts.body = JSON.stringify(bodyObj);
                } catch(e) {}
            }
        }
        
        return origFetch.apply(this, arguments);
    };
    
    var origXHROpen = XMLHttpRequest.prototype.open;
    var origXHRSend = XMLHttpRequest.prototype.send;
    var origXHRSetHeader = XMLHttpRequest.prototype.setRequestHeader;
    
    XMLHttpRequest.prototype.open = function(method, url) {
        this._antifUrl = url;
        this._antifBlocked = isBlocked(url);
        this._antifSpoof = url && url.includes && url.includes('ap.emergent.sh');
        if (this._antifBlocked) { blockedCount++; console.log('%c[XHR BLOCKED] ' + url, 'color: #ff4444;'); }
        if (this._antifSpoof) { console.log('%c[XHR SPOOF] ' + url, 'color: #ffaa00;'); }
        return origXHROpen.apply(this, arguments);
    };
    
    XMLHttpRequest.prototype.setRequestHeader = function(name, value) {
        // Spoof device-id headers for ap.emergent.sh
        if (this._antifSpoof && (name.toLowerCase() === 'x-device-id' || name.toLowerCase() === 'device-id')) {
            value = window._antifDeviceId || 'spoof-' + Math.random().toString(36).substr(2,16);
        }
        return origXHRSetHeader.apply(this, arguments);
    };
    
    XMLHttpRequest.prototype.send = function(body) {
        if (this._antifBlocked) {
            // Fake successful response instead of blocking
            var self = this;
            setTimeout(function() {
                Object.defineProperty(self, 'readyState', { value: 4 });
                Object.defineProperty(self, 'status', { value: 200 });
                Object.defineProperty(self, 'responseText', { value: '{}' });
                if (self.onreadystatechange) self.onreadystatechange();
                if (self.onload) self.onload();
            }, 0);
            return;
        }
        
        // Spoof body data for ap.emergent.sh
        if (this._antifSpoof && body && typeof body === 'string') {
            try {
                var bodyObj = JSON.parse(body);
                if (bodyObj.distinct_id) bodyObj.distinct_id = window._antifDeviceId;
                if (bodyObj.device_id) bodyObj.device_id = window._antifDeviceId;
                if (bodyObj['${'$'}device_id']) bodyObj['${'$'}device_id'] = window._antifDeviceId;
                body = JSON.stringify(bodyObj);
            } catch(e) {}
        }
        
        return origXHRSend.call(this, body);
    };
    
    var origBeacon = navigator.sendBeacon;
    if (origBeacon) {
        navigator.sendBeacon = function(url) {
            if (isBlocked(url)) { blockedCount++; return false; }
            return origBeacon.apply(this, arguments);
        };
    }
    
    console.log('[AntiF] Tracking blocker: ' + blockedDomains.length + ' domains');

    // ═══════════════════════════════════════════════════════════════════════
    // 2. POSTHOG NEUTRALIZE (FIXED - no redefine error)
    // ═══════════════════════════════════════════════════════════════════════
    var fakePostHog = {
        capture: function(e) { console.log('[AntiF] PostHog blocked: ' + e); },
        identify: function() {}, alias: function() {}, people: { set: function() {} },
        register: function() {}, register_once: function() {}, unregister: function() {},
        opt_out_capturing: function() {}, opt_in_capturing: function() {},
        has_opted_out_capturing: function() { return true; },
        has_opted_in_capturing: function() { return false; },
        reset: function() {}, get_distinct_id: function() { return 'blocked-' + Math.random().toString(36).substr(2,9); },
        getFeatureFlag: function() { return undefined; },
        isFeatureEnabled: function() { return false; },
        reloadFeatureFlags: function() {}, onFeatureFlags: function() {},
        setPersonProperties: function() {}, group: function() {},
        init: function() { return fakePostHog; }, __loaded: true,
        _i: [], push: function() {}, __SV: 1.0
    };
    
    // Safe PostHog replacement (avoid Cannot redefine property error)
    try {
        if (typeof window.posthog === 'undefined') {
            window.posthog = fakePostHog;
        } else {
            // Already defined - override methods safely
            for (var key in fakePostHog) {
                try { window.posthog[key] = fakePostHog[key]; } catch(e) {}
            }
        }
    } catch(e) {
        // If defineProperty fails, just set directly
        try { window.posthog = fakePostHog; } catch(e2) {}
    }
    
    // Block PostHog from loading via script injection (but allow ap.emergent.sh)
    var origCreateElement = document.createElement;
    document.createElement = function(tag) {
        var el = origCreateElement.apply(document, arguments);
        if (tag.toLowerCase() === 'script') {
            var origSetAttr = el.setAttribute;
            el.setAttribute = function(name, value) {
                if (name === 'src' && value && value.includes('posthog')) {
                    console.log('[AntiF] Blocked PostHog script: ' + value);
                    return;
                }
                // Allow ap.emergent.sh scripts - we spoof the data instead
                return origSetAttr.apply(el, arguments);
            };
        }
        return el;
    };
    
    // Facebook Pixel
    window.fbq = function() {};
    window._fbq = window.fbq;
    
    // TikTok Pixel
    window.ttq = { track: function(){}, identify: function(){}, page: function(){} };
    
    // FingerprintJS
    window.FingerprintJS = { 
        load: function() { 
            return Promise.resolve({ 
                get: function() { return Promise.resolve({ visitorId: 'blocked-' + Math.random().toString(36).substr(2,9) }); } 
            }); 
        } 
    };
    
    console.log('[AntiF] PostHog, FB, TikTok, FingerprintJS neutralized');

    // ═══════════════════════════════════════════════════════════════════════
    // 3. DEVICE-ID SPOOF
    // ═══════════════════════════════════════════════════════════════════════
    var newDeviceId = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = Math.random() * 16 | 0;
        return (c == 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
    
    var origFetch2 = window.fetch;
    window.fetch = function(url, opts) {
        opts = opts || {};
        opts.headers = opts.headers || {};
        if (opts.headers instanceof Headers) {
            opts.headers.set('X-Device-Id', newDeviceId);
        } else {
            opts.headers['X-Device-Id'] = newDeviceId;
        }
        return origFetch2.apply(this, arguments);
    };
    
    var origXHRSetHeader = XMLHttpRequest.prototype.setRequestHeader;
    XMLHttpRequest.prototype.setRequestHeader = function(name, value) {
        if (name.toLowerCase() === 'x-device-id') {
            return origXHRSetHeader.call(this, name, newDeviceId);
        }
        return origXHRSetHeader.apply(this, arguments);
    };
    
    console.log('[AntiF] Device-Id: ' + newDeviceId);

    // ═══════════════════════════════════════════════════════════════════════
    // 4. SUPABASE ANON KEY SPOOF
    // ═══════════════════════════════════════════════════════════════════════
    var ANON_KEY = '${SupabaseConfig.ANON_KEY}';
    
    var origFetch3 = window.fetch;
    window.fetch = function(url, opts) {
        var urlStr = typeof url === 'string' ? url : (url.url || '');
        if (urlStr.includes('supabase.co') || urlStr.includes('auth.emergent.sh')) {
            opts = opts || {};
            opts.headers = opts.headers || {};
            if (opts.headers instanceof Headers) {
                opts.headers.set('apikey', ANON_KEY);
                opts.headers.set('Authorization', 'Bearer ' + ANON_KEY);
            } else {
                opts.headers['apikey'] = ANON_KEY;
                opts.headers['Authorization'] = 'Bearer ' + ANON_KEY;
            }
        }
        return origFetch3.apply(this, arguments);
    };
    
    console.log('[AntiF] Supabase ANON KEY spoofing active');

    // ═══════════════════════════════════════════════════════════════════════
    // 5. FEATURE FLAGS SPOOF
    // ═══════════════════════════════════════════════════════════════════════
    var spoofedFlags = ${featureFlags.entries.joinToString(",", "{", "}") { "\"${it.key}\":${it.value}" }};
    
    if (window.posthog) {
        window.posthog.isFeatureEnabled = function(flag) { return spoofedFlags[flag] || false; };
        window.posthog.getFeatureFlag = function(flag) { return spoofedFlags[flag]; };
    }
    
    localStorage.setItem('${'$'}enabled_feature_flags', JSON.stringify(spoofedFlags));
    console.log('[AntiF] Feature Flags spoofed: ' + Object.keys(spoofedFlags).length);

    // ═══════════════════════════════════════════════════════════════════════
    // 6. FULL CLEANUP (localStorage, sessionStorage, cookies, IndexedDB)
    // ═══════════════════════════════════════════════════════════════════════
    var lsKeys = ${localStorageKeys.joinToString(",", "[", "]") { "\"$it\"" }};
    var ssKeys = ${sessionStorageKeys.joinToString(",", "[", "]") { "\"$it\"" }};
    var ckNames = ${cookies.joinToString(",", "[", "]") { "\"$it\"" }};
    
    var lsCleared = 0, ssCleared = 0, ckCleared = 0;
    
    // localStorage
    lsKeys.forEach(function(key) {
        Object.keys(localStorage).forEach(function(k) {
            if (k === key || k.startsWith(key) || k.includes('hasSeen') || k.includes('fingerprint') || k.startsWith('ph_')) {
                localStorage.removeItem(k);
                lsCleared++;
            }
        });
    });
    
    // sessionStorage
    ssKeys.forEach(function(key) {
        Object.keys(sessionStorage).forEach(function(k) {
            if (k === key || k.startsWith(key)) {
                sessionStorage.removeItem(k);
                ssCleared++;
            }
        });
    });
    
    // Cookies
    document.cookie.split(';').forEach(function(c) {
        var name = c.split('=')[0].trim();
        ckNames.forEach(function(cn) {
            if (name === cn || name.startsWith(cn)) {
                document.cookie = name + '=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/';
                document.cookie = name + '=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/;domain=' + location.hostname;
                document.cookie = name + '=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/;domain=.' + location.hostname;
                ckCleared++;
            }
        });
    });
    
    // IndexedDB
    try {
        if (indexedDB.databases) {
            indexedDB.databases().then(function(dbs) {
                dbs.forEach(function(db) { indexedDB.deleteDatabase(db.name); });
            });
        }
    } catch(e) {}
    
    // Cache Storage
    try {
        if (caches && caches.keys) {
            caches.keys().then(function(names) {
                names.forEach(function(name) { caches.delete(name); });
            });
        }
    } catch(e) {}
    
    console.log('[AntiF] Cleanup: LS=' + lsCleared + ', SS=' + ssCleared + ', CK=' + ckCleared);

    // ═══════════════════════════════════════════════════════════════════════
    // 7. RESET ALL MODALS (50+ functions)
    // ═══════════════════════════════════════════════════════════════════════
    var resetFuncs = ${resetFunctions.joinToString(",", "[", "]") { "\"$it\"" }};
    
    resetFuncs.forEach(function(fn) {
        try { if (window[fn]) window[fn](); } catch(e) {}
    });
    
    // Remove modal elements from DOM
    ['modal', 'Modal', 'dialog', 'Dialog', 'popup', 'Popup', 'overlay', 'Overlay', 'backdrop', 'Backdrop'].forEach(function(cls) {
        document.querySelectorAll('[class*="' + cls + '"]').forEach(function(el) {
            if (!el.closest('main') && !el.closest('#root > div:first-child')) {
                el.style.display = 'none';
                try { el.remove(); } catch(e) {}
            }
        });
    });
    
    document.querySelectorAll('[role="dialog"], [role="alertdialog"], [data-radix-portal], [data-state="open"]').forEach(function(el) {
        el.style.display = 'none';
        try { el.remove(); } catch(e) {}
    });
    
    // Restore scroll
    document.body.style.overflow = 'auto';
    document.body.style.overflowY = 'auto';
    document.documentElement.style.overflow = 'auto';
    document.body.classList.remove('overflow-hidden', 'modal-open', 'no-scroll');
    
    console.log('[AntiF] Modals reset: ' + resetFuncs.length + ' functions called');

    // ═══════════════════════════════════════════════════════════════════════
    // SUMMARY
    // ═══════════════════════════════════════════════════════════════════════
    console.log('%c╔═══════════════════════════════════════════════════════════╗', 'color: #00ff41; font-weight: bold;');
    console.log('%c║  FULL BYPASS COMPLETE!                                    ║', 'color: #00ff41; font-weight: bold;');
    console.log('%c║  • Tracking: ' + blockedDomains.length + ' domains blocked                         ║', 'color: #00ff41;');
    console.log('%c║  • PostHog/FB/TikTok/FP: neutralized                     ║', 'color: #00ff41;');
    console.log('%c║  • Device-Id: spoofed                                    ║', 'color: #00ff41;');
    console.log('%c║  • Supabase: ANON KEY injected                           ║', 'color: #00ff41;');
    console.log('%c║  • Feature Flags: ' + Object.keys(spoofedFlags).length + ' flags spoofed                       ║', 'color: #00ff41;');
    console.log('%c║  • Storage: LS=' + lsCleared + ' SS=' + ssCleared + ' CK=' + ckCleared + ' cleared                      ║', 'color: #00ff41;');
    console.log('%c║  • Modals: ' + resetFuncs.length + ' reset functions called                   ║', 'color: #00ff41;');
    console.log('%c╚═══════════════════════════════════════════════════════════╝', 'color: #00ff41; font-weight: bold;');
    
    window.antifBypass = {
        deviceId: newDeviceId,
        blockedCount: blockedCount,
        featureFlags: spoofedFlags,
        cleanup: { localStorage: lsCleared, sessionStorage: ssCleared, cookies: ckCleared }
    };
    
    return window.antifBypass;
})();
        """.trimIndent()
    }

    /**
     * Скрипт сброса всех модалок
     */
    fun generateResetModalsScript(): String {
        val funcsJs = resetFunctions.joinToString("\n    ") { "try { window.$it && window.$it(); } catch(e) {}" }
        return """
(function() {
    console.log('[AntiF] Resetting all modals...');
    $funcsJs
    
    document.querySelectorAll('[class*="modal"],[class*="Modal"],[class*="dialog"],[class*="Dialog"],[class*="popup"],[class*="Popup"],[class*="overlay"],[class*="Overlay"],[role="dialog"],[data-radix-portal]').forEach(function(el) {
        el.style.display = 'none';
        try { el.remove(); } catch(e) {}
    });
    
    document.body.style.overflow = 'auto';
    document.documentElement.style.overflow = 'auto';
    
    console.log('[AntiF] All modals reset!');
})();
        """.trimIndent()
    }

    /**
     * Скрипт очистки данных
     */
    fun generateCleanupScript(): String {
        val lsKeysJs = localStorageKeys.joinToString(",") { "\"$it\"" }
        val ssKeysJs = sessionStorageKeys.joinToString(",") { "\"$it\"" }
        val ckNamesJs = cookies.joinToString(",") { "\"$it\"" }
        return """
(function() {
    console.log('[AntiF] Full cleanup...');
    var lsKeys = [$lsKeysJs];
    var ssKeys = [$ssKeysJs];
    var ckNames = [$ckNamesJs];
    
    lsKeys.forEach(function(key) {
        Object.keys(localStorage).forEach(function(k) {
            if (k === key || k.startsWith(key) || k.includes('hasSeen') || k.includes('fingerprint') || k.startsWith('ph_')) {
                localStorage.removeItem(k);
            }
        });
    });
    
    ssKeys.forEach(function(key) {
        Object.keys(sessionStorage).forEach(function(k) {
            if (k === key || k.startsWith(key)) sessionStorage.removeItem(k);
        });
    });
    
    document.cookie.split(';').forEach(function(c) {
        var name = c.split('=')[0].trim();
        ckNames.forEach(function(cn) {
            if (name === cn || name.startsWith(cn)) {
                document.cookie = name + '=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/';
                document.cookie = name + '=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/;domain=.' + location.hostname;
            }
        });
    });
    
    try { indexedDB.databases().then(function(dbs) { dbs.forEach(function(db) { indexedDB.deleteDatabase(db.name); }); }); } catch(e) {}
    try { caches.keys().then(function(names) { names.forEach(function(name) { caches.delete(name); }); }); } catch(e) {}
    
    console.log('[AntiF] Cleanup complete!');
})();
        """.trimIndent()
    }

    /**
     * Скрипт блокировки трекинга
     */
    fun generateTrackingBlockerScript(): String {
        val domainsJs = trackingDomains.joinToString(",") { "\"$it\"" }
        return """
(function() {
    var blocked = [$domainsJs];
    function isBlocked(url) { return url && blocked.some(function(d) { return url.toLowerCase().includes(d); }); }
    
    var origFetch = window.fetch;
    window.fetch = function(url, opts) {
        if (isBlocked(typeof url === 'string' ? url : url.url)) return Promise.reject(new Error('Blocked'));
        return origFetch.apply(this, arguments);
    };
    
    var origXHROpen = XMLHttpRequest.prototype.open;
    XMLHttpRequest.prototype.open = function(m, url) {
        if (isBlocked(url)) { this._blocked = true; return; }
        return origXHROpen.apply(this, arguments);
    };
    var origXHRSend = XMLHttpRequest.prototype.send;
    XMLHttpRequest.prototype.send = function() {
        if (this._blocked) return;
        return origXHRSend.apply(this, arguments);
    };
    
    window.posthog = { capture: function(){}, identify: function(){}, init: function(){} };
    window.fbq = function(){};
    window.ttq = { track: function(){}, identify: function(){} };
    window.FingerprintJS = { load: function() { return Promise.resolve({ get: function() { return Promise.resolve({ visitorId: 'blocked' }); } }); } };
    
    console.log('[AntiF] Tracking blocker active: ' + blocked.length + ' domains');
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
    
    var origFetch = window.fetch;
    window.fetch = function(url, opts) {
        opts = opts || {}; opts.headers = opts.headers || {};
        if (opts.headers instanceof Headers) opts.headers.set('X-Device-Id', id);
        else opts.headers['X-Device-Id'] = id;
        return origFetch.apply(this, arguments);
    };
    
    var origSetHeader = XMLHttpRequest.prototype.setRequestHeader;
    XMLHttpRequest.prototype.setRequestHeader = function(name, value) {
        if (name.toLowerCase() === 'x-device-id') return origSetHeader.call(this, name, id);
        return origSetHeader.apply(this, arguments);
    };
    
    console.log('[AntiF] Device-Id: ' + id);
    return id;
})();
        """.trimIndent()
    }

    /**
     * Скрипт спуфинга Supabase ANON KEY
     */
    fun generateSupabaseSpoofScript(): String {
        return """
(function() {
    var ANON_KEY = '${SupabaseConfig.ANON_KEY}';
    
    var origFetch = window.fetch;
    window.fetch = function(url, opts) {
        var urlStr = typeof url === 'string' ? url : (url.url || '');
        if (urlStr.includes('supabase.co') || urlStr.includes('auth.emergent.sh')) {
            opts = opts || {}; opts.headers = opts.headers || {};
            if (opts.headers instanceof Headers) {
                opts.headers.set('apikey', ANON_KEY);
                opts.headers.set('Authorization', 'Bearer ' + ANON_KEY);
            } else {
                opts.headers['apikey'] = ANON_KEY;
                opts.headers['Authorization'] = 'Bearer ' + ANON_KEY;
            }
            console.log('[AntiF] Supabase request intercepted');
        }
        return origFetch.apply(this, arguments);
    };
    
    console.log('[AntiF] Supabase ANON KEY spoof active');
    console.log('Key: ' + ANON_KEY.substring(0, 50) + '...');
})();
        """.trimIndent()
    }

    /**
     * Скрипт спуфинга Feature Flags
     */
    fun generateFeatureFlagsSpoofScript(): String {
        val flagsJs = featureFlags.entries.joinToString(",") { "\"${it.key}\":${it.value}" }
        return """
(function() {
    var flags = {$flagsJs};
    
    if (window.posthog) {
        window.posthog.isFeatureEnabled = function(f) { return flags[f] || false; };
        window.posthog.getFeatureFlag = function(f) { return flags[f]; };
    }
    
    localStorage.setItem('${'$'}enabled_feature_flags', JSON.stringify(flags));
    console.log('[AntiF] Feature Flags spoofed: ' + Object.keys(flags).length);
})();
        """.trimIndent()
    }

    /**
     * Скрипт нейтрализации PostHog (FIXED)
     */
    fun generatePostHogNeutralizeScript(): String {
        return """
(function() {
    var fake = {
        capture: function(e) { console.log('[AntiF] PostHog blocked: ' + e); },
        identify: function(){}, alias: function(){}, people: { set: function(){} },
        register: function(){}, register_once: function(){}, unregister: function(){},
        opt_out_capturing: function(){}, opt_in_capturing: function(){},
        has_opted_out_capturing: function() { return true; },
        has_opted_in_capturing: function() { return false; },
        reset: function(){}, get_distinct_id: function() { return 'blocked-' + Math.random().toString(36).substr(2,9); },
        getFeatureFlag: function() { return undefined; },
        isFeatureEnabled: function() { return false; },
        reloadFeatureFlags: function(){}, onFeatureFlags: function(){},
        setPersonProperties: function(){}, group: function(){},
        init: function() { return fake; }, __loaded: true,
        _i: [], push: function(){}, __SV: 1.0
    };
    
    // Safe replacement without Object.defineProperty
    try {
        if (typeof window.posthog === 'undefined') {
            window.posthog = fake;
        } else {
            for (var key in fake) {
                try { window.posthog[key] = fake[key]; } catch(e) {}
            }
        }
    } catch(e) {
        try { window.posthog = fake; } catch(e2) {}
    }
    
    // Block ap.emergent.sh completely
    var origFetch = window.fetch;
    window.fetch = function(url, opts) {
        var urlStr = typeof url === 'string' ? url : (url.url || '');
        if (urlStr.includes('ap.emergent.sh') || urlStr.includes('posthog')) {
            console.log('[AntiF] Blocked analytics: ' + urlStr);
            return Promise.resolve(new Response('{}', { status: 200 }));
        }
        return origFetch.apply(this, arguments);
    };
    
    console.log('[AntiF] PostHog fully neutralized (safe mode)');
})();
        """.trimIndent()
    }

    /**
     * Скрипт региональных скидок
     */
    fun generateRegionalDiscountScript(region: String): String {
        val rd = regionalDiscounts.find { it.countryCode.equals(region, true) || it.countryName.equals(region, true) }
            ?: regionalDiscounts.first { it.countryCode == "TR" }
        
        return """
(function() {
    console.log('[AntiF] Applying ${rd.countryName} regional settings...');
    
    // Clear previous regional modals
    localStorage.removeItem('hasSeenFreeWeekendTurkeyModal');
    localStorage.removeItem('hasSeenFreeWeekendCanadaModal');
    localStorage.removeItem('hasSeenPaddleEuropeModal');
    localStorage.removeItem('hasSeenPagBrasilModal');
    
    // Override timezone
    var origDTF = Intl.DateTimeFormat;
    Intl.DateTimeFormat = function(locale, options) {
        options = options || {};
        options.timeZone = '${rd.timezone}';
        return new origDTF(locale || '${rd.language}', options);
    };
    Intl.DateTimeFormat.prototype = origDTF.prototype;
    Intl.DateTimeFormat.supportedLocalesOf = origDTF.supportedLocalesOf;
    
    // Override language
    Object.defineProperty(navigator, 'language', { get: function() { return '${rd.language}'; } });
    Object.defineProperty(navigator, 'languages', { get: function() { return ['${rd.language}', '${rd.language.split("-")[0]}']; } });
    
    console.log('[AntiF] ${rd.countryName} (${rd.countryCode}): ${rd.discount}');
    console.log('[AntiF] Timezone: ${rd.timezone}, Language: ${rd.language}');
    console.log('[AntiF] Payment: ${rd.paymentSystem}');
    console.log('%c[AntiF] RELOAD PAGE to see regional offers!', 'color: #ffff00; font-weight: bold;');
})();
        """.trimIndent()
    }

    /**
     * Скрипт показа полной конфигурации
     */
    fun generateShowConfigScript(): String {
        return """
(function() {
    console.log('%c╔══════════════════════════════════════════════════════════════╗', 'color: #00d4ff; font-weight: bold;');
    console.log('%c║     EMERGENT.SH FULL CONFIGURATION (AntiF Analysis)         ║', 'color: #00d4ff; font-weight: bold;');
    console.log('%c╚══════════════════════════════════════════════════════════════╝', 'color: #00d4ff; font-weight: bold;');
    
    console.log('%c\n=== FINGERPRINTJS PRO ===', 'color: #00ff41; font-weight: bold;');
    console.log('API Key: ${FingerprintJSConfig.API_KEY}');
    console.log('Endpoint: ${FingerprintJSConfig.ENDPOINT}');
    console.log('Version: ${FingerprintJSConfig.VERSION}');
    console.log('Params collected: ${FingerprintJSConfig.collectedParams.size}');
    
    console.log('%c\n=== POSTHOG ===', 'color: #00ff41; font-weight: bold;');
    console.log('API Key: ${PostHogConfig.API_KEY}');
    console.log('API Host: ${PostHogConfig.API_HOST}');
    console.log('Sample Rate: ${PostHogConfig.SAMPLE_RATE} (5%)');
    console.log('Events tracked: ${postHogEvents.size}+');
    
    console.log('%c\n=== SUPABASE ===', 'color: #00ff41; font-weight: bold;');
    console.log('URL: ${SupabaseConfig.PUBLIC_URL}');
    console.log('Auth: ${SupabaseConfig.AUTH_URL}');
    console.log('ANON KEY: ${SupabaseConfig.ANON_KEY.take(50)}...');
    
    console.log('%c\n=== PADDLE ===', 'color: #00ff41; font-weight: bold;');
    console.log('Client Token: ${PaddleConfig.CLIENT_TOKEN}');
    console.log('SDK: ${PaddleConfig.SDK_URL}');
    
    console.log('%c\n=== CREDITS ===', 'color: #00ff41; font-weight: bold;');
    console.log('Daily: ${CreditsConfig.DAILY_CREDITS}');
    console.log('Monthly: ${CreditsConfig.MONTHLY_CREDITS}');
    console.log('Total Free: ${CreditsConfig.TOTAL_FREE_CREDITS}');
    
    console.log('%c\n=== REGIONAL DISCOUNTS ===', 'color: #00ff41; font-weight: bold;');
    ${regionalDiscounts.joinToString("\n    ") { "console.log('${it.countryCode} (${it.countryName}): ${it.discount}');" }}
    
    console.log('%c\n=== STORAGE KEYS ===', 'color: #00ff41; font-weight: bold;');
    console.log('localStorage: ${localStorageKeys.size} keys');
    console.log('sessionStorage: ${sessionStorageKeys.size} keys');
    console.log('cookies: ${cookies.size} cookies');
    
    console.log('%c\n=== FEATURE FLAGS ===', 'color: #00ff41; font-weight: bold;');
    console.log('Total: ${featureFlags.size} flags');
    
    console.log('%c\n=== RESET FUNCTIONS ===', 'color: #00ff41; font-weight: bold;');
    console.log('Total: ${resetFunctions.size} functions');
    
    console.log('%c\n=== API ENDPOINTS ===', 'color: #00ff41; font-weight: bold;');
    console.log('Total: ${apiEndpoints.size} endpoints');
})();
        """.trimIndent()
    }

    /**
     * Скрипт показа данных Emergent
     */
    fun generateShowEmergentDataScript(): String {
        return """
(function() {
    console.log('%c=== EMERGENT DATA ANALYSIS ===', 'color: #00d4ff; font-weight: bold;');
    
    console.log('%c\nlocalStorage:', 'color: #00ff41; font-weight: bold;');
    Object.keys(localStorage).filter(function(k) {
        return k.includes('modal') || k.includes('Modal') || k.includes('hasSeen') ||
               k.includes('fingerprint') || k.includes('fp_') || k.includes('coupon') ||
               k.includes('utm_') || k.includes('posthog') || k.startsWith('ph_') ||
               k.includes('first_');
    }).forEach(function(k) {
        console.log('  ' + k + ' = ' + localStorage.getItem(k));
    });
    
    console.log('%c\nsessionStorage:', 'color: #00ff41; font-weight: bold;');
    Object.keys(sessionStorage).forEach(function(k) {
        console.log('  ' + k + ' = ' + sessionStorage.getItem(k).substring(0, 100));
    });
    
    console.log('%c\nCookies:', 'color: #00ff41; font-weight: bold;');
    document.cookie.split(';').forEach(function(c) { console.log('  ' + c.trim()); });
    
    console.log('%c\nReset functions:', 'color: #00ff41; font-weight: bold;');
    Object.keys(window).filter(function(k) { return k.startsWith('reset'); }).forEach(function(f) {
        console.log('  window.' + f + '()');
    });
})();
        """.trimIndent()
    }

    /**
     * Скрипт мониторинга API
     */
    fun generateFullResetModalsScript(): String = generateResetModalsScript()
    fun generateFullCleanupScript(): String = generateCleanupScript()
    fun generateShowFullConfigScript(): String = generateShowConfigScript()

    fun generateApiMonitorScript(): String {
        val endpointsJs = apiEndpoints.joinToString(",") { "\"$it\"" }
        return """
(function() {
    var endpoints = [$endpointsJs];
    var log = [];
    
    var origFetch = window.fetch;
    window.fetch = function(url, opts) {
        var urlStr = typeof url === 'string' ? url : url.url || '';
        if (endpoints.some(function(e) { return urlStr.includes(e); })) {
            log.push({ time: new Date().toISOString(), method: (opts && opts.method) || 'GET', url: urlStr });
            console.log('%c[API] ' + ((opts && opts.method) || 'GET') + ' ' + urlStr, 'color: #00d4ff;');
        }
        return origFetch.apply(this, arguments);
    };
    
    window.antifApiLog = function() { console.table(log); return log; };
    console.log('[AntiF] API Monitor: ' + endpoints.length + ' endpoints. Use antifApiLog() to view.');
})();
        """.trimIndent()
    }
}
