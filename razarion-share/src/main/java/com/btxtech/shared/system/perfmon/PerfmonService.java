package com.btxtech.shared.system.perfmon;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 26.07.12
 * Time: 14:45
 */
@ApplicationScoped
public class PerfmonService {
    public static final int COUNT = 100;
    public static final long DUMP_DELAY = 2000;
    private Logger logger = Logger.getLogger(PerfmonService.class.getName());
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @Inject
    private ExceptionHandler exceptionHandler;
    private Map<PerfmonEnum, Long> enterTimes = new HashMap<>();
    private Collection<SampleEntry> sampleEntries = new ArrayList<>();
    private StatisticConsumer clientStatisticConsumer = new StatisticConsumer();
    private StatisticConsumer serverStatisticConsumer = new StatisticConsumer();
    private SimpleScheduledFuture simpleScheduledFuture;
    private List<TerrainTileStatistic> terrainTileStatistics = new ArrayList<>();
    private String gameSessionUuid;

    public void start(String gameSessionUuid) {
        this.gameSessionUuid = gameSessionUuid;
        if (simpleScheduledFuture != null) {
            simpleScheduledFuture.cancel();
            simpleScheduledFuture = null;
            logger.warning("PerfmonService.stop(): simpleScheduledFuture != null");
        }
        simpleScheduledFuture = simpleExecutorService.scheduleAtFixedRate(DUMP_DELAY, true, () -> {
            try {
                Collection<StatisticEntry> statisticEntries = analyse();
                for (StatisticEntry statisticEntry : statisticEntries) {
                    clientStatisticConsumer.push(statisticEntry);
                    serverStatisticConsumer.push(statisticEntry);
                }
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        }, SimpleExecutorService.Type.UNSPECIFIED);
    }

    public void stop() {
        if (simpleScheduledFuture != null) {
            simpleScheduledFuture.cancel();
            simpleScheduledFuture = null;
            enterTimes.clear();
            sampleEntries.clear();
            clientStatisticConsumer.clear();
            serverStatisticConsumer.clear();
        } else {
            logger.warning("PerfmonService.stop(): simpleScheduledFuture == null");
        }
    }

    public void onEntered(PerfmonEnum perfmonEnum) {
        if (!perfmonEnum.isFps()) {
            return;
        }
        if (enterTimes.containsKey(perfmonEnum)) {
            logger.warning("PerfmonService.onEntered(): onEntered has already been called for " + perfmonEnum);
        }
        enterTimes.put(perfmonEnum, System.currentTimeMillis());
    }

    public void onLeft(PerfmonEnum perfmonEnum) {
        if (!perfmonEnum.isFps()) {
            return;
        }
        Long startTime = enterTimes.remove(perfmonEnum);
        if (startTime == null) {
            logger.warning("PerfmonService.onLeft(): onEntered was not called before " + perfmonEnum);
            return;
        }
        sampleEntries.add(new SampleEntry(perfmonEnum, startTime));
    }

    public List<PerfmonStatistic> peekClientPerfmonStatistics() {
        return clientStatisticConsumer.peek(gameSessionUuid);
    }

    public List<PerfmonStatistic> pullServerPerfmonStatistics() {
        return serverStatisticConsumer.pull(gameSessionUuid);
    }

    private Collection<StatisticEntry> analyse() {
        MapCollection<PerfmonEnum, SampleEntry> groupedMap = new MapCollection<>();
        Collection<SampleEntry> tmpSampleEntries = sampleEntries;
        sampleEntries = new ArrayList<>();
        for (SampleEntry sampleEntry : tmpSampleEntries) {
            groupedMap.put(sampleEntry.getPerfmonEnum(), sampleEntry);
        }
        Collection<StatisticEntry> statisticEntries = new ArrayList<>();
        for (Map.Entry<PerfmonEnum, Collection<SampleEntry>> entry : groupedMap.getMap().entrySet()) {
            if (entry.getValue().size() < 2) {
                continue;
            }
            StatisticEntry statisticEntry = new StatisticEntry(entry.getKey());
            for (SampleEntry sample : entry.getValue()) {
                statisticEntry.analyze(sample);
            }
            statisticEntry.finalizeStatistic();
            statisticEntries.add(statisticEntry);
        }
        return statisticEntries;
    }

    public void onTerrainTile(Index terrainTileIndex, long time) {
        if (simpleScheduledFuture != null) {
            TerrainTileStatistic terrainTileStatistic = new TerrainTileStatistic();
            terrainTileStatistic.setGenerationTime((int) time);
            terrainTileStatistic.setTerrainTileIndex(terrainTileIndex);
            terrainTileStatistic.setTimeStamp(new Date());
            terrainTileStatistic.setGameSessionUuid(gameSessionUuid);
            terrainTileStatistics.add(terrainTileStatistic);
        }
    }

    public List<TerrainTileStatistic> flushTerrainTileStatistics() {
        List<TerrainTileStatistic> tmp = terrainTileStatistics;
        terrainTileStatistics = new ArrayList<>();
        return tmp;
    }
}
