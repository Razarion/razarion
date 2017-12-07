package com.btxtech.shared.gameengine.datatypes.workerdto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 05.01.2017.
 * <p>
 * This Object is only used to transport the data from the web worker to the client
 * <p>
 * It is meant to use the 'structured clone algorithm'
 * https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API/Structured_clone_algorithm
 * <p>
 * GWT strips of unused methods. Getter and setter are not available
 */
public class SyncBaseItemSimpleDto extends SyncItemSimpleDto { // Rename to Snapshot or volatile
    private int baseId;
    private Matrix4 weaponTurret;
    private double spawning;
    private double buildup;
    private double health;
    private double constructing;
    private Vertex harvestingResourcePosition;
    private Vertex buildingPosition;
    private DecimalPosition interpolatableVelocity;
    private int containingItemCount;
    private double maxContainingRadius;
    private boolean contained;

    public int getBaseId() {
        return baseId;
    }

    public void setBaseId(int baseId) {
        this.baseId = baseId;
    }

    public Matrix4 getWeaponTurret() {
        return weaponTurret;
    }

    public void setWeaponTurret(Matrix4 weaponTurret) {
        this.weaponTurret = weaponTurret;
    }

    public double getSpawning() {
        return spawning;
    }

    public void setSpawning(double spawning) {
        this.spawning = spawning;
    }

    public boolean checkSpawning() {
        return spawning < 1.0;
    }

    public double getBuildup() {
        return buildup;
    }

    public void setBuildup(double buildup) {
        this.buildup = buildup;
    }

    public boolean checkBuildup() {
        return buildup >= 1.0;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public boolean checkHealth() {
        return health >= 1.0;
    }

    public double getConstructing() {
        return constructing;
    }

    public void setConstructing(double constructing) {
        this.constructing = constructing;
    }

    public boolean checkConstructing() {
        return constructing > 0.0;
    }

    public Vertex getHarvestingResourcePosition() {
        return harvestingResourcePosition;
    }

    public void setHarvestingResourcePosition(Vertex harvestingResourcePosition) {
        this.harvestingResourcePosition = harvestingResourcePosition;
    }

    public Vertex getBuildingPosition() {
        return buildingPosition;
    }

    public void setBuildingPosition(Vertex buildingPosition) {
        this.buildingPosition = buildingPosition;
    }

    public DecimalPosition getInterpolatableVelocity() {
        return interpolatableVelocity;
    }

    public void setInterpolatableVelocity(DecimalPosition interpolatableVelocity) {
        this.interpolatableVelocity = interpolatableVelocity;
    }

    public int getContainingItemCount() {
        return containingItemCount;
    }

    public void setContainingItemCount(int containingItemCount) {
        this.containingItemCount = containingItemCount;
    }

    public double getMaxContainingRadius() {
        return maxContainingRadius;
    }

    public void setMaxContainingRadius(double maxContainingRadius) {
        this.maxContainingRadius = maxContainingRadius;
    }

    public boolean isContained() {
        return contained;
    }

    public void setContained(boolean contained) {
        this.contained = contained;
    }
}
