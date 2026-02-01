/**
 * Worker Bootstrap Script for TeaVM WebAssembly Worker
 *
 * This script loads and initializes the TeaVM-compiled WebAssembly module
 * in a Web Worker context.
 */
(function() {
    'use strict';

    console.log('[TeaVM Worker] Starting WebAssembly worker bootstrap...');

    async function initializeWasm() {
        try {
            // Import the TeaVM WebAssembly runtime
            importScripts('razarion-worker.wasm-runtime.js');

            console.log('[TeaVM Worker] Runtime loaded, fetching WebAssembly module...');

            // Fetch the WebAssembly binary
            const response = await fetch('razarion-worker.wasm');
            if (!response.ok) {
                throw new Error('Failed to fetch WebAssembly module: ' + response.status);
            }

            const wasmBytes = await response.arrayBuffer();
            console.log('[TeaVM Worker] WebAssembly binary loaded, size: ' + wasmBytes.byteLength + ' bytes');

            // TeaVM provides a load function via the runtime
            if (typeof TeaVM !== 'undefined' && typeof TeaVM.wasm === 'function') {
                console.log('[TeaVM Worker] Using TeaVM.wasm() loader...');
                const teavm = await TeaVM.wasm(wasmBytes);
                console.log('[TeaVM Worker] WebAssembly instantiated, calling main...');
                teavm.main([]);
            } else if (typeof self.createTeaVM === 'function') {
                console.log('[TeaVM Worker] Using createTeaVM() loader...');
                const teavm = await self.createTeaVM({
                    wasmBinary: wasmBytes
                });
                console.log('[TeaVM Worker] WebAssembly instantiated, calling main...');
                teavm.main([]);
            } else {
                // Direct WebAssembly instantiation fallback
                console.log('[TeaVM Worker] Using direct WebAssembly instantiation...');

                // TeaVM WASM requires specific imports
                const importObject = {
                    env: {
                        memory: new WebAssembly.Memory({ initial: 256, maximum: 4096 })
                    },
                    teavm: {}
                };

                // Check if runtime provides import requirements
                if (typeof self.teavmGetImports === 'function') {
                    const imports = self.teavmGetImports();
                    Object.assign(importObject, imports);
                }

                const result = await WebAssembly.instantiate(wasmBytes, importObject);
                console.log('[TeaVM Worker] WebAssembly instantiated');

                const exports = result.instance.exports;
                if (typeof exports.main === 'function') {
                    exports.main();
                } else if (typeof exports._start === 'function') {
                    exports._start();
                } else {
                    console.log('[TeaVM Worker] Available exports:', Object.keys(exports));
                }
            }

            console.log('[TeaVM Worker] Worker initialization complete');

        } catch (error) {
            console.error('[TeaVM Worker] Failed to initialize WebAssembly worker:', error);

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
    initializeWasm();
})();
