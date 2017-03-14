package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;

/**
 * Created by Beat
 * 10.05.2016.
 */
public class TerrainObjectPosition {
    private Integer id;
    private int terrainObjectId;
    private DecimalPosition position;
    private double scale;
    private double rotationZ;

    public int getId() {
        return id;
    }

    public TerrainObjectPosition setId(int id) {
        this.id = id;
        return this;
    }

    public boolean hasId() {
        return id != null;
    }

    public int getTerrainObjectId() {
        return terrainObjectId;
    }

    public TerrainObjectPosition setTerrainObjectId(int terrainObjectId) {
        this.terrainObjectId = terrainObjectId;
        return this;
    }

    public DecimalPosition getPosition() {
        return position;
    }

    public TerrainObjectPosition setPosition(DecimalPosition position) {
        this.position = position;
        return this;
    }

    public double getScale() {
        return scale;
    }

    public TerrainObjectPosition setScale(double scale) {
        this.scale = scale;
        return this;
    }

    public double getRotationZ() {
        return rotationZ;
    }

    public TerrainObjectPosition setRotationZ(double rotationZ) {
        this.rotationZ = rotationZ;
        return this;
    }

    public Matrix4 createModelMatrix(double z) {
        Matrix4 matrix4 = Matrix4.createTranslation(position.getX(), position.getY(), z);
        matrix4 = matrix4.multiply(Matrix4.createScale(scale, scale, scale));
        return matrix4.multiply(Matrix4.createZRotation(rotationZ));
    }
}
