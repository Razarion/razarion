package com.btxtech.shared.gameengine.datatypes.config.bot;

import java.util.Objects;

/**
 * Created by Beat
 * on 23.03.2018.
 */
public class BotSceneConflictConfig {
    private Integer id;
    // Provocation to reach this conflict settings
    private int enterKills; // ms
    private int enterDuration;
    // Calm down to leave this conflict step
    private int leaveNoKillDuration; // ms
    // Bot start settings
    private Integer rePopMillis;
    private double minDistance;
    private double maxDistance;
    private Integer targetBaseItemTypeId;
    private BotConfig botConfig;
    // Bot stop settings
    private Integer stopKills;
    private Integer stopMillis;

    public Integer getId() {
        return id;
    }

    public BotSceneConflictConfig setId(Integer id) {
        this.id = id;
        return this;
    }

    public int getEnterKills() {
        return enterKills;
    }

    public BotSceneConflictConfig setEnterKills(int enterKills) {
        this.enterKills = enterKills;
        return this;
    }

    public int getEnterDuration() {
        return enterDuration;
    }

    public BotSceneConflictConfig setEnterDuration(int enterDuration) {
        this.enterDuration = enterDuration;
        return this;
    }

    public int getLeaveNoKillDuration() {
        return leaveNoKillDuration;
    }

    public BotSceneConflictConfig setLeaveNoKillDuration(int leaveNoKillDuration) {
        this.leaveNoKillDuration = leaveNoKillDuration;
        return this;
    }

    public Integer getRePopMillis() {
        return rePopMillis;
    }

    public BotSceneConflictConfig setRePopMillis(Integer rePopMillis) {
        this.rePopMillis = rePopMillis;
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

    public Integer getStopKills() {
        return stopKills;
    }

    public BotSceneConflictConfig setStopKills(Integer stopKills) {
        this.stopKills = stopKills;
        return this;
    }

    public Integer getStopMillis() {
        return stopMillis;
    }

    public BotSceneConflictConfig setStopMillis(Integer stopMillis) {
        this.stopMillis = stopMillis;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BotSceneConflictConfig that = (BotSceneConflictConfig) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
