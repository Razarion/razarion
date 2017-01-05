package com.btxtech.shared.gameengine.datatypes.workerdto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 05.01.2017.
 *
 * This Object is only used to transport the data from the web worker to the client
 *
 * It is meant to use the 'structured clone algorithm'
 * https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API/Structured_clone_algorithm
 *
 * GWT strips of unused methods. Getter and setter are not available
 *
 */
public class SyncBaseItemSimpleDto {
    private int baseItemTypeId;
    private Matrix4 model;
    private Matrix4 weaponTurret;
    private DecimalPosition position;
    private double spawning;
    private double buildup;
    private double health;
    private Vertex harvestingResourcePosition;
    private Vertex buildingPosition;

    public int getBaseItemTypeId() {
        return baseItemTypeId;
    }

    public void setBaseItemTypeId(int baseItemTypeId) {
        this.baseItemTypeId = baseItemTypeId;
    }

    public Matrix4 getModel() {
        return model;
    }

    public void setModel(Matrix4 model) {
        this.model = model;
    }

    public Matrix4 getWeaponTurret() {
        return weaponTurret;
    }

    public void setWeaponTurret(Matrix4 weaponTurret) {
        this.weaponTurret = weaponTurret;
    }

    public DecimalPosition getPosition() {
        return position;
    }

    public void setPosition(DecimalPosition position) {
        this.position = position;
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
}
