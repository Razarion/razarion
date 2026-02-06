/**
 * Client Bootstrap Script for TeaVM WebAssembly GC Client
 *
 * This script loads and initializes the TeaVM-compiled WebAssembly GC module
 * in the main thread (browser window context).
 *
 * WASM-GC requires: Chrome 119+, Firefox 120+, Safari 18.2+
 */
(function() {
    'use strict';

    window.RAZ_startTime = performance.now();

    console.log('[TeaVM Client] Starting WebAssembly GC client bootstrap...');

    var script = document.createElement('script');
    script.src = '/teavm-client/classes.wasm-runtime.js';
    script.onload = async function() {
        try {
            console.log('[TeaVM Client] Runtime loaded, initializing WASM-GC module...');

            var teavm = await TeaVM.wasmGC.load("/teavm-client/razarion-client.wasm", {
                stackDeobfuscator: {
                    enabled: false
                }
            });

            console.log('[TeaVM Client] WASM-GC module loaded, calling main...');

            teavm.exports.main([]);

            console.log('[TeaVM Client] Client initialization complete');

        } catch (error) {
            console.error('[TeaVM Client] Failed to initialize WebAssembly GC client:', error);
        }
    };
    script.onerror = function(error) {
        console.error('[TeaVM Client] Failed to load WASM-GC runtime script:', error);
    };
    document.head.appendChild(script);
})();
