package com.btxtech.shared.system.perfmon;

import jsinterop.annotations.JsType;

import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * 20.01.2017.
 */
@JsType
public class PerfmonStatistic {
    private PerfmonEnum perfmonEnum;
    private List<PerfmonStatisticEntry> perfmonStatisticEntries;
    private Date timeStamp;
    private String gameSessionUuid;

    public PerfmonEnum getPerfmonEnum() {
        return perfmonEnum;
    }

    @SuppressWarnings("unused") // Called by Angular
    public String getPerfmonEnumString() {
        return perfmonEnum.name();
    }

    public void setPerfmonEnum(PerfmonEnum perfmonEnum) {
        this.perfmonEnum = perfmonEnum;
    }

    public List<PerfmonStatisticEntry> getPerfmonStatisticEntries() {
        return perfmonStatisticEntries;
    }

    @SuppressWarnings("unused") // Called by Angular
    public PerfmonStatisticEntry[] getPerfmonStatisticEntriesArray() {
        return perfmonStatisticEntries.toArray(new PerfmonStatisticEntry[0]);
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
