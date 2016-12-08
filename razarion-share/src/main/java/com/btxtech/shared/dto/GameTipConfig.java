package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        SCROLL,
        WATCH_QUEST,
        LOAD_CONTAINER,
        UNLOAD_CONTAINER;
    }

    private Tip tip;
    private int actor;
    private int target;
    private int toBeBuiltId;
    private int resourceId;
    private DecimalPosition terrainPositionHint;
    private PlaceConfig placeConfig;

    public Tip getTip() {
        return tip;
    }

    public GameTipConfig setTip(Tip tip) {
        this.tip = tip;
        return this;
    }

    public int getActor() {
        return actor;
    }

    public GameTipConfig setActor(int actor) {
        this.actor = actor;
        return this;
    }

    public int getTarget() {
        return target;
    }

    public GameTipConfig setTarget(int target) {
        this.target = target;
        return this;
    }

    public int getToBeBuiltId() {
        return toBeBuiltId;
    }

    public GameTipConfig setToBeBuiltId(int toBeBuiltId) {
        this.toBeBuiltId = toBeBuiltId;
        return this;
    }

    public DecimalPosition getTerrainPositionHint() {
        return terrainPositionHint;
    }

    public GameTipConfig setTerrainPositionHint(DecimalPosition terrainPositionHint) {
        this.terrainPositionHint = terrainPositionHint;
        return this;
    }

    public int getResourceId() {
        return resourceId;
    }

    public GameTipConfig setResourceId(int resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public PlaceConfig getPlaceConfig() {
        return placeConfig;
    }

    public GameTipConfig setPlaceConfig(PlaceConfig placeConfig) {
        this.placeConfig = placeConfig;
        return this;
    }
}
