package com.btxtech.shared.gameengine.datatypes.itemtype;


import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import jsinterop.annotations.JsType;
import org.teavm.flavour.json.JsonPersistable;

@JsType
@JsonPersistable
public class ResourceItemType extends ItemType {
    private double radius;
    private boolean fixVerticalNorm;
    private TerrainType terrainType;
    private int amount;

    public double getRadius() {
        return radius;
    }

    public ResourceItemType setRadius(double radius) {
        this.radius = radius;
        return this;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public ResourceItemType setTerrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
        return this;
    }

    public boolean isFixVerticalNorm() {
        return fixVerticalNorm;
    }

    public ResourceItemType setFixVerticalNorm(boolean fixVerticalNorm) {
        this.fixVerticalNorm = fixVerticalNorm;
        return this;
    }

    public int getAmount() {
        return amount;
    }

    public ResourceItemType setAmount(int amount) {
        this.amount = amount;
        return this;
    }
}
