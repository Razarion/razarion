/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.shared.gameengine.datatypes.itemtype;

import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;

import java.util.List;

/**
 * User: beat
 * Date: 04.12.2009
 * Time: 20:12:06
 */
public class BaseItemType extends ItemType {
    private PhysicalAreaConfig physicalAreaConfig;
    private int health;
    private int price;
    private int buildup;
    private int xpOnKilling;
    private int consumingHouseSpace;
    private WeaponType weaponType;
    private FactoryType factoryType;
    private HarvesterType harvesterType;
    private BuilderType builderType;
    private GeneratorType generatorType;
    private ConsumerType consumerType;
    private ItemContainerType itemContainerType;
    private HouseType houseType;
    private SpecialType specialType;
    private Integer dropBoxItemTypeId;
    private double dropBoxPossibility;
    private double boxPickupRange;
    private Integer unlockCrystals;
    private int spawnDurationMillis;
    private Integer spawnShape3DId;
    private Integer spawnAudioId;
    private Integer explosionParticleConfigId;
    private List<DemolitionStepEffect> demolitionStepEffects;
    private Integer wreckageShape3DId;
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer demolitionImageId;
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer buildupTextureId;

    public PhysicalAreaConfig getPhysicalAreaConfig() {
        return physicalAreaConfig;
    }

    public BaseItemType setPhysicalAreaConfig(PhysicalAreaConfig physicalAreaConfig) {
        this.physicalAreaConfig = physicalAreaConfig;
        return this;
    }

    public int getHealth() {
        return health;
    }

    public BaseItemType setHealth(int health) {
        this.health = health;
        return this;
    }

    public int getPrice() {
        return price;
    }

    public BaseItemType setPrice(int price) {
        this.price = price;
        return this;
    }

    public Integer getDropBoxItemTypeId() {
        return dropBoxItemTypeId;
    }

    public BaseItemType setDropBoxItemTypeId(Integer dropBoxItemTypeId) {
        this.dropBoxItemTypeId = dropBoxItemTypeId;
        return this;
    }

    public double getDropBoxPossibility() {
        return dropBoxPossibility;
    }

    public BaseItemType setDropBoxPossibility(double dropBoxPossibility) {
        this.dropBoxPossibility = dropBoxPossibility;
        return this;
    }

    public double getBoxPickupRange() {
        return boxPickupRange;
    }

    public BaseItemType setBoxPickupRange(double boxPickupRange) {
        this.boxPickupRange = boxPickupRange;
        return this;
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public BaseItemType setWeaponType(WeaponType weaponType) {
        this.weaponType = weaponType;
        return this;
    }

    public FactoryType getFactoryType() {
        return factoryType;
    }

    public BaseItemType setFactoryType(FactoryType factoryType) {
        this.factoryType = factoryType;
        return this;
    }

    public HarvesterType getHarvesterType() {
        return harvesterType;
    }

    public BaseItemType setHarvesterType(HarvesterType harvesterType) {
        this.harvesterType = harvesterType;
        return this;
    }

    public BuilderType getBuilderType() {
        return builderType;
    }

    public BaseItemType setBuilderType(BuilderType builderType) {
        this.builderType = builderType;
        return this;
    }

    public GeneratorType getGeneratorType() {
        return generatorType;
    }

    public BaseItemType setGeneratorType(GeneratorType generatorType) {
        this.generatorType = generatorType;
        return this;
    }

    public ConsumerType getConsumerType() {
        return consumerType;
    }

    public BaseItemType setConsumerType(ConsumerType consumerType) {
        this.consumerType = consumerType;
        return this;
    }

    public ItemContainerType getItemContainerType() {
        return itemContainerType;
    }

    public BaseItemType setItemContainerType(ItemContainerType itemContainerType) {
        this.itemContainerType = itemContainerType;
        return this;
    }

    public HouseType getHouseType() {
        return houseType;
    }

    public BaseItemType setHouseType(HouseType houseType) {
        this.houseType = houseType;
        return this;
    }

    public SpecialType getSpecialType() {
        return specialType;
    }

    public BaseItemType setSpecialType(SpecialType specialType) {
        this.specialType = specialType;
        return this;
    }

    public int getBuildup() {
        return buildup;
    }

    public BaseItemType setBuildup(int buildup) {
        this.buildup = buildup;
        return this;
    }

    public int getXpOnKilling() {
        return xpOnKilling;
    }

    public BaseItemType setXpOnKilling(int xpOnKilling) {
        this.xpOnKilling = xpOnKilling;
        return this;
    }

    public int getConsumingHouseSpace() {
        return consumingHouseSpace;
    }

    public BaseItemType setConsumingHouseSpace(int consumingHouseSpace) {
        this.consumingHouseSpace = consumingHouseSpace;
        return this;
    }

    public boolean unlockNeeded() {
        return unlockCrystals != null;
    }

    public Integer getUnlockCrystals() {
        return unlockCrystals;
    }

    public BaseItemType setUnlockCrystals(Integer unlockCrystals) {
        this.unlockCrystals = unlockCrystals;
        return this;
    }

    public int getSpawnDurationMillis() {
        return spawnDurationMillis;
    }

    public BaseItemType setSpawnDurationMillis(int spawnDurationMillis) {
        this.spawnDurationMillis = spawnDurationMillis;
        return this;
    }

    public Integer getSpawnShape3DId() {
        return spawnShape3DId;
    }

    public BaseItemType setSpawnShape3DId(Integer spawnShape3DId) {
        this.spawnShape3DId = spawnShape3DId;
        return this;
    }

    public Integer getSpawnAudioId() {
        return spawnAudioId;
    }

    public BaseItemType setSpawnAudioId(Integer spawnAudioId) {
        this.spawnAudioId = spawnAudioId;
        return this;
    }

    public Integer getExplosionParticleConfigId() {
        return explosionParticleConfigId;
    }

    public BaseItemType setExplosionParticleConfigId(Integer explosionParticleConfigId) {
        this.explosionParticleConfigId = explosionParticleConfigId;
        return this;
    }

    public List<DemolitionStepEffect> getDemolitionStepEffects() {
        return demolitionStepEffects;
    }

    public BaseItemType setDemolitionStepEffects(List<DemolitionStepEffect> demolitionStepEffects) {
        this.demolitionStepEffects = demolitionStepEffects;
        return this;
    }

    public DemolitionStepEffect getDemolitionStepEffect(int step) {
        if (demolitionStepEffects == null) {
            throw new IllegalStateException("No demolition configured for: " + this);
        }
        return demolitionStepEffects.get(step);
    }

    public int getDemolitionStep(double health) {
        if (health >= 1.0) {
            throw new IllegalArgumentException("SyncBaseItem must not be healthy");
        }

        if (health >= 0.0) {
            int step = (int) (demolitionStepEffects.size() * (1.0 - health));
            if (step >= demolitionStepEffects.size()) {
                return demolitionStepEffects.size() - 1;
            } else if (step < 0) {
                return 0;
            } else {
                return step;
            }
        } else {
            return demolitionStepEffects.size() - 1;
        }
    }

    public Integer getWreckageShape3DId() {
        return wreckageShape3DId;
    }

    public BaseItemType setWreckageShape3DId(Integer wreckageShape3DId) {
        this.wreckageShape3DId = wreckageShape3DId;
        return this;
    }

    public Integer getDemolitionImageId() {
        return demolitionImageId;
    }

    public BaseItemType setDemolitionImageId(Integer demolitionImageId) {
        this.demolitionImageId = demolitionImageId;
        return this;
    }

    public Integer getBuildupTextureId() {
        return buildupTextureId;
    }

    public BaseItemType setBuildupTextureId(Integer buildupTextureId) {
        this.buildupTextureId = buildupTextureId;
        return this;
    }

    public static int nameComparator(BaseItemType b1, BaseItemType b2) {
        if (b1.getInternalName() == null && b2.getInternalName() == null) {
            return 0;
        }
        if (b1.getInternalName() == null && b2.getInternalName() != null) {
            return 1;
        }
        if (b1.getInternalName() != null && b2.getInternalName() == null) {
            return -1;
        }
        return b1.getInternalName().compareTo(b2.getInternalName());
    }
}
