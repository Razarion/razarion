package com.btxtech.shared.dto;

import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;

/**
 * Created by Beat
 * 07.10.2016.
 */
public class BotHarvestCommandConfig extends AbstractBotCommandConfig<BotHarvestCommandConfig> {
    @CollectionReference(CollectionReferenceType.BASE_ITEM)
    private Integer harvesterItemTypeId;
    private Integer resourceItemTypeId;
    private PlaceConfig resourceSelection;

    public Integer getHarvesterItemTypeId() {
        return harvesterItemTypeId;
    }

    public BotHarvestCommandConfig setHarvesterItemTypeId(Integer harvesterItemTypeId) {
        this.harvesterItemTypeId = harvesterItemTypeId;
        return this;
    }

    public Integer getResourceItemTypeId() {
        return resourceItemTypeId;
    }

    public BotHarvestCommandConfig setResourceItemTypeId(Integer resourceItemTypeId) {
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
