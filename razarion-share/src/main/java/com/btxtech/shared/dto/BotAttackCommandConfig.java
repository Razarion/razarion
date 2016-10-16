package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;

/**
 * Created by Beat
 * 15.10.2016.
 */
public class BotAttackCommandConfig extends AbstractBotCommandConfig<BotAttackCommandConfig> {
    private int targetItemTypeId;
    private PlaceConfig targetSelection;
    private int actorItemTypeId;

    public int getTargetItemTypeId() {
        return targetItemTypeId;
    }

    public BotAttackCommandConfig setTargetItemTypeId(int targetItemTypeId) {
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

    public int getActorItemTypeId() {
        return actorItemTypeId;
    }

    public BotAttackCommandConfig setActorItemTypeId(int actorItemTypeId) {
        this.actorItemTypeId = actorItemTypeId;
        return this;
    }
}
