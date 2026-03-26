package com.antif.browser.core

import com.antif.browser.data.BrowserProfile

object FingerprintSpoofer {

    fun generateSpoofScript(profile: BrowserProfile): String {
        val ua = profile.userAgent.replace("'", "\\'")
        val plat = profile.platform.replace("'", "\\'")
        val lang = profile.language.replace("'", "\\'")
        val tz = profile.timezone.replace("'", "\\'")
        val wglVendor = profile.webglVendor.replace("'", "\\'")
        val wglRenderer = profile.webglRenderer.replace("'", "\\'")

        return """
(function() {
    'use strict';

    // ===== UTILITY FUNCTIONS =====
    // Deterministic PRNG (Mulberry32)
    function mulberry32(seed) {
        return function() {
            var t = seed += 0x6D2B79F5;
            t = Math.imul(t ^ t >>> 15, t | 1);
            t ^= t + Math.imul(t ^ t >>> 7, t | 61);
            return ((t ^ t >>> 14) >>> 0) / 4294967296;
        };
    }
    
    // Gaussian random using Box-Muller
    function gaussianRandom(random) {
        var u1 = random();
        var u2 = random();
        return Math.sqrt(-2 * Math.log(u1)) * Math.cos(2 * Math.PI * u2);
    }
    
    // Clamp value
    function clamp(val, min, max) {
        return Math.max(min, Math.min(max, val));
    }
    
    // Hash float for deterministic noise
    function hashFloat(seed) {
        var x = Math.sin(seed * 12.9898) * 43758.5453;
        return x - Math.floor(x);
    }

    // ===== NAVIGATOR OVERRIDES =====
    var navProps = {
        userAgent: '${ua}',
        platform: '${plat}',
        language: '${lang}',
        languages: Object.freeze(${profile.languages}),
        hardwareConcurrency: ${profile.hardwareConcurrency},
        deviceMemory: ${profile.deviceMemory},
        maxTouchPoints: ${profile.maxTouchPoints},
        doNotTrack: '${profile.doNotTrack}',
        vendor: 'Google Inc.',
        appVersion: '${ua.removePrefix("Mozilla/").replace("'", "\\'")}'
    };

    for (var prop in navProps) {
        try {
            Object.defineProperty(navigator, prop, {
                get: (function(val) { return function() { return val; }; })(navProps[prop]),
                configurable: true
            });
        } catch(e) {}
    }

    // ===== SCREEN OVERRIDES =====
    var screenProps = {
        width: ${profile.screenWidth},
        height: ${profile.screenHeight},
        availWidth: ${profile.screenWidth},
        availHeight: ${profile.screenHeight - 40},
        colorDepth: ${profile.colorDepth},
        pixelDepth: ${profile.colorDepth}
    };

    for (var sp in screenProps) {
        try {
            Object.defineProperty(screen, sp, {
                get: (function(val) { return function() { return val; }; })(screenProps[sp]),
                configurable: true
            });
        } catch(e) {}
    }

    // Device Pixel Ratio
    try {
        Object.defineProperty(window, 'devicePixelRatio', {
            get: function() { return ${profile.devicePixelRatio}; },
            configurable: true
        });
    } catch(e) {}

    // Override window dimensions - ONLY for fingerprinting checks, not layout
    // Note: We do NOT override innerWidth/innerHeight/outerWidth/outerHeight
    // as that breaks CSS media queries and responsive design, causing "squished" sites
    // Screen properties above are enough for fingerprint uniqueness

    // ===== CANVAS FINGERPRINT - GAUSSIAN NOISE (UNDETECTABLE) =====
    if (${profile.blockCanvas}) {
        var canvasSeed = ${profile.canvasNoiseSeed};
        var canvasRandom = mulberry32(canvasSeed);
        
        var origToDataURL = HTMLCanvasElement.prototype.toDataURL;
        HTMLCanvasElement.prototype.toDataURL = function(type) {
            try {
                var ctx = this.getContext('2d');
                if (ctx && this.width > 0 && this.height > 0) {
                    var w = Math.min(this.width, 32);
                    var h = Math.min(this.height, 32);
                    var imgData = ctx.getImageData(0, 0, w, h);
                    var d = imgData.data;
                    var localRandom = mulberry32(canvasSeed);
                    
                    for (var i = 0; i < d.length; i += 4) {
                        // Gaussian noise with sigma=1.5 (undetectable statistically)
                        var noise = gaussianRandom(localRandom) * 1.5;
                        d[i] = clamp(d[i] + noise, 0, 255);     // R
                        d[i+1] = clamp(d[i+1] + noise, 0, 255); // G
                        d[i+2] = clamp(d[i+2] + noise, 0, 255); // B
                        // Alpha channel unchanged
                    }
                    ctx.putImageData(imgData, 0, 0);
                }
            } catch(e) {}
            return origToDataURL.apply(this, arguments);
        };

        var origGetImageData = CanvasRenderingContext2D.prototype.getImageData;
        CanvasRenderingContext2D.prototype.getImageData = function() {
            var imgData = origGetImageData.apply(this, arguments);
            try {
                var d = imgData.data;
                var localRandom = mulberry32(canvasSeed);
                for (var i = 0; i < d.length; i += 4) {
                    var noise = gaussianRandom(localRandom) * 1.5;
                    d[i] = clamp(d[i] + noise, 0, 255);
                    d[i+1] = clamp(d[i+1] + noise, 0, 255);
                    d[i+2] = clamp(d[i+2] + noise, 0, 255);
                }
            } catch(e) {}
            return imgData;
        };
    }

    // ===== WEBGL FULL PARAMETERS SPOOFING =====
    function patchWebGL(proto) {
        if (!proto) return;
        
        // Full WebGL parameters map
        var webglParams = {
            3379: 16384,  // MAX_TEXTURE_SIZE
            34076: 16384, // MAX_CUBE_MAP_TEXTURE_SIZE
            34024: 16384, // MAX_RENDERBUFFER_SIZE
            3386: [32767, 32767], // MAX_VIEWPORT_DIMS
            34921: 16,    // MAX_VERTEX_ATTRIBS
            36347: 4096,  // MAX_VERTEX_UNIFORM_VECTORS
            36348: 30,    // MAX_VARYING_VECTORS
            36349: 1024,  // MAX_FRAGMENT_UNIFORM_VECTORS
            34930: 16,    // MAX_TEXTURE_IMAGE_UNITS
            35660: 16,    // MAX_VERTEX_TEXTURE_IMAGE_UNITS
            35661: 32,    // MAX_COMBINED_TEXTURE_IMAGE_UNITS
            3408: [1, 1], // ALIASED_LINE_WIDTH_RANGE
            3379: [1, 1024], // ALIASED_POINT_SIZE_RANGE
            37445: '${wglVendor}',   // UNMASKED_VENDOR_WEBGL
            37446: '${wglRenderer}'  // UNMASKED_RENDERER_WEBGL
        };
        
        var origGetParam = proto.getParameter;
        proto.getParameter = function(param) {
            if (webglParams[param] !== undefined) {
                return webglParams[param];
            }
            return origGetParam.apply(this, arguments);
        };

        var origGetExtension = proto.getExtension;
        proto.getExtension = function(name) {
            if (name === 'WEBGL_debug_renderer_info') {
                return { UNMASKED_VENDOR_WEBGL: 37445, UNMASKED_RENDERER_WEBGL: 37446 };
            }
            return origGetExtension.apply(this, arguments);
        };
        
        // Spoof getSupportedExtensions
        proto.getSupportedExtensions = function() {
            return [
                "ANGLE_instanced_arrays",
                "EXT_blend_minmax",
                "EXT_color_buffer_half_float",
                "EXT_float_blend",
                "EXT_frag_depth",
                "EXT_shader_texture_lod",
                "EXT_texture_filter_anisotropic",
                "EXT_sRGB",
                "OES_element_index_uint",
                "OES_standard_derivatives",
                "OES_texture_float",
                "OES_texture_float_linear",
                "OES_texture_half_float",
                "OES_texture_half_float_linear",
                "OES_vertex_array_object",
                "WEBGL_color_buffer_float",
                "WEBGL_compressed_texture_s3tc",
                "WEBGL_debug_renderer_info",
                "WEBGL_depth_texture",
                "WEBGL_draw_buffers",
                "WEBGL_lose_context"
            ];
        };
        
        // Spoof getShaderPrecisionFormat
        var origGetShaderPrecision = proto.getShaderPrecisionFormat;
        proto.getShaderPrecisionFormat = function(shaderType, precisionType) {
            return {
                rangeMin: 127,
                rangeMax: 127,
                precision: 23
            };
        };
    }

    try { patchWebGL(WebGLRenderingContext.prototype); } catch(e) {}
    try { patchWebGL(WebGL2RenderingContext.prototype); } catch(e) {}

    // ===== TIMING ATTACK PROTECTION =====
    var timingSeed = ${profile.timingSeed};
    var timingState = timingSeed;
    
    function lcg() {
        timingState = (timingState * 1103515245 + 12345) & 0x7fffffff;
        return timingState / 0x7fffffff;
    }
    
    var origNow = performance.now.bind(performance);
    performance.now = function() {
        var real = origNow();
        // Add random jitter 0-100μs
        var jitter = lcg() * 0.1;
        // Quantize to 100μs to hide precise timing
        return Math.floor((real + jitter) * 10) / 10;
    };
    
    var origDateNow = Date.now;
    Date.now = function() {
        return Math.floor(origDateNow() + lcg() * 2);
    };

    // ===== AUDIO CONTEXT DEEP SPOOFING =====
    if (${profile.blockAudioContext}) {
        try {
            var audioSeed = ${profile.audioNoiseSeed};
            var OrigAudioContext = window.AudioContext || window.webkitAudioContext;
            
            if (OrigAudioContext) {
                // Patch AnalyserNode.getFloatFrequencyData
                var origGetFloatFrequencyData = AnalyserNode.prototype.getFloatFrequencyData;
                AnalyserNode.prototype.getFloatFrequencyData = function(array) {
                    origGetFloatFrequencyData.call(this, array);
                    for (var i = 0; i < array.length; i++) {
                        array[i] += (hashFloat(audioSeed + i) - 0.5) * 0.0001;
                    }
                };
                
                // Patch AudioBuffer.getChannelData
                var origGetChannelData = AudioBuffer.prototype.getChannelData;
                AudioBuffer.prototype.getChannelData = function(channel) {
                    var data = origGetChannelData.call(this, channel);
                    var localSeed = audioSeed + channel;
                    var len = Math.min(data.length, 1000);
                    for (var i = 0; i < len; i++) {
                        data[i] += (hashFloat(localSeed + i) - 0.5) * 0.00001;
                    }
                    return data;
                };
                
                // Spoof base latency
                Object.defineProperty(OrigAudioContext.prototype, 'baseLatency', {
                    get: function() { return 0.005333333333333333; }
                });
                Object.defineProperty(OrigAudioContext.prototype, 'outputLatency', {
                    get: function() { return 0.016; }
                });
                
                // Original oscillator noise
                var origCreateOsc = OrigAudioContext.prototype.createOscillator;
                OrigAudioContext.prototype.createOscillator = function() {
                    var osc = origCreateOsc.apply(this, arguments);
                    var origConnect = osc.connect;
                    var ctx = this;
                    osc.connect = function(dest) {
                        if (dest instanceof AnalyserNode) {
                            var gain = ctx.createGain();
                            gain.gain.value = 1.0 + (audioSeed * 0.0000001);
                            origConnect.call(osc, gain);
                            gain.connect(dest);
                            return gain;
                        }
                        return origConnect.apply(osc, arguments);
                    };
                    return osc;
                };
            }
        } catch(e) {}
    }

    // ===== FONT FINGERPRINTING PROTECTION =====
    try {
        Object.defineProperty(document, 'fonts', {
            get: function() {
                return {
                    check: function(font) {
                        var standardFonts = [
                            'Arial', 'Times New Roman', 'Courier New',
                            'Georgia', 'Verdana', 'Helvetica', 'Tahoma',
                            'Trebuchet MS', 'Impact', 'Comic Sans MS'
                        ];
                        var fontFamily = font.split(' ').pop().replace(/['"]/g, '');
                        return standardFonts.indexOf(fontFamily) !== -1;
                    },
                    ready: Promise.resolve(),
                    forEach: function() {},
                    entries: function() { return [][Symbol.iterator](); },
                    keys: function() { return [][Symbol.iterator](); },
                    values: function() { return [][Symbol.iterator](); },
                    size: 0,
                    addEventListener: function() {},
                    removeEventListener: function() {}
                };
            }
        });
    } catch(e) {}

    // ===== MEDIA QUERIES SPOOFING =====
    var origMatchMedia = window.matchMedia;
    window.matchMedia = function(query) {
        var result = origMatchMedia.call(window, query);
        
        var spoofedQueries = {
            '(prefers-color-scheme: dark)': ${profile.prefersDark},
            '(prefers-color-scheme: light)': ${!profile.prefersDark},
            '(prefers-reduced-motion: reduce)': false,
            '(prefers-reduced-motion: no-preference)': true,
            '(prefers-contrast: high)': false,
            '(prefers-contrast: no-preference)': true,
            '(hover: hover)': true,
            '(hover: none)': false,
            '(pointer: fine)': true,
            '(pointer: coarse)': false,
            '(any-hover: hover)': true,
            '(any-pointer: fine)': true
        };
        
        if (spoofedQueries.hasOwnProperty(query)) {
            return {
                matches: spoofedQueries[query],
                media: query,
                addListener: function() {},
                removeListener: function() {},
                addEventListener: function() {},
                removeEventListener: function() {},
                dispatchEvent: function() { return true; }
            };
        }
        return result;
    };

    // ===== CLIENT RECTS FINGERPRINTING PROTECTION =====
    var rectSeed = ${profile.rectNoiseSeed};
    
    var origGetBoundingClientRect = Element.prototype.getBoundingClientRect;
    Element.prototype.getBoundingClientRect = function() {
        var rect = origGetBoundingClientRect.call(this);
        return new DOMRect(
            rect.x + hashFloat(rectSeed) * 0.00001,
            rect.y + hashFloat(rectSeed + 1) * 0.00001,
            rect.width + hashFloat(rectSeed + 2) * 0.00001,
            rect.height + hashFloat(rectSeed + 3) * 0.00001
        );
    };
    
    var origGetClientRects = Element.prototype.getClientRects;
    Element.prototype.getClientRects = function() {
        var rects = origGetClientRects.call(this);
        var result = [];
        for (var i = 0; i < rects.length; i++) {
            var r = rects[i];
            result.push(new DOMRect(
                r.x + hashFloat(rectSeed + i * 4) * 0.00001,
                r.y + hashFloat(rectSeed + i * 4 + 1) * 0.00001,
                r.width + hashFloat(rectSeed + i * 4 + 2) * 0.00001,
                r.height + hashFloat(rectSeed + i * 4 + 3) * 0.00001
            ));
        }
        return result;
    };

    // ===== TIMEZONE OVERRIDE =====
    try {
        var origDTF = Intl.DateTimeFormat;
        var newDTF = function(locale, options) {
            if (!options) options = {};
            if (!options.timeZone) options.timeZone = '${tz}';
            return new origDTF(locale, options);
        };
        newDTF.prototype = origDTF.prototype;
        newDTF.supportedLocalesOf = origDTF.supportedLocalesOf;
        Intl.DateTimeFormat = newDTF;
    } catch(e) {}

    try {
        Date.prototype.getTimezoneOffset = function() { return ${profile.timezoneOffset}; };
    } catch(e) {}

    // ===== WEBRTC LEAK PROTECTION =====
    if (${profile.blockWebRTC}) {
        try {
            var noop = function() { return {}; };
            window.RTCPeerConnection = noop;
            window.webkitRTCPeerConnection = noop;
            window.mozRTCPeerConnection = noop;
            if (navigator.mediaDevices) {
                navigator.mediaDevices.enumerateDevices = function() {
                    return Promise.resolve([]);
                };
            }
        } catch(e) {}
    }

    // ===== BATTERY API =====
    try {
        navigator.getBattery = function() {
            return Promise.resolve({
                charging: true, chargingTime: Infinity,
                dischargingTime: Infinity, level: 1.0,
                addEventListener: function(){}, removeEventListener: function(){}
            });
        };
    } catch(e) {}

    // ===== CONNECTION API =====
    try {
        Object.defineProperty(navigator, 'connection', {
            get: function() {
                return {
                    effectiveType: '4g', rtt: 50, downlink: 10, saveData: false,
                    addEventListener: function(){}, removeEventListener: function(){}
                };
            }, configurable: true
        });
    } catch(e) {}

    // ===== PLUGINS OVERRIDE (OS-consistent) =====
    try {
        var osType = '${profile.osType}';
        var plugins = [];
        
        if (osType === 'windows' || osType === 'mac') {
            plugins = [
                {name: 'Chrome PDF Plugin', filename: 'internal-pdf-viewer', description: 'Portable Document Format'},
                {name: 'Chrome PDF Viewer', filename: 'mhjfbmdgcfjbbpaeojofohoefgiehjai', description: ''},
                {name: 'Native Client', filename: 'internal-nacl-plugin', description: ''}
            ];
        }
        
        Object.defineProperty(navigator, 'plugins', {
            get: function() { return plugins; },
            configurable: true
        });
        Object.defineProperty(navigator, 'mimeTypes', {
            get: function() { return []; },
            configurable: true
        });
    } catch(e) {}

    // ===== PERMISSIONS API OVERRIDE =====
    try {
        var origQuery = navigator.permissions.query;
        navigator.permissions.query = function(desc) {
            if (desc.name === 'notifications') {
                return Promise.resolve({ state: 'prompt', addEventListener: function(){} });
            }
            return origQuery.apply(this, arguments);
        };
    } catch(e) {}

    // ===== DISABLE FINGERPRINTING SCRIPTS =====
    var blockedDomains = [
        'api.fpjs.io', 'cdn.fpjs.io', 'fpjs.io',
        'fpnpmcdn.net', 'fpcdn.io', 'fingerprint.com'
    ];

    var origFetch = window.fetch;
    window.fetch = function(url, opts) {
        if (typeof url === 'string') {
            for (var i = 0; i < blockedDomains.length; i++) {
                if (url.indexOf(blockedDomains[i]) !== -1) {
                    return Promise.reject(new Error('Blocked by AntiF'));
                }
            }
        }
        return origFetch.apply(this, arguments);
    };

    var origXHROpen = XMLHttpRequest.prototype.open;
    XMLHttpRequest.prototype.open = function(method, url) {
        if (typeof url === 'string') {
            for (var i = 0; i < blockedDomains.length; i++) {
                if (url.indexOf(blockedDomains[i]) !== -1) {
                    this._blocked = true;
                    return;
                }
            }
        }
        return origXHROpen.apply(this, arguments);
    };

    var origXHRSend = XMLHttpRequest.prototype.send;
    XMLHttpRequest.prototype.send = function() {
        if (this._blocked) return;
        return origXHRSend.apply(this, arguments);
    };

    console.log('%c[AntiF] Advanced fingerprint protection active', 'color: #00ff41; font-weight: bold;');
})();
        """.trimIndent()
    }

    fun generateCleanupScript(): String {
        return """
(function() {
    // Clear all storage
    try { localStorage.clear(); } catch(e) {}
    try { sessionStorage.clear(); } catch(e) {}
    
    // Clear IndexedDB
    try {
        indexedDB.databases().then(function(dbs) {
            dbs.forEach(function(db) { indexedDB.deleteDatabase(db.name); });
        });
    } catch(e) {}
    
    console.log('%c[AntiF] Storage cleared', 'color: #ff6600; font-weight: bold;');
})();
        """.trimIndent()
    }

    fun generateResetModalsScript(): String {
        return """
(function() {
    // Reset all modals
    try { window.resetAllModals && window.resetAllModals(); } catch(e) {}
    try { window.resetFreeStandardPlanModal && window.resetFreeStandardPlanModal(); } catch(e) {}
    
    // Force close any visible modals
    var modals = document.querySelectorAll('[class*="modal"], [class*="dialog"], [class*="popup"], [class*="overlay"]');
    modals.forEach(function(m) {
        m.style.display = 'none';
        m.remove();
    });
    
    // Remove overflow hidden from body
    document.body.style.overflow = 'auto';
    document.documentElement.style.overflow = 'auto';
    
    console.log('%c[AntiF] All modals reset', 'color: #00ff41; font-weight: bold;');
})();
        """.trimIndent()
    }
}
