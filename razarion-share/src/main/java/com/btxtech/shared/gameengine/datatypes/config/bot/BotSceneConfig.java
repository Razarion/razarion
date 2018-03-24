package com.btxtech.shared.gameengine.datatypes.config.bot;

import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;

import java.util.List;

/**
 * Created by Beat
 * on 19.03.2018.
 */
public class BotSceneConfig implements ObjectNameIdProvider {
    private int id;
    private String internalName;
    private int scheduleTimeMillis;
    private int killThreshold;
    private List<Integer> botIdsToWatch;
    private List<BotSceneConflictConfig> botSceneConflictConfigs;

    public int getId() {
        return id;
    }

    public BotSceneConfig setId(int id) {
        this.id = id;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public BotSceneConfig setInternalName(String internalName) {
        this.internalName = internalName;
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

    public List<BotSceneConflictConfig> getBotSceneConflictConfigs() {
        return botSceneConflictConfigs;
    }

    public BotSceneConfig setBotSceneConflictConfigs(List<BotSceneConflictConfig> botSceneConflictConfigs) {
        this.botSceneConflictConfigs = botSceneConflictConfigs;
        return this;
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(id, internalName);
    }

    @Override
    public String toString() {
        return "BotConfig" + internalName + "(" + id + ")";
    }

}
