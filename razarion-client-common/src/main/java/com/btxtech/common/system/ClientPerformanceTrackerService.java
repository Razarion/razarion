package com.btxtech.common.system;

import com.btxtech.shared.rest.TrackerProvider;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.shared.system.perfmon.PerfmonStatistic;
import com.btxtech.shared.utils.CollectionUtils;
import org.jboss.errai.common.client.api.Caller;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Level;
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

    public void start() {
        simpleExecutorService.scheduleAtFixedRate(SEND_SERVER_INTERVAL, true, () -> {
            int sendCount = (int) (SEND_SERVER_INTERVAL / PerfmonService.DUMP_DELAY);
            List<PerfmonStatistic> perfmonStatistics = perfmonService.getPerfmonStatistics(sendCount);
            if (perfmonStatistics.size() != 1) {
                logger.severe("PerfmonService SEND_SERVER_INTERVAL perfmonStatistics.size() != 1: " + perfmonStatistics.size());
            }
            if (perfmonStatistics.isEmpty()) {
                return;
            }
            PerfmonStatistic perfmonStatistic = CollectionUtils.getFirst(perfmonStatistics);
            providerCaller.call(response -> {
            }, (message, throwable) -> {
                logger.log(Level.SEVERE, "TrackerProvider.performanceTracker() failed: " + message, throwable);
                return false;
            }).performanceTracker(perfmonStatistic);
        }, SimpleExecutorService.Type.UNSPECIFIED);

    }

}