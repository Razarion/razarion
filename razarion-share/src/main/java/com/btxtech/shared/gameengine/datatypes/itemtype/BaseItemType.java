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

/**
 * User: beat
 * Date: 04.12.2009
 * Time: 20:12:06
 */
public class BaseItemType extends ItemType {
    private double radius;
    private int health;
    private int price;
    private int buildup;
    private int xpOnKilling;
    private int consumingHouseSpace;
    private MovableType movableType;
    private WeaponType weaponType;
    private FactoryType factoryType;
    private HarvesterType harvesterType;
    private BuilderType builderType;
    private GeneratorType generatorType;
    private ConsumerType consumerType;
    private SpecialType specialType;
    private ItemContainerType itemContainerType;
    private HouseType houseType;
    private Integer upgradeable;
    private int upgradeProgress;
    private double dropBoxPossibility;
    private int boxPickupRange;
    private Integer unlockCrystals;
    private SpawnItemType spawnItemType;

    public double getRadius() {
        return radius;
    }

    public BaseItemType setRadius(double radius) {
        this.radius = radius;
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

    public double getDropBoxPossibility() {
        return dropBoxPossibility;
    }

    public BaseItemType setDropBoxPossibility(double dropBoxPossibility) {
        this.dropBoxPossibility = dropBoxPossibility;
        return this;
    }

    public int getBoxPickupRange() {
        return boxPickupRange;
    }

    public BaseItemType setBoxPickupRange(int boxPickupRange) {
        this.boxPickupRange = boxPickupRange;
        return this;
    }

    public MovableType getMovableType() {
        return movableType;
    }

    public BaseItemType setMovableType(MovableType movableType) {
        this.movableType = movableType;
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

    public SpecialType getSpecialType() {
        return specialType;
    }

    public BaseItemType setSpecialType(SpecialType specialType) {
        this.specialType = specialType;
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

    public Integer getUpgradeable() {
        return upgradeable;
    }

    public BaseItemType setUpgradeable(Integer upgradeable) {
        this.upgradeable = upgradeable;
        return this;
    }

    public double getUpgradeProgress() {
        return upgradeProgress;
    }

    public BaseItemType setUpgradeProgress(int upgradeProgress) {
        this.upgradeProgress = upgradeProgress;
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

    public boolean isUnlockNeeded() {
        return unlockCrystals != null;
    }

    public Integer getUnlockCrystals() {
        return unlockCrystals;
    }

    public BaseItemType setUnlockCrystals(Integer unlockCrystals) {
        this.unlockCrystals = unlockCrystals;
        return this;
    }

    public SpawnItemType getSpawnItemType() {
        return spawnItemType;
    }

    public BaseItemType setSpawnItemType(SpawnItemType spawnItemType) {
        this.spawnItemType = spawnItemType;
        return this;
    }
}
