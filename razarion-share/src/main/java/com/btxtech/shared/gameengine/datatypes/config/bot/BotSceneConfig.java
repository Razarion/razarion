package com.btxtech.shared.gameengine.datatypes.config.bot;

import java.util.List;

/**
 * Created by Beat
 * on 19.03.2018.
 */
public class BotSceneConfig {
    private int id;
    private String name;
    private int scheduleTimeMillis;
    private int killThreshold;
    private List<Integer> botIdsToWatch;
    private BotSceneConflictConfig botSceneConflictConfig;

    public int getId() {
        return id;
    }

    public BotSceneConfig setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public BotSceneConfig setName(String name) {
        this.name = name;
        return this;
    }

    public int getScheduleTimeMillis() {
        return scheduleTimeMillis;
    }

    public BotSceneConfig setScheduleTimeMillis(int scheduleTimeMillis) {
        this.scheduleTimeMillis = scheduleTimeMillis;
        return this;
    }

    public int getKillThreshold() {
        return killThreshold;
    }

    public BotSceneConfig setKillThreshold(int killThreshold) {
        this.killThreshold = killThreshold;
        return this;
    }

    public List<Integer> getBotIdsToWatch() {
        return botIdsToWatch;
    }

    public BotSceneConfig setBotIdsToWatch(List<Integer> botIdsToWatch) {
        this.botIdsToWatch = botIdsToWatch;
        return this;
    }

    public BotSceneConflictConfig getBotSceneConflictConfig() {
        return botSceneConflictConfig;
    }

    public BotSceneConfig setBotSceneConflictConfig(BotSceneConflictConfig botSceneConflictConfig) {
        this.botSceneConflictConfig = botSceneConflictConfig;
        return this;
    }

    @Override
    public String toString() {
        return "BotConfig" + name + "(" + id + ")";
    }

}
