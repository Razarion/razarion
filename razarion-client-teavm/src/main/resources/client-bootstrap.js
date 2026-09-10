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

    /*
     * Whether this failure is worth a second attempt.
     *
     * Seven days of PROD split the 179 dead starts in this file almost in half:
     *
     *   51  TypeError: Failed to fetch
     *   46  WebAssembly compilation aborted: Network error: Response body loading was aborted
     *   66  CompileError: ... enable with --experimental-wasm-gc / Invalid opcode 0xfb
     *
     * The first two are a connection that died mid-body - a phone changing cell inside an in-app
     * browser, and the median download here is eighteen seconds, so there is plenty of body to
     * die in the middle of. Those are worth retrying.
     *
     * A CompileError is not. The browser has read the bytes and cannot execute them; fetching the
     * same eight megabytes again produces the same verdict, on a device that already waited
     * eighteen seconds for the first copy. Retrying it would be cruelty with extra steps.
     */
    function worthRetrying(error) {
        if (error instanceof WebAssembly.CompileError) {
            return false;
        }
        var text = String(error && error.message ? error.message : error);
        return /Failed to fetch|NetworkError|Network error|aborted|network|load failed/i.test(text);
    }

    /*
     * Three attempts, 1s and 3s apart. The gaps are there because an instant retry hits the same
     * dead radio; they are short because the player has already been waiting and a fourth attempt
     * would be past anyone's patience - PROD says a mobile visitor gives up at around 11 seconds.
     */
    async function loadWithRetry() {
        var versuche = 3;
        for (var i = 1; ; i++) {
            try {
                return await TeaVM.wasmGC.load("/teavm-client/razarion-client.wasm?v=" + BUILD, {
                    noAutoImports: true,
                    stackDeobfuscator: {
                        enabled: false
                    }
                });
            } catch (error) {
                if (i >= versuche || !worthRetrying(error)) {
                    throw error;
                }
                console.warn('[TeaVM Client] WASM download failed (attempt ' + i + '), retrying:', error);
                // Reported per attempt, not only at the end: without it a start that succeeded on
                // the second try is indistinguishable from one that never stumbled, and the
                // question this retry has to answer is how often it is actually needed.
                track('WASM_RETRY', 'attempt ' + i + ': ' + error);
                await new Promise(function (ok) { setTimeout(ok, i * 2000 - 1000); });
            }
        }
    }

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
            var teavm = await loadWithRetry();

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
            /*
             * Tell the player. Until now this line was the whole response to a start that can
             * never finish: 179 sessions in seven days watched a progress bar with nothing behind
             * it, and one of them reached the game.
             *
             * A CompileError is the browser saying it cannot run this, which is a sentence a
             * person can act on. Everything else here is a network that gave up after three
             * attempts, and "reload" is the honest advice for that - so it keeps the ordinary
             * loading screen rather than claiming the browser is at fault.
             */
            if (error instanceof WebAssembly.CompileError && window.RAZ_showUnsupported) {
                window.RAZ_showUnsupported('compile failed: ' + error);
            }
        }
    };
    script.onerror = function(error) {
        console.error('[TeaVM Client] Failed to load WASM-GC runtime script:', error);
        track('WASM_LOAD', 'WASM-GC runtime script failed to load');
    };
    document.head.appendChild(script);
})();
