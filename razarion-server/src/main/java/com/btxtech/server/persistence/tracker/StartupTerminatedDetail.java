package com.btxtech.server.persistence.tracker;

import java.util.Date;

/**
 * Created by Beat
 * on 25.07.2017.
 */
public class StartupTerminatedDetail {
    private boolean successful;
    private int totalTime;
    private Date timeStamp;

    public boolean isSuccessful() {
        return successful;
    }

    public StartupTerminatedDetail setSuccessful(boolean successful) {
        this.successful = successful;
        return this;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public StartupTerminatedDetail setTotalTime(int totalTime) {
        this.totalTime = totalTime;
        return this;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public StartupTerminatedDetail setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }
}
