package com.btxtech.shared.gameengine.datatypes.config.bot;

/**
 * Created by Beat
 * on 23.03.2018.
 */
public class BotSceneConflictConfig {
    private int id;
    private double minDistance;
    private double maxDistance;
    private Integer targetBaseItemTypeId;
    private BotConfig botConfig;

    public int getId() {
        return id;
    }

    public BotSceneConflictConfig setId(int id) {
        this.id = id;
        return this;
    }

    public double getMinDistance() {
        return minDistance;
    }

    public BotSceneConflictConfig setMinDistance(double minDistance) {
        this.minDistance = minDistance;
        return this;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public BotSceneConflictConfig setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
        return this;
    }

    public Integer getTargetBaseItemTypeId() {
        return targetBaseItemTypeId;
    }

    public BotSceneConflictConfig setTargetBaseItemTypeId(Integer targetBaseItemTypeId) {
        this.targetBaseItemTypeId = targetBaseItemTypeId;
        return this;
    }

    public BotConfig getBotConfig() {
        return botConfig;
    }

    public BotSceneConflictConfig setBotConfig(BotConfig botConfig) {
        this.botConfig = botConfig;
        return this;
    }

}
