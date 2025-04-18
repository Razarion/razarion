package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;

/**
 * User: beat
 * Date: 22.08.12
 * Time: 17:50
 */
public class GameTipConfig {
    public enum Tip {
        BUILD,
        FABRICATE,
        HARVEST,
        MOVE,
        ATTACK,
        START_PLACER,
        PICK_BOX,
        SPAN_INVENTORY_ITEM,
        SCROLL,
        SCROLL_HOME_BUTTON
        // WATCH_QUEST,
        // LOAD_CONTAINER,
        // UNLOAD_CONTAINER;
    }

    private Tip tip;
    private Integer actor;
    private Integer toCreatedItemTypeId;
    @CollectionReference(CollectionReferenceType.RESOURCE_ITEM)
    private Integer resourceItemTypeId;
    private Integer boxItemTypeId;
    private Integer inventoryItemId;
    private DecimalPosition terrainPositionHint;
    private PlaceConfig placeConfig;
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer scrollMapImageId;

    public Tip getTip() {
        return tip;
    }

    public GameTipConfig setTip(Tip tip) {
        this.tip = tip;
        return this;
    }

    public Integer getActor() {
        return actor;
    }

    public GameTipConfig setActor(Integer actor) {
        this.actor = actor;
        return this;
    }

    public Integer getToCreatedItemTypeId() {
        return toCreatedItemTypeId;
    }

    public GameTipConfig setToCreatedItemTypeId(Integer toCreatedItemTypeId) {
        this.toCreatedItemTypeId = toCreatedItemTypeId;
        return this;
    }

    public DecimalPosition getTerrainPositionHint() {
        return terrainPositionHint;
    }

    public GameTipConfig setTerrainPositionHint(DecimalPosition terrainPositionHint) {
        this.terrainPositionHint = terrainPositionHint;
        return this;
    }

    public Integer getResourceItemTypeId() {
        return resourceItemTypeId;
    }

    public GameTipConfig setResourceItemTypeId(Integer resourceItemTypeId) {
        this.resourceItemTypeId = resourceItemTypeId;
        return this;
    }

    public Integer getBoxItemTypeId() {
        return boxItemTypeId;
    }

    public GameTipConfig setBoxItemTypeId(Integer boxItemTypeId) {
        this.boxItemTypeId = boxItemTypeId;
        return this;
    }

    public Integer getInventoryItemId() {
        return inventoryItemId;
    }

    public GameTipConfig setInventoryItemId(Integer inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
        return this;
    }

    public PlaceConfig getPlaceConfig() {
        return placeConfig;
    }

    public GameTipConfig setPlaceConfig(PlaceConfig placeConfig) {
        this.placeConfig = placeConfig;
        return this;
    }

    public Integer getScrollMapImageId() {
        return scrollMapImageId;
    }

    public GameTipConfig setScrollMapImageId(Integer scrollMapImageId) {
        this.scrollMapImageId = scrollMapImageId;
        return this;
    }
}
