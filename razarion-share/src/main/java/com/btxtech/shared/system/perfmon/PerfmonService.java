package com.btxtech.shared.system.perfmon;

import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.utils.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 26.07.12
 * Time: 14:45
 */
@ApplicationScoped
public class PerfmonService {
    private static final long DUMP_DELAY = 1000;
    private Logger log = Logger.getLogger(PerfmonService.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    private Map<PerfmonEnum, Long> enterTimes = new HashMap<>();
    private Collection<SampleEntry> sampleEntries = new ArrayList<>();
    private MapList<PerfmonEnum, StatisticEntry> statisticEntries = new MapList<>();

    @PostConstruct
    public void postConstruct() {
        simpleExecutorService.scheduleAtFixedRate(DUMP_DELAY, true, () -> {
            try {
                Collection<StatisticEntry> statisticEntries = analyse();
                for (StatisticEntry statisticEntry : statisticEntries) {
                    this.statisticEntries.put(statisticEntry.getPerfmonEnum(), statisticEntry);
                }
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        }, SimpleExecutorService.Type.UNSPECIFIED);
    }

    public void onEntered(PerfmonEnum perfmonEnum) {
        if (enterTimes.containsKey(perfmonEnum)) {
            log.warning("PerfmonService.onEntered(): onEntered has already been called for " + perfmonEnum);
        }
        enterTimes.put(perfmonEnum, System.currentTimeMillis());
    }

    public void onLeft(PerfmonEnum perfmonEnum) {
        Long startTime = enterTimes.remove(perfmonEnum);
        if (startTime == null) {
            log.warning("PerfmonService.onLeft(): onEntered was not called before " + perfmonEnum);
            return;
        }
        sampleEntries.add(new SampleEntry(perfmonEnum, startTime));
    }

    public MapList<PerfmonEnum, StatisticEntry> getStatisticEntries() {
        return statisticEntries;
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
