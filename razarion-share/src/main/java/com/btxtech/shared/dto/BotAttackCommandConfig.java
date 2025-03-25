package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;

/**
 * Created by Beat
 * 15.10.2016.
 */
public class BotAttackCommandConfig extends AbstractBotCommandConfig<BotAttackCommandConfig> {
    private Integer targetItemTypeId;
    private PlaceConfig targetSelection;
    private Integer actorItemTypeId;

    public Integer getTargetItemTypeId() {
        return targetItemTypeId;
    }

    public BotAttackCommandConfig setTargetItemTypeId(Integer targetItemTypeId) {
        this.targetItemTypeId = targetItemTypeId;
        return this;
    }

    public PlaceConfig getTargetSelection() {
        return targetSelection;
    }

    public BotAttackCommandConfig setTargetSelection(PlaceConfig targetSelection) {
        this.targetSelection = targetSelection;
        return this;
    }

    public Integer getActorItemTypeId() {
        return actorItemTypeId;
    }

    public BotAttackCommandConfig setActorItemTypeId(Integer actorItemTypeId) {
        this.actorItemTypeId = actorItemTypeId;
        return this;
    }
}
