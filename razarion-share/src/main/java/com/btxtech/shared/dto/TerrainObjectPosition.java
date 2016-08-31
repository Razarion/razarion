package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by Beat
 * 10.05.2016.
 */
@Portable
public class TerrainObjectPosition {
    private Integer id;
    private int terrainObjectId;
    private DecimalPosition position;
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

    public DecimalPosition getPosition() {
        return position;
    }

    public void setPosition(DecimalPosition position) {
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

    public Matrix4 createModelMatrix(int z) {
        Matrix4 matrix4 = Matrix4.createTranslation(position.getX(), position.getY(), z);
        matrix4 = matrix4.multiply(Matrix4.createScale(scale, scale, scale));
        return matrix4.multiply(Matrix4.createZRotation(zRotation));
    }
}
