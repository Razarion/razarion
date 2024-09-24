package com.btxtech.common.system;

import com.btxtech.shared.rest.TrackerProvider;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.shared.system.perfmon.PerfmonStatistic;
import com.btxtech.shared.system.perfmon.TerrainTileStatistic;
import com.btxtech.client.Caller;

import javax.inject.Singleton;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 27.03.2017.
 */
@Singleton
public class ClientPerformanceTrackerService {
    private static final long SEND_SERVER_INTERVAL = 10000;
    private Logger logger = Logger.getLogger(ClientPerformanceTrackerService.class.getName());

    private Caller<TrackerProvider> providerCaller;

    private SimpleExecutorService simpleExecutorService;

    private PerfmonService perfmonService;

    private ClientExceptionHandlerImpl exceptionHandler;
    private SimpleScheduledFuture simpleScheduledFuture;

    @Inject
    public ClientPerformanceTrackerService(ClientExceptionHandlerImpl exceptionHandler, PerfmonService perfmonService, SimpleExecutorService simpleExecutorService, Caller<com.btxtech.shared.rest.TrackerProvider> providerCaller) {
        this.exceptionHandler = exceptionHandler;
        this.perfmonService = perfmonService;
        this.simpleExecutorService = simpleExecutorService;
        this.providerCaller = providerCaller;
    }

    public void start() {
//    TODO   if (simpleScheduledFuture != null) {
//    TODO      simpleScheduledFuture.cancel();
//    TODO      logger.warning("ClientPerformanceTrackerService.start() simpleScheduledFuture != null");
//    TODO   }
//    TODO   simpleScheduledFuture = simpleExecutorService.scheduleAtFixedRate(SEND_SERVER_INTERVAL, true, this::sendToClient, SimpleExecutorService.Type.PERFMON_SEND_TO_CLIENT);
    }

    public void stop() {
        try {
            if (simpleScheduledFuture != null) {
                simpleScheduledFuture.cancel();
                simpleScheduledFuture = null;
                sendToClient();
            } else {
                logger.warning("ClientPerformanceTrackerService.stop() simpleScheduledFuture == null");
            }
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    private void sendToClient() {
        List<PerfmonStatistic> perfmonStatistics = perfmonService.pullServerPerfmonStatistics();
        if (perfmonStatistics.size() > 0) {
            providerCaller.call(response -> {
                    }, exceptionHandler.restErrorHandler("TrackerProvider.performanceTracker()")
            ).performanceTracker(perfmonStatistics);
        }
        List<TerrainTileStatistic> terrainTileStatistics = perfmonService.flushTerrainTileStatistics();
        if (!terrainTileStatistics.isEmpty()) {
            providerCaller.call(response -> {
            }, exceptionHandler.restErrorHandler("TrackerProvider.terrainTileStatisticsTracker()")).terrainTileStatisticsTracker(terrainTileStatistics);
        }
    }
}
