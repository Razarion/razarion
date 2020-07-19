package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 10.05.2016.
 */
public class TerrainObjectConfig implements Config {
    private int id;
    private String internalName;
    private Integer shape3DId;
    private double radius;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public Integer getShape3DId() {
        return shape3DId;
    }

    public void setShape3DId(Integer shape3DId) {
        this.shape3DId = shape3DId;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TerrainObjectConfig that = (TerrainObjectConfig) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public TerrainObjectConfig id(int id) {
        this.id = id;
        return this;
    }

    public TerrainObjectConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public TerrainObjectConfig shape3DId(Integer shape3DId) {
        setShape3DId(shape3DId);
        return this;
    }

    public TerrainObjectConfig radius(double radius) {
        setRadius(radius);
        return this;
    }
}
