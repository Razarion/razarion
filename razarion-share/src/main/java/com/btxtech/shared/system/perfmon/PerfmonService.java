package com.btxtech.shared.system.perfmon;

import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.utils.CollectionUtils;

import javax.enterprise.context.ApplicationScoped;
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
    private Logger log = Logger.getLogger(PerfmonService.class.getName());
    private Map<PerfmonEnum, Long> enterTimes = new HashMap<>();
    private Collection<SampleEntry> sampleEntries = new ArrayList<>();

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

    public Collection<StatisticEntry> analyse() {
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
