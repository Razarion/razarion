package com.btxtech.worker;

import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.worker.jso.JsConsole;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * TeaVM implementation of ClientPerformanceTrackerService
 * Handles performance tracking in the Web Worker
 */
@Singleton
public class TeaVMClientPerformanceTrackerService {
    private static final long SEND_SERVER_INTERVAL = 10000;

    private final PerfmonService perfmonService;
    private final SimpleExecutorService simpleExecutorService;
    private SimpleScheduledFuture simpleScheduledFuture;

    @Inject
    public TeaVMClientPerformanceTrackerService(PerfmonService perfmonService,
                                                SimpleExecutorService simpleExecutorService) {
        this.perfmonService = perfmonService;
        this.simpleExecutorService = simpleExecutorService;
    }

    public void start() {
        // TODO: Implement performance tracking if needed
        // Currently disabled in the original GWT implementation as well
    }

    public void stop() {
        try {
            if (simpleScheduledFuture != null) {
                simpleScheduledFuture.cancel();
                simpleScheduledFuture = null;
                sendToServer();
            } else {
                JsConsole.warn("TeaVMClientPerformanceTrackerService.stop() simpleScheduledFuture == null");
            }
        } catch (Throwable t) {
            JsConsole.warn("TeaVMClientPerformanceTrackerService.stop() error: " + t.getMessage());
        }
    }

    private void sendToServer() {
        // TODO: Implement sending performance data to server if needed
        // Currently disabled in the original GWT implementation as well
    }
}
