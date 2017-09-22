package com.btxtech.shared.gameengine.datatypes.config;

/**
 * Created by Beat
 * on 22.09.2017.
 */
public class LevelUnlockConfig {
    private int id;
    private String internalName;
    private Integer baseItemType;
    private int baseItemTypeCount;
    private int crystalCost;

    public int getId() {
        return id;
    }

    public LevelUnlockConfig setId(int id) {
        this.id = id;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public LevelUnlockConfig setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public Integer getBaseItemType() {
        return baseItemType;
    }

    public LevelUnlockConfig setBaseItemType(Integer baseItemType) {
        this.baseItemType = baseItemType;
        return this;
    }

    public int getBaseItemTypeCount() {
        return baseItemTypeCount;
    }

    public LevelUnlockConfig setBaseItemTypeCount(int baseItemTypeCount) {
        this.baseItemTypeCount = baseItemTypeCount;
        return this;
    }

    public int getCrystalCost() {
        return crystalCost;
    }

    public LevelUnlockConfig setCrystalCost(int crystalCost) {
        this.crystalCost = crystalCost;
        return this;
    }
}
