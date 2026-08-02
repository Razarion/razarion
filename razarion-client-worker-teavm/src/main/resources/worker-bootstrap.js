/**
 * Worker Bootstrap Script for TeaVM WebAssembly GC Worker
 *
 * This script loads and initializes the TeaVM-compiled WebAssembly GC module
 * in a Web Worker context.
 *
 * WASM-GC requires: Chrome 119+, Firefox 120+, Safari 18.2+
 */
(function() {
    'use strict';

    /*
     * Build stamp, substituted by Maven (see razarion.build in the pom). This file is always
     * fetched fresh - CommonUrl.getWorkerScriptUrl() appends a timestamp - but a relative URL
     * inside a worker does NOT inherit that query, so both files below need the stamp of their
     * own. Without it they kept their fixed names and the browser served the previous build.
     */
    var BUILD = '${razarion.build}';

    console.log('[TeaVM Worker] Starting WebAssembly GC worker bootstrap, build ' + BUILD);

    // Load the TeaVM WASM-GC runtime (fixed filename from TeaVM)
    importScripts('classes.wasm-runtime.js?v=' + BUILD);

    async function initializeWasmGC() {
        try {
            console.log('[TeaVM Worker] Runtime loaded, initializing WASM-GC module...');

            // Load the WASM-GC module using TeaVM's loader
            // noAutoImports: skip WebAssembly.Module.imports(), which throws on Safari/WebKit
            // for WasmGC ref types. No global imports here, so nothing is lost.
            let teavm = await TeaVM.wasmGC.load("razarion-worker.wasm?v=" + BUILD, {
                noAutoImports: true,
                stackDeobfuscator: {
                    enabled: false  // Disable to avoid 404 for deobfuscator.wasm
                }
            });

            console.log('[TeaVM Worker] WASM-GC module loaded, calling main...');

            // Call the Java main method
            teavm.exports.main([]);

            console.log('[TeaVM Worker] Worker initialization complete');

        } catch (error) {
            console.error('[TeaVM Worker] Failed to initialize WebAssembly GC worker:', error);

            // Send error message to main thread
            self.postMessage({
                type: 'error',
                message: 'Worker initialization failed: ' + error.message,
                error: error.toString()
            });

            throw error;
        }
    }

    // Start initialization
    initializeWasmGC();
})();
