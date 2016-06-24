package com.btxtech.shared.dto;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.primitives.Matrix4;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by Beat
 * 10.05.2016.
 */
@Portable
public class TerrainObjectPosition {
    private Integer id;
    private int terrainObjectId;
    private Index position;
    private double scale;
    private double zRotation;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean hasId() {
        return id != null;
    }

    public int getTerrainObjectId() {
        return terrainObjectId;
    }

    public void setTerrainObjectId(int terrainObjectId) {
        this.terrainObjectId = terrainObjectId;
    }

    public Index getPosition() {
        return position;
    }

    public void setPosition(Index position) {
        this.position = position;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getZRotation() {
        return zRotation;
    }

    public void setZRotation(double zRotation) {
        this.zRotation = zRotation;
    }

    public Matrix4 createRotationModelMatrix() {
        return Matrix4.createZRotation(zRotation);
    }

    public Matrix4 createModelMatrix(double generalScale, int z) {
        Matrix4 matrix4 = Matrix4.createTranslation(position.getX(), position.getY(), z);
        matrix4 = matrix4.multiply(Matrix4.createScale(scale * generalScale, scale * generalScale, scale * generalScale));
        return matrix4.multiply(createRotationModelMatrix());
    }
}
