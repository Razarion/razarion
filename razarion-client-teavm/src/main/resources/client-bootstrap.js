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

    /*
     * Build stamp, substituted by Maven (see razarion.build in the pom). This file itself is
     * always fetched fresh - wasm-boot.ts appends a timestamp - so the stamp below is always
     * the one from the running deployment. Everything it is appended to may therefore be cached
     * indefinitely: a deploy changes the URL, nothing else does.
     *
     * Without it both files kept their fixed names, the browser held on to them for an hour, and
     * a returning player ran the freshly deployed TypeScript against the previous WASM - with the
     * whole Java/JS bridge contract sitting on that seam.
     */
    var BUILD = '${razarion.build}';

    console.log('[TeaVM Client] Starting WebAssembly GC client bootstrap, build ' + BUILD);

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
    script.src = '/teavm-client/classes.wasm-runtime.js?v=' + BUILD;
    script.onload = async function() {
        try {
            console.log('[TeaVM Client] Runtime loaded, initializing WASM-GC module...');

            /*
             * noAutoImports: the loader would otherwise call WebAssembly.Module.imports() to
             * resolve ES module globals. Safari/WebKit throws a TypeError there ("unable to
             * produce import descriptors") because WasmGC ref types are not expressible in the
             * JS type reflection API - even though the module itself compiled fine. Our module
             * has no global imports at all (only teavmJso/teavmMath/teavmDate/teavm functions),
             * so skipping that step costs nothing.
             */
            var teavm = await TeaVM.wasmGC.load("/teavm-client/razarion-client.wasm?v=" + BUILD, {
                noAutoImports: true,
                stackDeobfuscator: {
                    enabled: false
                }
            });

            console.log('[TeaVM Client] WASM-GC module loaded');

            /*
             * Downloading and compiling the module needs nothing from the page; entering main
             * does. It reaches straight for the cockpit adapters on the facade, so it has to
             * wait for Angular to have wired them.
             *
             * That is why this file is fetched from main.ts before bootstrapApplication rather
             * than from GameComponent: the compile then happens beside the bundle download
             * instead of after it. The latch releases on whichever of the two finishes second,
             * so neither order can lose. Without it - an older page, the mock build - main runs
             * straight away, which is what it did before.
             */
            var startMain = function () {
                try {
                    console.log('[TeaVM Client] Calling main...');
                    teavm.exports.main([]);
                    console.log('[TeaVM Client] Client initialization complete');
                } catch (error) {
                    console.error('[TeaVM Client] main() failed:', error);
                    track('WASM_LOAD', 'WASM-GC main failed: ' + error);
                }
            };
            if (window.RAZ_boot && window.RAZ_boot.wasmReady) {
                /*
                 * If the other half never arrives the game hangs on the splash screen with
                 * nothing to show for it - the one failure mode this arrangement adds. Say so
                 * rather than wait quietly; twenty seconds is far past any real Angular boot,
                 * and the engine is not started here because calling into an unwired facade
                 * fails worse and later than not calling at all.
                 */
                var released = false;
                setTimeout(function () {
                    if (!released) {
                        track('WASM_LOAD', 'Angular never signalled readiness');
                    }
                }, 20000);
                window.RAZ_boot.wasmReady(function () {
                    released = true;
                    startMain();
                });
            } else {
                startMain();
            }

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
