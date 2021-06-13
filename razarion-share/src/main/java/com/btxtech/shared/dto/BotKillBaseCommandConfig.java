package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.CollectionReference;
import com.btxtech.shared.datatypes.CollectionReferenceType;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;

/**
 * Created by Beat
 * 01.11.2016.
 */
public class BotKillBaseCommandConfig<T extends BotKillBaseCommandConfig<T>> extends AbstractBotCommandConfig<T> {
    @CollectionReference(CollectionReferenceType.BASE_ITEM)
    private Integer attackerBaseItemTypeId;
    private int dominanceFactor;
    private PlaceConfig spawnPoint;

    public Integer getAttackerBaseItemTypeId() {
        return attackerBaseItemTypeId;
    }

    public T setAttackerBaseItemTypeId(int attackerBaseItemTypeId) {
        this.attackerBaseItemTypeId = attackerBaseItemTypeId;
        return (T) this;
    }

    public int getDominanceFactor() {
        return dominanceFactor;
    }

    public T setDominanceFactor(int dominanceFactor) {
        this.dominanceFactor = dominanceFactor;
        return (T) this;
    }

    public PlaceConfig getSpawnPoint() {
        return spawnPoint;
    }

    public T setSpawnPoint(PlaceConfig spawnPoint) {
        this.spawnPoint = spawnPoint;
        return (T) this;
    }
}
