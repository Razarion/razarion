package com.btxtech.shared.system.perfmon;

import com.btxtech.shared.datatypes.MapList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 04.01.2018.
 */
public class StatisticConsumer {
    private MapList<PerfmonEnum, StatisticEntry> statisticEntries = new MapList<>();

    public void push(StatisticEntry statisticEntry) {
        this.statisticEntries.put(statisticEntry.getPerfmonEnum(), statisticEntry);
        if (this.statisticEntries.get(statisticEntry.getPerfmonEnum()).size() >= PerfmonService.COUNT) {
            this.statisticEntries.get(statisticEntry.getPerfmonEnum()).remove(0);
        }
    }

    public void clear() {
        statisticEntries.clear();
    }

    public List<PerfmonStatistic> peek(String gameSessionUuid) {
        return setupPerfmonStatistic(gameSessionUuid);
    }

    public List<PerfmonStatistic> pull(String gameSessionUuid) {
        List<PerfmonStatistic> result = setupPerfmonStatistic(gameSessionUuid);
        clear();
        return result;
    }

    private List<PerfmonStatistic> setupPerfmonStatistic(String gameSessionUuid) {
        List<PerfmonStatistic> perfmonStatistics = new ArrayList<>();
        for (Map.Entry<PerfmonEnum, List<StatisticEntry>> entry : statisticEntries.getMap().entrySet()) {
            PerfmonStatistic perfmonStatistic = new PerfmonStatistic();
            perfmonStatistic.setTimeStamp(new Date());
            perfmonStatistic.setGameSessionUuid(gameSessionUuid);
            perfmonStatistic.setPerfmonEnum(entry.getKey());
            perfmonStatistic.setPerfmonStatisticEntries(entry.getValue().stream().map(statisticEntry -> {
                PerfmonStatisticEntry perfmonStatisticEntry = new PerfmonStatisticEntry();
                perfmonStatisticEntry.setFrequency(statisticEntry.getFrequency());
                perfmonStatisticEntry.setAvgDuration(statisticEntry.getAvgDuration());
                perfmonStatisticEntry.setSamples(statisticEntry.getSamples());
                perfmonStatisticEntry.setDate(new Date(statisticEntry.getSamplingPeriodStart()));
                return perfmonStatisticEntry;
            }).collect(Collectors.toList()));
            perfmonStatistics.add(perfmonStatistic);
        }
        return perfmonStatistics;
    }
}
