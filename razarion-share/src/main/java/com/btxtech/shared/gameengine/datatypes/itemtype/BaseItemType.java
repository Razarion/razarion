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
import com.btxtech.shared.system.Nullable;
import jsinterop.annotations.JsType;

import java.util.List;

/**
 * User: beat
 * Date: 04.12.2009
 * Time: 20:12:06
 */
@JsType
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
    private Integer spawnAudioId;
    private List<DemolitionStepEffect> demolitionStepEffects;
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer demolitionImageId;
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer buildupTextureId;
    private Integer explosionAudioItemConfigId;
    @CollectionReference(CollectionReferenceType.PARTICLE_SYSTEM)
    private Integer explosionParticleId;

    public PhysicalAreaConfig getPhysicalAreaConfig() {
        return physicalAreaConfig;
    }

    public void setPhysicalAreaConfig(PhysicalAreaConfig physicalAreaConfig) {
        this.physicalAreaConfig = physicalAreaConfig;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getBuildup() {
        return buildup;
    }

    public void setBuildup(int buildup) {
        this.buildup = buildup;
    }

    public int getXpOnKilling() {
        return xpOnKilling;
    }

    public void setXpOnKilling(int xpOnKilling) {
        this.xpOnKilling = xpOnKilling;
    }

    public int getConsumingHouseSpace() {
        return consumingHouseSpace;
    }

    public void setConsumingHouseSpace(int consumingHouseSpace) {
        this.consumingHouseSpace = consumingHouseSpace;
    }

    public @Nullable WeaponType getWeaponType() {
        return weaponType;
    }

    public void setWeaponType(@Nullable WeaponType weaponType) {
        this.weaponType = weaponType;
    }

    public @Nullable FactoryType getFactoryType() {
        return factoryType;
    }

    public void setFactoryType(@Nullable FactoryType factoryType) {
        this.factoryType = factoryType;
    }

    public @Nullable HarvesterType getHarvesterType() {
        return harvesterType;
    }

    public void setHarvesterType(@Nullable HarvesterType harvesterType) {
        this.harvesterType = harvesterType;
    }

    public @Nullable BuilderType getBuilderType() {
        return builderType;
    }

    public void setBuilderType(@Nullable BuilderType builderType) {
        this.builderType = builderType;
    }

    public @Nullable GeneratorType getGeneratorType() {
        return generatorType;
    }

    public void setGeneratorType(@Nullable GeneratorType generatorType) {
        this.generatorType = generatorType;
    }

    public @Nullable ConsumerType getConsumerType() {
        return consumerType;
    }

    public void setConsumerType(@Nullable ConsumerType consumerType) {
        this.consumerType = consumerType;
    }

    public @Nullable ItemContainerType getItemContainerType() {
        return itemContainerType;
    }

    public void setItemContainerType(@Nullable ItemContainerType itemContainerType) {
        this.itemContainerType = itemContainerType;
    }

    public @Nullable HouseType getHouseType() {
        return houseType;
    }

    public void setHouseType(@Nullable HouseType houseType) {
        this.houseType = houseType;
    }

    public @Nullable SpecialType getSpecialType() {
        return specialType;
    }

    public void setSpecialType(@Nullable SpecialType specialType) {
        this.specialType = specialType;
    }

    public Integer getDropBoxItemTypeId() {
        return dropBoxItemTypeId;
    }

    public void setDropBoxItemTypeId(Integer dropBoxItemTypeId) {
        this.dropBoxItemTypeId = dropBoxItemTypeId;
    }

    public double getDropBoxPossibility() {
        return dropBoxPossibility;
    }

    public void setDropBoxPossibility(double dropBoxPossibility) {
        this.dropBoxPossibility = dropBoxPossibility;
    }

    public double getBoxPickupRange() {
        return boxPickupRange;
    }

    public void setBoxPickupRange(double boxPickupRange) {
        this.boxPickupRange = boxPickupRange;
    }

    public Integer getUnlockCrystals() {
        return unlockCrystals;
    }

    public void setUnlockCrystals(Integer unlockCrystals) {
        this.unlockCrystals = unlockCrystals;
    }

    public int getSpawnDurationMillis() {
        return spawnDurationMillis;
    }

    public void setSpawnDurationMillis(int spawnDurationMillis) {
        this.spawnDurationMillis = spawnDurationMillis;
    }

    public Integer getSpawnAudioId() {
        return spawnAudioId;
    }

    public void setSpawnAudioId(Integer spawnAudioId) {
        this.spawnAudioId = spawnAudioId;
    }

    public List<DemolitionStepEffect> getDemolitionStepEffects() {
        return demolitionStepEffects;
    }

    public void setDemolitionStepEffects(List<DemolitionStepEffect> demolitionStepEffects) {
        this.demolitionStepEffects = demolitionStepEffects;
    }

    public Integer getDemolitionImageId() {
        return demolitionImageId;
    }

    public void setDemolitionImageId(Integer demolitionImageId) {
        this.demolitionImageId = demolitionImageId;
    }

    public Integer getBuildupTextureId() {
        return buildupTextureId;
    }

    public void setBuildupTextureId(Integer buildupTextureId) {
        this.buildupTextureId = buildupTextureId;
    }

    public Integer getExplosionAudioItemConfigId() {
        return explosionAudioItemConfigId;
    }

    public void setExplosionAudioItemConfigId(Integer explosionAudioItemConfigId) {
        this.explosionAudioItemConfigId = explosionAudioItemConfigId;
    }

    public @Nullable Integer getExplosionParticleId() {
        return explosionParticleId;
    }

    public void setExplosionParticleId(@Nullable Integer explosionParticleId) {
        this.explosionParticleId = explosionParticleId;
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

    public BaseItemType physicalAreaConfig(PhysicalAreaConfig physicalAreaConfig) {
        setPhysicalAreaConfig(physicalAreaConfig);
        return this;
    }

    public BaseItemType health(int health) {
        setHealth(health);
        return this;
    }

    public BaseItemType price(int price) {
        setPrice(price);
        return this;
    }

    public BaseItemType buildup(int buildup) {
        setBuildup(buildup);
        return this;
    }

    public BaseItemType xpOnKilling(int xpOnKilling) {
        setXpOnKilling(xpOnKilling);
        return this;
    }

    public BaseItemType consumingHouseSpace(int consumingHouseSpace) {
        setConsumingHouseSpace(consumingHouseSpace);
        return this;
    }

    public BaseItemType weaponType(WeaponType weaponType) {
        setWeaponType(weaponType);
        return this;
    }

    public BaseItemType factoryType(FactoryType factoryType) {
        setFactoryType(factoryType);
        return this;
    }

    public BaseItemType harvesterType(HarvesterType harvesterType) {
        setHarvesterType(harvesterType);
        return this;
    }

    public BaseItemType builderType(BuilderType builderType) {
        setBuilderType(builderType);
        return this;
    }

    public BaseItemType generatorType(GeneratorType generatorType) {
        setGeneratorType(generatorType);
        return this;
    }

    public BaseItemType consumerType(ConsumerType consumerType) {
        setConsumerType(consumerType);
        return this;
    }

    public BaseItemType itemContainerType(ItemContainerType itemContainerType) {
        setItemContainerType(itemContainerType);
        return this;
    }

    public BaseItemType houseType(HouseType houseType) {
        setHouseType(houseType);
        return this;
    }

    public BaseItemType specialType(SpecialType specialType) {
        setSpecialType(specialType);
        return this;
    }

    public BaseItemType dropBoxItemTypeId(Integer dropBoxItemTypeId) {
        setDropBoxItemTypeId(dropBoxItemTypeId);
        return this;
    }

    public BaseItemType dropBoxPossibility(double dropBoxPossibility) {
        setDropBoxPossibility(dropBoxPossibility);
        return this;
    }

    public BaseItemType boxPickupRange(double boxPickupRange) {
        setBoxPickupRange(boxPickupRange);
        return this;
    }

    public BaseItemType unlockCrystals(Integer unlockCrystals) {
        setUnlockCrystals(unlockCrystals);
        return this;
    }

    public BaseItemType spawnDurationMillis(int spawnDurationMillis) {
        setSpawnDurationMillis(spawnDurationMillis);
        return this;
    }

    public BaseItemType spawnAudioId(Integer spawnAudioId) {
        setSpawnAudioId(spawnAudioId);
        return this;
    }

    public BaseItemType demolitionStepEffects(List<DemolitionStepEffect> demolitionStepEffects) {
        setDemolitionStepEffects(demolitionStepEffects);
        return this;
    }

    public BaseItemType demolitionImageId(Integer demolitionImageId) {
        setDemolitionImageId(demolitionImageId);
        return this;
    }

    public BaseItemType buildupTextureId(Integer buildupTextureId) {
        setBuildupTextureId(buildupTextureId);
        return this;
    }

    public BaseItemType explosionAudioItemConfigId(Integer explosionAudioItemConfigId) {
        setExplosionAudioItemConfigId(explosionAudioItemConfigId);
        return this;
    }

    public BaseItemType explosionParticleId(Integer explosionParticleId) {
        setExplosionParticleId(explosionParticleId);
        return this;
    }
}
