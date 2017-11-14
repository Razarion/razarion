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
import com.btxtech.shared.datatypes.Index;

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
    private DecimalPosition rallyPoint;
    private List<Integer> containedItems;
    private Integer targetContainer;
    private Integer containedIn;
    private DecimalPosition unloadPos;
    private Index targetPosition;
    private double spawnProgress;
    private Integer syncBoxItemId;

    public int getId() {
        return id;
    }

    public SyncBaseItemInfo setId(int id) {
        this.id = id;
        return this;
    }

    public SyncPhysicalAreaInfo getSyncPhysicalAreaInfo() {
        return syncPhysicalAreaInfo;
    }

    public SyncBaseItemInfo setSyncPhysicalAreaInfo(SyncPhysicalAreaInfo syncPhysicalAreaInfo) {
        this.syncPhysicalAreaInfo = syncPhysicalAreaInfo;
        return this;
    }

    public int getItemTypeId() {
        return itemTypeId;
    }

    public SyncBaseItemInfo setItemTypeId(int itemTypeId) {
        this.itemTypeId = itemTypeId;
        return this;
    }

    public int getBaseId() {
        return baseId;
    }

    public SyncBaseItemInfo setBaseId(int baseId) {
        this.baseId = baseId;
        return this;
    }

    public DecimalPosition getToBeBuildPosition() {
        return toBeBuildPosition;
    }

    public Integer getToBeBuiltTypeId() {
        return toBeBuiltTypeId;
    }

    public Integer getCurrentBuildup() {
        return currentBuildup;
    }

    public Double getFactoryBuildupProgress() {
        return factoryBuildupProgress;
    }

    public Integer getTarget() {
        return target;
    }

    public double getHealth() {
        return health;
    }

    public Boolean isFollowTarget() {
        return followTarget;
    }

    public SyncBaseItemInfo setToBeBuildPosition(DecimalPosition toBeBuildPosition) {
        this.toBeBuildPosition = toBeBuildPosition;
        return this;
    }

    public SyncBaseItemInfo setToBeBuiltTypeId(Integer toBeBuiltTypeId) {
        this.toBeBuiltTypeId = toBeBuiltTypeId;
        return this;
    }

    public SyncBaseItemInfo setCurrentBuildup(Integer currentBuildup) {
        this.currentBuildup = currentBuildup;
        return this;
    }

    public SyncBaseItemInfo setFactoryBuildupProgress(Double factoryBuildupProgress) {
        this.factoryBuildupProgress = factoryBuildupProgress;
        return this;
    }

    public SyncBaseItemInfo setTarget(Integer target) {
        this.target = target;
        return this;
    }

    public SyncBaseItemInfo setHealth(double health) {
        this.health = health;
        return this;
    }

    public SyncBaseItemInfo setFollowTarget(boolean followTarget) {
        this.followTarget = followTarget;
        return this;
    }

    public double getReloadProgress() {
        return reloadProgress;
    }

    public SyncBaseItemInfo setReloadProgress(double reloadProgress) {
        this.reloadProgress = reloadProgress;
        return this;
    }

    public DecimalPosition getRallyPoint() {
        return rallyPoint;
    }

    public SyncBaseItemInfo setRallyPoint(DecimalPosition rallyPoint) {
        this.rallyPoint = rallyPoint;
        return this;
    }

    public List<Integer> getContainedItems() {
        return containedItems;
    }

    public SyncBaseItemInfo setContainedItems(List<Integer> containedItems) {
        this.containedItems = containedItems;
        return this;
    }

    public Integer getTargetContainer() {
        return targetContainer;
    }

    public SyncBaseItemInfo setTargetContainer(Integer targetContainer) {
        this.targetContainer = targetContainer;
        return this;
    }

    public Integer getContainedIn() {
        return containedIn;
    }

    public SyncBaseItemInfo setContainedIn(Integer containedIn) {
        this.containedIn = containedIn;
        return this;
    }

    public DecimalPosition getUnloadPos() {
        return unloadPos;
    }

    public SyncBaseItemInfo setUnloadPos(DecimalPosition unloadPos) {
        this.unloadPos = unloadPos;
        return this;
    }

    public double getBuildup() {
        return buildup;
    }

    public SyncBaseItemInfo setBuildup(double buildup) {
        this.buildup = buildup;
        return this;
    }

    public Index getTargetPosition() {
        return targetPosition;
    }

    public SyncBaseItemInfo setTargetPosition(Index targetPosition) {
        this.targetPosition = Index.saveCopy(targetPosition);
        return this;
    }

    public Integer getSyncBoxItemId() {
        return syncBoxItemId;
    }

    public SyncBaseItemInfo setSyncBoxItemId(Integer syncBoxItemId) {
        this.syncBoxItemId = syncBoxItemId;
        return this;
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

    public double getSpawnProgress() {
        return spawnProgress;
    }

    public SyncBaseItemInfo setSpawnProgress(double spawnProgress) {
        this.spawnProgress = spawnProgress;
        return this;
    }

    @Override
    public String toString() {
        return "SyncItemInfo: " + id +
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
                " unloadPos:" + unloadPos +
                " targetPosition:" + targetPosition;
    }
}
