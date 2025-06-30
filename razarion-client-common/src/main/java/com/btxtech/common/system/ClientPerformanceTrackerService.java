package com.btxtech.common.system;

import com.btxtech.shared.deprecated.Caller;
import com.btxtech.shared.rest.TrackerController;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.shared.system.perfmon.PerfmonService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 27.03.2017.
 */
@Singleton
public class ClientPerformanceTrackerService {
    private static final long SEND_SERVER_INTERVAL = 10000;
    private final Logger logger = Logger.getLogger(ClientPerformanceTrackerService.class.getName());
    private final Caller<TrackerController> providerCaller;
    private final SimpleExecutorService simpleExecutorService;
    private final PerfmonService perfmonService;
    private SimpleScheduledFuture simpleScheduledFuture;

    @Inject
    public ClientPerformanceTrackerService(PerfmonService perfmonService,
                                           SimpleExecutorService simpleExecutorService,
                                           Caller<TrackerController> providerCaller) {
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
        } catch (Throwable t) {
            logger.log(Level.WARNING, t.getMessage(), t);
        }
    }

    private void sendToClient() {
//    TODO    List<PerfmonStatistic> perfmonStatistics = perfmonService.pullServerPerfmonStatistics();
//        if (perfmonStatistics.size() > 0) {
//            providerCaller.call(response -> {
//                    }, exceptionHandler.restErrorHandler("TrackerProvider.performanceTracker()")
//            ).performanceTracker(perfmonStatistics);
//        }
//        List<TerrainTileStatistic> terrainTileStatistics = perfmonService.flushTerrainTileStatistics();
//        if (!terrainTileStatistics.isEmpty()) {
//            providerCaller.call(response -> {
//            }, exceptionHandler.restErrorHandler("TrackerProvider.terrainTileStatisticsTracker()")).terrainTileStatisticsTracker(terrainTileStatistics);
//        }
    }
}
