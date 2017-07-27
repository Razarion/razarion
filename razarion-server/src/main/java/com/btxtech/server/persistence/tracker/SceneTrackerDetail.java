package com.btxtech.server.persistence.tracker;

import java.util.Date;

/**
 * Created by Beat
 * on 27.07.2017.
 */
public class SceneTrackerDetail {
    private Date clientStartTime;
    private String internalName;
    private int duration;

    public Date getClientStartTime() {
        return clientStartTime;
    }

    public SceneTrackerDetail setClientStartTime(Date clientStartTime) {
        this.clientStartTime = clientStartTime;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public SceneTrackerDetail setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public SceneTrackerDetail setDuration(int duration) {
        this.duration = duration;
        return this;
    }
}
