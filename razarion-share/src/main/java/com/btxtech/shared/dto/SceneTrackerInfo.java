package com.btxtech.shared.dto;

import java.util.Date;

/**
 * Created by Beat
 * 03.03.2017.
 */
public class SceneTrackerInfo {
    private int sceneId;
    private String gameSessionUuid;
    private Date startTime;
    private int duration;

    public int getSceneId() {
        return sceneId;
    }

    public SceneTrackerInfo setSceneId(int sceneId) {
        this.sceneId = sceneId;
        return this;
    }

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }

    public SceneTrackerInfo setGameSessionUuid(String gameSessionUuid) {
        this.gameSessionUuid = gameSessionUuid;
        return this;
    }

    public Date getStartTime() {
        return startTime;
    }

    public SceneTrackerInfo setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public SceneTrackerInfo setDuration(int duration) {
        this.duration = duration;
        return this;
    }
}
