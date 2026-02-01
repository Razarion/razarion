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

    console.log('[TeaVM Worker] Starting WebAssembly GC worker bootstrap...');

    // Load the TeaVM WASM-GC runtime (fixed filename from TeaVM)
    importScripts('classes.wasm-runtime.js');

    async function initializeWasmGC() {
        try {
            console.log('[TeaVM Worker] Runtime loaded, initializing WASM-GC module...');

            // Load the WASM-GC module using TeaVM's loader
            let teavm = await TeaVM.wasmGC.load("razarion-worker.wasm", {
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
