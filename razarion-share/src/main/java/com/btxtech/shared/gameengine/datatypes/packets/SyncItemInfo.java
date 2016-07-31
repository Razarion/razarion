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


import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;

import java.util.Iterator;
import java.util.List;

/**
 * User: beat
 * Date: 23.11.2009
 * Time: 21:54:50
 */
public class SyncItemInfo extends Packet {
    private int id;
    private Index position;
    private int itemTypeId;
    private boolean isAlive = true;
    private PlayerBase base;
    private PlayerBase killedBy;
    private List<Index> pathToDestination;
    private Double angel;
    private Vertex toBeBuildPosition;
    private Integer toBeBuiltTypeId;
    private Integer currentBuildup;
    private Double factoryBuildupProgress;
    private Double projectileBuildupProgress;
    private Integer target;
    private Double health;
    private double buildup;
    private Double amount;
    private Boolean followTarget;
    private Boolean operationState;
    private double reloadProgress;
    private Vertex rallyPoint;
    private List<Integer> containedItems;
    private Integer targetContainer;
    private Integer containedIn;
    private Index unloadPos;
    private boolean explode;
    private Index targetPosition;
    private Double destinationAngel;
    private Long clientTimeStamp;
    private String startUuid;
    private Integer syncBoxItemId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Index getPosition() {
        return position;
    }

    public void setPosition(Index position) {
        this.position = Index.saveCopy(position);
    }

    public int getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(int itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public PlayerBase getKilledBy() {
        return killedBy;
    }

    public void setKilledBy(PlayerBase killedBy) {
        this.killedBy = killedBy;
    }

    public PlayerBase getBase() {
        return base;
    }

    public void setBase(PlayerBase base) {
        this.base = base;
    }

    public List<Index> getPathToDestination() {
        return pathToDestination;
    }

    public double getAngel() {
        return angel;
    }

    public Vertex getToBeBuildPosition() {
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

    public Double getProjectileBuildupProgress() {
        return projectileBuildupProgress;
    }

    public void setProjectileBuildupProgress(Double projectileBuildupProgress) {
        this.projectileBuildupProgress = projectileBuildupProgress;
    }

    public Integer getTarget() {
        return target;
    }

    public Double getHealth() {
        return health;
    }

    public Double getAmount() {
        return amount;
    }

    public Boolean isFollowTarget() {
        return followTarget;
    }

    public void setPathToDestination(List<Index> pathToDestination) {
        this.pathToDestination = pathToDestination;
    }

    public void setAngel(Double angel) {
        this.angel = angel;
    }

    public void setToBeBuildPosition(Vertex toBeBuildPosition) {
        this.toBeBuildPosition = toBeBuildPosition;
    }

    public void setToBeBuiltTypeId(Integer toBeBuiltTypeId) {
        this.toBeBuiltTypeId = toBeBuiltTypeId;
    }

    public void setCurrentBuildup(int currentBuildup) {
        this.currentBuildup = currentBuildup;
    }

    public void setFactoryBuildupProgress(Double factoryBuildupProgress) {
        this.factoryBuildupProgress = factoryBuildupProgress;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public void setHealth(Double health) {
        this.health = health;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setFollowTarget(boolean followTarget) {
        this.followTarget = followTarget;
    }

    public Boolean isOperationState() {
        return operationState;
    }

    public void setOperationState(Boolean operationState) {
        this.operationState = operationState;
    }

    public double getReloadProgress() {
        return reloadProgress;
    }

    public void setReloadProgress(double reloadProgress) {
        this.reloadProgress = reloadProgress;
    }

    public Vertex getRallyPoint() {
        return rallyPoint;
    }

    public void setRallyPoint(Vertex rallyPoint) {
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

    public Index getUnloadPos() {
        return unloadPos;
    }

    public void setUnloadPos(Index unloadPos) {
        this.unloadPos = unloadPos;
    }

    public boolean isExplode() {
        return explode;
    }

    public void setExplode(boolean explode) {
        this.explode = explode;
    }

    public double getBuildup() {
        return buildup;
    }

    public void setBuildup(double buildup) {
        this.buildup = buildup;
    }

    public Index getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(Index targetPosition) {
        this.targetPosition = Index.saveCopy(targetPosition);
    }

    public void setDestinationAngel(Double destinationAngel) {
        this.destinationAngel = destinationAngel;
    }

    public Double getDestinationAngel() {
        return destinationAngel;
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

    public Long getClientTimeStamp() {
        return clientTimeStamp;
    }

    public void setClientTimeStamp() {
        clientTimeStamp = System.currentTimeMillis();
    }

    public String getStartUuid() {
        return startUuid;
    }

    public void setStartUuid(String startUuid) {
        this.startUuid = startUuid;
    }

    public Integer getSyncBoxItemId() {
        return syncBoxItemId;
    }

    public void setSyncBoxItemId(Integer syncBoxItemId) {
        this.syncBoxItemId = syncBoxItemId;
    }

    @Override
    public String toString() {
        return "SyncItemInfo: " + id +
                " pos:" + position +
                " itemTypeId:" + itemTypeId +
                " isAlive:" + isAlive +
                " explode:" + explode +
                base +
                " pathToDestination:" + Index.toString(pathToDestination) +
                " angel:" + angel +
                " toBeBuildPosition:" + toBeBuildPosition +
                " toBeBuiltTypeId:" + toBeBuiltTypeId +
                " currentBuildup:" + currentBuildup +
                " buildupProgress:" + factoryBuildupProgress +
                " target:" + target +
                " health:" + health +
                " buildup:" + buildup +
                " amount:" + amount +
                " followTarget:" + followTarget +
                " operationState:" + operationState +
                " reloadProgress:" + reloadProgress +
                " rallyPoint:" + rallyPoint +
                " containedItems:" + intCollectionAsString() +
                " targetContainer:" + targetContainer +
                " containedIn:" + containedIn +
                " unloadPos:" + unloadPos +
                " targetPosition:" + targetPosition +
                " destinationAngel:" + destinationAngel +
                " clientTimeStamp:" + clientTimeStamp +
                " syncBoxItemId:" + syncBoxItemId;
    }
}
