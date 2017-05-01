package com.btxtech.shared.system.perfmon;

import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.shared.utils.CollectionUtils;

import javax.annotation.PostConstruct;
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
    public static final int COUNT = 180;
    public static final long DUMP_DELAY = 1000;
    private Logger logger = Logger.getLogger(PerfmonService.class.getName());
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @Inject
    private ExceptionHandler exceptionHandler;
    private Map<PerfmonEnum, Long> enterTimes = new HashMap<>();
    private Collection<SampleEntry> sampleEntries = new ArrayList<>();
    private MapList<PerfmonEnum, StatisticEntry> statisticEntries = new MapList<>();
    private SimpleScheduledFuture simpleScheduledFuture;

    public void start() {
        if (simpleScheduledFuture != null) {
            simpleScheduledFuture.cancel();
            simpleScheduledFuture = null;
            logger.warning("PerfmonService.stop(): simpleScheduledFuture != null");
        }
        simpleScheduledFuture = simpleExecutorService.scheduleAtFixedRate(DUMP_DELAY, true, () -> {
            try {
                Collection<StatisticEntry> statisticEntries = analyse();
                for (StatisticEntry statisticEntry : statisticEntries) {
                    this.statisticEntries.put(statisticEntry.getPerfmonEnum(), statisticEntry);
                    if (this.statisticEntries.get(statisticEntry.getPerfmonEnum()).size() >= COUNT) {
                        this.statisticEntries.get(statisticEntry.getPerfmonEnum()).remove(0);
                    }
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
            statisticEntries.clear();
        } else {
            logger.warning("PerfmonService.stop(): simpleScheduledFuture == null");
        }
    }

    public void onEntered(PerfmonEnum perfmonEnum) {
        if (enterTimes.containsKey(perfmonEnum)) {
            logger.warning("PerfmonService.onEntered(): onEntered has already been called for " + perfmonEnum);
        }
        enterTimes.put(perfmonEnum, System.currentTimeMillis());
    }

    public void onLeft(PerfmonEnum perfmonEnum) {
        Long startTime = enterTimes.remove(perfmonEnum);
        if (startTime == null) {
            logger.warning("PerfmonService.onLeft(): onEntered was not called before " + perfmonEnum);
            return;
        }
        sampleEntries.add(new SampleEntry(perfmonEnum, startTime));
    }

    public MapList<PerfmonEnum, StatisticEntry> getStatisticEntries() {
        return statisticEntries;
    }

    public List<PerfmonStatistic> getPerfmonStatistics(int count) {
        List<PerfmonStatistic> perfmonStatistics = new ArrayList<>();
        for (Map.Entry<PerfmonEnum, List<StatisticEntry>> entry : statisticEntries.getMap().entrySet()) {
            PerfmonStatistic perfmonStatistic = new PerfmonStatistic();
            perfmonStatistic.setTimeStamp(new Date());
            perfmonStatistic.setPerfmonEnum(entry.getKey());
            List<Double> frequency = new ArrayList<>();
            List<Double> avgDuration = new ArrayList<>();
            List<StatisticEntry> value = entry.getValue();
            if (count < 1) {
                count = value.size() - 1;
            } else if (count > value.size() - 1) {
                count = value.size() - 1;
            }
            for (int i = count; i >= 0; i--) {
                StatisticEntry statisticEntry = value.get(i);
                frequency.add(statisticEntry.getFrequency());
                avgDuration.add(statisticEntry.getAvgDuration());
            }
            Collections.reverse(frequency);
            perfmonStatistic.setFrequency(frequency);
            Collections.reverse(avgDuration);
            perfmonStatistic.setAvgDuration(avgDuration);
            perfmonStatistics.add(perfmonStatistic);
        }
        return perfmonStatistics;
    }

    private Collection<StatisticEntry> analyse() {
        MapCollection<PerfmonEnum, SampleEntry> orderedMap = new MapCollection<>();
        Collection<SampleEntry> tmpSampleEntries = sampleEntries;
        sampleEntries = new ArrayList<>();
        for (SampleEntry sampleEntry : tmpSampleEntries) {
            orderedMap.put(sampleEntry.getPerfmonEnum(), sampleEntry);
        }
        Collection<StatisticEntry> statisticEntries = new ArrayList<>();
        for (Collection<SampleEntry> samples : orderedMap.getMap().values()) {
            StatisticEntry statisticEntry = new StatisticEntry(CollectionUtils.getFirst(samples).getPerfmonEnum());
            for (SampleEntry sample : samples) {
                statisticEntry.analyze(sample);
            }
            statisticEntry.finalizeStatistic();
            statisticEntries.add(statisticEntry);
        }
        return statisticEntries;
    }
}
