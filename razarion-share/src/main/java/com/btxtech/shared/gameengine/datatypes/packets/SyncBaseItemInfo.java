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

package com.btxtech.shared.gameengine.datatypes.packets;


import com.btxtech.shared.datatypes.DecimalPosition;

import java.util.Iterator;
import java.util.List;

/**
 * User: beat
 * Date: 23.11.2009
 * Time: 21:54:50
 */
public class SyncBaseItemInfo {
    private int id;
    private int itemTypeId;
    private SyncPhysicalAreaInfo syncPhysicalAreaInfo;
    private int baseId;
    private DecimalPosition toBeBuildPosition;
    private Integer toBeBuiltTypeId;
    private Integer currentBuildup;
    private Double factoryBuildupProgress;
    private Integer target;
    private double health;
    private double buildup;
    private Boolean followTarget;
    private double reloadProgress;
    private DecimalPosition spawnPoint;
    private DecimalPosition rallyPoint;
    private List<Integer> containedItems;
    private Integer targetContainer;
    private Integer containedIn;
    private DecimalPosition unloadPos;
    private double spawnProgress;
    private Integer syncBoxItemId;
    private Double turretAngle;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(int itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public SyncPhysicalAreaInfo getSyncPhysicalAreaInfo() {
        return syncPhysicalAreaInfo;
    }

    public void setSyncPhysicalAreaInfo(SyncPhysicalAreaInfo syncPhysicalAreaInfo) {
        this.syncPhysicalAreaInfo = syncPhysicalAreaInfo;
    }

    public int getBaseId() {
        return baseId;
    }

    public void setBaseId(int baseId) {
        this.baseId = baseId;
    }

    public DecimalPosition getToBeBuildPosition() {
        return toBeBuildPosition;
    }

    public void setToBeBuildPosition(DecimalPosition toBeBuildPosition) {
        this.toBeBuildPosition = toBeBuildPosition;
    }

    public Integer getToBeBuiltTypeId() {
        return toBeBuiltTypeId;
    }

    public void setToBeBuiltTypeId(Integer toBeBuiltTypeId) {
        this.toBeBuiltTypeId = toBeBuiltTypeId;
    }

    public Integer getCurrentBuildup() {
        return currentBuildup;
    }

    public void setCurrentBuildup(Integer currentBuildup) {
        this.currentBuildup = currentBuildup;
    }

    public Double getFactoryBuildupProgress() {
        return factoryBuildupProgress;
    }

    public void setFactoryBuildupProgress(Double factoryBuildupProgress) {
        this.factoryBuildupProgress = factoryBuildupProgress;
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public double getBuildup() {
        return buildup;
    }

    public void setBuildup(double buildup) {
        this.buildup = buildup;
    }

    public Boolean getFollowTarget() {
        return followTarget;
    }

    public void setFollowTarget(Boolean followTarget) {
        this.followTarget = followTarget;
    }

    public double getReloadProgress() {
        return reloadProgress;
    }

    public void setReloadProgress(double reloadProgress) {
        this.reloadProgress = reloadProgress;
    }

    public DecimalPosition getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(DecimalPosition spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    public DecimalPosition getRallyPoint() {
        return rallyPoint;
    }

    public void setRallyPoint(DecimalPosition rallyPoint) {
        this.rallyPoint = rallyPoint;
    }

    public List<Integer> getContainedItems() {
        return containedItems;
    }

    public void setContainedItems(List<Integer> containedItems) {
        this.containedItems = containedItems;
    }

    public Integer getTargetContainer() {
        return targetContainer;
    }

    public void setTargetContainer(Integer targetContainer) {
        this.targetContainer = targetContainer;
    }

    public Integer getContainedIn() {
        return containedIn;
    }

    public void setContainedIn(Integer containedIn) {
        this.containedIn = containedIn;
    }

    public DecimalPosition getUnloadPos() {
        return unloadPos;
    }

    public void setUnloadPos(DecimalPosition unloadPos) {
        this.unloadPos = unloadPos;
    }

    public double getSpawnProgress() {
        return spawnProgress;
    }

    public void setSpawnProgress(double spawnProgress) {
        this.spawnProgress = spawnProgress;
    }

    public Integer getSyncBoxItemId() {
        return syncBoxItemId;
    }

    public void setSyncBoxItemId(Integer syncBoxItemId) {
        this.syncBoxItemId = syncBoxItemId;
    }

    public Double getTurretAngle() {
        return turretAngle;
    }

    public void setTurretAngle(Double turretAngle) {
        this.turretAngle = turretAngle;
    }

    public SyncBaseItemInfo id(int id) {
        setId(id);
        return this;
    }

    public SyncBaseItemInfo itemTypeId(int itemTypeId) {
        setItemTypeId(itemTypeId);
        return this;
    }

    public SyncBaseItemInfo syncPhysicalAreaInfo(SyncPhysicalAreaInfo syncPhysicalAreaInfo) {
        setSyncPhysicalAreaInfo(syncPhysicalAreaInfo);
        return this;
    }

    public SyncBaseItemInfo baseId(int baseId) {
        setBaseId(baseId);
        return this;
    }

    public SyncBaseItemInfo toBeBuildPosition(DecimalPosition toBeBuildPosition) {
        setToBeBuildPosition(toBeBuildPosition);
        return this;
    }

    public SyncBaseItemInfo toBeBuiltTypeId(Integer toBeBuiltTypeId) {
        setToBeBuiltTypeId(toBeBuiltTypeId);
        return this;
    }

    public SyncBaseItemInfo currentBuildup(Integer currentBuildup) {
        setCurrentBuildup(currentBuildup);
        return this;
    }

    public SyncBaseItemInfo factoryBuildupProgress(Double factoryBuildupProgress) {
        setFactoryBuildupProgress(factoryBuildupProgress);
        return this;
    }

    public SyncBaseItemInfo target(Integer target) {
        setTarget(target);
        return this;
    }

    public SyncBaseItemInfo health(double health) {
        setHealth(health);
        return this;
    }

    public SyncBaseItemInfo buildup(double buildup) {
        setBuildup(buildup);
        return this;
    }

    public SyncBaseItemInfo followTarget(Boolean followTarget) {
        setFollowTarget(followTarget);
        return this;
    }

    public SyncBaseItemInfo reloadProgress(double reloadProgress) {
        setReloadProgress(reloadProgress);
        return this;
    }

    public SyncBaseItemInfo spawnPoint(DecimalPosition spawnPoint) {
        setSpawnPoint(spawnPoint);
        return this;
    }

    public SyncBaseItemInfo rallyPoint(DecimalPosition rallyPoint) {
        setRallyPoint(rallyPoint);
        return this;
    }

    public SyncBaseItemInfo containedItems(List<Integer> containedItems) {
        setContainedItems(containedItems);
        return this;
    }

    public SyncBaseItemInfo targetContainer(Integer targetContainer) {
        setTargetContainer(targetContainer);
        return this;
    }

    public SyncBaseItemInfo containedIn(Integer containedIn) {
        setContainedIn(containedIn);
        return this;
    }

    public SyncBaseItemInfo unloadPos(DecimalPosition unloadPos) {
        setUnloadPos(unloadPos);
        return this;
    }

    public SyncBaseItemInfo spawnProgress(double spawnProgress) {
        setSpawnProgress(spawnProgress);
        return this;
    }

    public SyncBaseItemInfo syncBoxItemId(Integer syncBoxItemId) {
        setSyncBoxItemId(syncBoxItemId);
        return this;
    }

    public SyncBaseItemInfo turretAngle(Double turretAngle) {
        setTurretAngle(turretAngle);
        return this;
    }

    @Override
    public String toString() {
        return " SyncItemInfo: " + id +
                " itemTypeId:" + itemTypeId +
                baseId +
                " syncPhysicalAreaInfo:" + syncPhysicalAreaInfo +
                " toBeBuildPosition:" + toBeBuildPosition +
                " toBeBuiltTypeId:" + toBeBuiltTypeId +
                " currentBuildup:" + currentBuildup +
                " buildupProgress:" + factoryBuildupProgress +
                " target:" + target +
                " health:" + health +
                " buildup:" + buildup +
                " followTarget:" + followTarget +
                " reloadProgress:" + reloadProgress +
                " rallyPoint:" + rallyPoint +
                " containedItems:" + intCollectionAsString() +
                " targetContainer:" + targetContainer +
                " containedIn:" + containedIn +
                " unloadPos:" + unloadPos;
    }

    private String intCollectionAsString() {
        StringBuilder builder = new StringBuilder();
        if (containedItems != null) {
            builder.append("{");
            Iterator<Integer> iterator = containedItems.iterator();
            while (iterator.hasNext()) {
                builder.append(iterator.next());
                if (iterator.hasNext()) {
                    builder.append(", ");
                }
            }
            builder.append("}");
        } else {
            builder.append("{-}");
        }
        return builder.toString();
    }

}
