package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;

/**
 * Created by Beat
 * 07.10.2016.
 */
public class BotHarvestCommandConfig extends AbstractBotCommandConfig<BotHarvestCommandConfig> {
    private int harvesterItemTypeId;
    private int resourceItemTypeId;
    private PlaceConfig resourceSelection;

    public int getHarvesterItemTypeId() {
        return harvesterItemTypeId;
    }

    public BotHarvestCommandConfig setHarvesterItemTypeId(int harvesterItemTypeId) {
        this.harvesterItemTypeId = harvesterItemTypeId;
        return this;
    }

    public int getResourceItemTypeId() {
        return resourceItemTypeId;
    }

    public BotHarvestCommandConfig setResourceItemTypeId(int resourceItemTypeId) {
        this.resourceItemTypeId = resourceItemTypeId;
        return this;
    }

    public PlaceConfig getResourceSelection() {
        return resourceSelection;
    }

    public BotHarvestCommandConfig setResourceSelection(PlaceConfig resourceSelection) {
        this.resourceSelection = resourceSelection;
        return this;
    }
}
