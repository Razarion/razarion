package com.btxtech.worker;

import com.btxtech.worker.di.DaggerWorkerComponent;
import com.btxtech.worker.di.WorkerComponent;
import com.btxtech.worker.jso.JsConsole;

/**
 * TeaVM Entry Point for the Web Worker
 * Replaces GWT's EntryPoint interface with a standard main method
 */
public class TeaVMWorkerMain {

    public static void main(String[] args) {
        try {
            JsConsole.log("TeaVM Worker initializing...");

            // Create Dagger component and initialize the worker
            WorkerComponent component = DaggerWorkerComponent.create();
            component.clientGameEngineWorker().init();

            JsConsole.log("TeaVM Worker initialized successfully");
        } catch (Throwable t) {
            JsConsole.error("TeaVM Worker initialization failed: " + t.getMessage());
            throw t;
        }
    }
}
