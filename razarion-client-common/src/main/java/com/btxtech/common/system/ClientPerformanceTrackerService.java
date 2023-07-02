package com.btxtech.common.system;

import com.btxtech.shared.rest.TrackerProvider;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.shared.system.perfmon.PerfmonStatistic;
import com.btxtech.shared.system.perfmon.TerrainTileStatistic;
import org.jboss.errai.common.client.api.Caller;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 27.03.2017.
 */
@ApplicationScoped
public class ClientPerformanceTrackerService {
    private static final long SEND_SERVER_INTERVAL = 10000;
    private Logger logger = Logger.getLogger(ClientPerformanceTrackerService.class.getName());
    @Inject
    private Caller<TrackerProvider> providerCaller;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @Inject
    private PerfmonService perfmonService;
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    private SimpleScheduledFuture simpleScheduledFuture;

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
