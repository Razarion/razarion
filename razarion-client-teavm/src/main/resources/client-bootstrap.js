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

    window.RAZ_startTime = Date.now();

    console.log('[TeaVM Client] Starting WebAssembly GC client bootstrap...');

    var wasmStart = Date.now();

    /*
     * Report a startup task from here, where the engine cannot yet report for itself. A browser
     * without WASM-GC support fails inside this file and used to leave nothing behind but a
     * console message - the session simply stopped after PAGE_LOADED. The globals and the
     * sender come from the inline script in index.html; see PageBootGlobals.java.
     */
    function track(taskEnum, error) {
        try {
            window.RAZ_currentTask = taskEnum;
            if (!window.RAZ_track || !window.RAZ_trackPayload) {
                return;
            }
            window.RAZ_track('/rest/tracker/startupTask', window.RAZ_trackPayload({
                taskEnum: taskEnum,
                startTime: wasmStart,
                duration: Date.now() - wasmStart,
                error: error || null
            }));
        } catch (ignored) {
        }
    }

    track('WASM_BOOTSTRAP');

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
            // Needs Chrome 119+, Firefox 120+ or Safari 18.2+. Older browsers land here.
            track('WASM_LOAD', 'WASM-GC init failed: ' + error);
        }
    };
    script.onerror = function(error) {
        console.error('[TeaVM Client] Failed to load WASM-GC runtime script:', error);
        track('WASM_LOAD', 'WASM-GC runtime script failed to load');
    };
    document.head.appendChild(script);
})();
