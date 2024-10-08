package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.system.Nullable;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 10.05.2016.
 */
@JsType
public class TerrainObjectPosition {
    private int id;
    private int terrainObjectConfigId;
    private DecimalPosition position;
    private Vertex scale;
    private Vertex rotation;
    private Vertex offset;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTerrainObjectConfigId() {
        return terrainObjectConfigId;
    }

    public void setTerrainObjectConfigId(int terrainObjectConfigId) {
        this.terrainObjectConfigId = terrainObjectConfigId;
    }

    public DecimalPosition getPosition() {
        return position;
    }

    public void setPosition(DecimalPosition position) {
        this.position = position;
    }

    public @Nullable Vertex getRotation() {
        return rotation;
    }

    public void setRotation(@Nullable Vertex rotation) {
        this.rotation = rotation;
    }

    public @Nullable Vertex getScale() {
        return scale;
    }

    public void setScale(@Nullable Vertex scale) {
        this.scale = scale;
    }

    public @Nullable Vertex getOffset() {
        return offset;
    }

    public void setOffset(@Nullable Vertex offset) {
        this.offset = offset;
    }

    public TerrainObjectPosition id(int id) {
        setId(id);
        return this;
    }

    public TerrainObjectPosition terrainObjectConfigId(int terrainObjectId) {
        setTerrainObjectConfigId(terrainObjectId);
        return this;
    }

    public TerrainObjectPosition position(DecimalPosition position) {
        setPosition(position);
        return this;
    }

    public TerrainObjectPosition scale(Vertex scale) {
        setScale(scale);
        return this;
    }

    public TerrainObjectPosition rotation(Vertex rotation) {
        setRotation(rotation);
        return this;
    }

    public TerrainObjectPosition offset(Vertex offset) {
        setOffset(offset);
        return this;
    }
}
