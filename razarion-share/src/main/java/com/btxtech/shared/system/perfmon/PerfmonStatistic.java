package com.btxtech.shared.system.perfmon;

import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * 20.01.2017.
 */
public class PerfmonStatistic {
    private PerfmonEnum perfmonEnum;
    private List<PerfmonStatisticEntry> perfmonStatisticEntries;
    private Date timeStamp;
    private String gameSessionUuid;

    public PerfmonEnum getPerfmonEnum() {
        return perfmonEnum;
    }

    public void setPerfmonEnum(PerfmonEnum perfmonEnum) {
        this.perfmonEnum = perfmonEnum;
    }

    public List<PerfmonStatisticEntry> getPerfmonStatisticEntries() {
        return perfmonStatisticEntries;
    }

    public void setPerfmonStatisticEntries(List<PerfmonStatisticEntry> perfmonStatisticEntries) {
        this.perfmonStatisticEntries = perfmonStatisticEntries;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }

    public void setGameSessionUuid(String gameSessionUuid) {
        this.gameSessionUuid = gameSessionUuid;
    }
}
