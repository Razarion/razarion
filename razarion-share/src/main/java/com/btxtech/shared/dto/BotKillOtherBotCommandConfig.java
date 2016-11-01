package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;

/**
 * Created by Beat
 * 01.11.2016.
 */
public class BotKillOtherBotCommandConfig extends AbstractBotCommandConfig<BotKillOtherBotCommandConfig> {
    private int targetBotId;
    private int attackerBaseItemTypeId;
    private int dominanceFactor;
    private PlaceConfig spawnPoint;

    public int getTargetBotId() {
        return targetBotId;
    }

    public BotKillOtherBotCommandConfig setTargetBotId(int targetBotId) {
        this.targetBotId = targetBotId;
        return this;
    }

    public int getAttackerBaseItemTypeId() {
        return attackerBaseItemTypeId;
    }

    public BotKillOtherBotCommandConfig setAttackerBaseItemTypeId(int attackerBaseItemTypeId) {
        this.attackerBaseItemTypeId = attackerBaseItemTypeId;
        return this;
    }

    public int getDominanceFactor() {
        return dominanceFactor;
    }

    public BotKillOtherBotCommandConfig setDominanceFactor(int dominanceFactor) {
        this.dominanceFactor = dominanceFactor;
        return this;
    }

    public PlaceConfig getSpawnPoint() {
        return spawnPoint;
    }

    public BotKillOtherBotCommandConfig setSpawnPoint(PlaceConfig spawnPoint) {
        this.spawnPoint = spawnPoint;
        return this;
    }
}
