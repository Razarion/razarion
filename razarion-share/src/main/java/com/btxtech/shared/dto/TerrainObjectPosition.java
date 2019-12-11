package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 10.05.2016.
 */
public class TerrainObjectPosition {
    private Integer id;
    private int terrainObjectId;
    private DecimalPosition position;
    private Vertex scale;
    private Vertex rotation;
    private Vertex offset;

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

    public Vertex getRotation() {
        return rotation;
    }

    public TerrainObjectPosition setRotation(Vertex rotation) {
        this.rotation = rotation;
        return this;
    }

    public Vertex get_Scale() {
        return scale;
    }

    public TerrainObjectPosition setScale(Vertex scale) {
        this.scale = scale;
        return this;
    }

    public Vertex getOffset() {
        return offset;
    }

    public TerrainObjectPosition setOffset(Vertex offset) {
        this.offset = offset;
        return this;
    }

    @Deprecated
    public double getScale() {
        return 0;
    }

    @Deprecated
    public TerrainObjectPosition setScale(double scale) {
        this.scale = new Vertex(scale, scale, scale);
        return this;
    }

    @Deprecated
    public double getRotationZ() {
        return 0;
    }

    @Deprecated
    public TerrainObjectPosition setRotationZ(double rotationZ) {
        return this;
    }
}
