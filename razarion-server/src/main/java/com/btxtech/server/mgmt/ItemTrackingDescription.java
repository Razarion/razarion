package com.btxtech.server.mgmt;

import java.util.Map;

/**
 * Created by Beat
 * on 06.04.2018.
 */
public class ItemTrackingDescription {
    private Map<Integer, String> baseItemTypeNames;
    private Map<Integer, String> boxItemTypeNames;
    private Map<Integer, String> resourceItemTypeNames;
    private Map<Integer, String> humanPlayerIdNames;
    private Map<Integer, String> botNames;

    public Map<Integer, String> getBaseItemTypeNames() {
        return baseItemTypeNames;
    }

    public ItemTrackingDescription setBaseItemTypeNames(Map<Integer, String> baseItemTypeNames) {
        this.baseItemTypeNames = baseItemTypeNames;
        return this;
    }

    public Map<Integer, String> getBoxItemTypeNames() {
        return boxItemTypeNames;
    }

    public ItemTrackingDescription setBoxItemTypeNames(Map<Integer, String> boxItemTypeNames) {
        this.boxItemTypeNames = boxItemTypeNames;
        return this;
    }

    public Map<Integer, String> getResourceItemTypeNames() {
        return resourceItemTypeNames;
    }

    public ItemTrackingDescription setResourceItemTypeNames(Map<Integer, String> resourceItemTypeNames) {
        this.resourceItemTypeNames = resourceItemTypeNames;
        return this;
    }

    public Map<Integer, String> getHumanPlayerIdNames() {
        return humanPlayerIdNames;
    }

    public ItemTrackingDescription setHumanPlayerIdNames(Map<Integer, String> humanPlayerIdNames) {
        this.humanPlayerIdNames = humanPlayerIdNames;
        return this;
    }

    public Map<Integer, String> getBotNames() {
        return botNames;
    }

    public ItemTrackingDescription setBotNames(Map<Integer, String> botNames) {
        this.botNames = botNames;
        return this;
    }
}
