package com.btxtech.client.editor.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.shared.nativejs.NativeMatrixFactory;

/**
 * Created by Beat
 * 15.01.2017.
 */
public class EditorTerrainObjectWrapper {
    private static final double OVERLAPS_SAFETY_DISTANCE = 1;
    private int terrainObjectId;
    private Integer originalId;
    private DecimalPosition position;
    private double radius;
    private Vertex scale;
    private Vertex rotation;
    private ModelMatrices modelMatrices;
    private boolean deleted;
    private boolean dirty;

    public EditorTerrainObjectWrapper(TerrainObjectPosition terrainObjectPosition, double radius) {
        this.position = terrainObjectPosition.getPosition();
        terrainObjectId = terrainObjectPosition.getTerrainObjectConfigId();
        originalId = terrainObjectPosition.getId();
        scale = terrainObjectPosition.getScale();
        rotation = terrainObjectPosition.getRotation();
        this.radius = radius;
    }

    public EditorTerrainObjectWrapper(int terrainObjectId, DecimalPosition position, double scale, double rotationZ, double radius) {
        this.terrainObjectId = terrainObjectId;
        this.position = position;
        this.rotation = new Vertex(0, 0, rotationZ);
        this.radius = radius;
        this.scale = new Vertex(scale, scale, scale);
    }

    public boolean overlaps(Vertex terrainPosition) {
        return !deleted && position.getDistance(terrainPosition.toXY().toIndex()) < MathHelper.getPythagorasC(scale.getX(), scale.getY()) * radius + OVERLAPS_SAFETY_DISTANCE;
    }

    public void setHover(boolean hover) {
        if (hover) {
            modelMatrices.updateProgress(1);
        } else {
            modelMatrices.updateProgress(0);
        }
    }

    public void setNewPosition(Vertex newPosition, NativeMatrixFactory nativeMatrixFactory) {
        position = newPosition.toXY();
        modelMatrices = ModelMatrices.create4Editor(position.getX(), position.getY(), 0, scale.multiply(radius), nativeMatrixFactory);
        dirty = true;
    }

    public ModelMatrices createModelMatrices(NativeMatrixFactory nativeMatrixFactory) {
        modelMatrices = ModelMatrices.create4Editor(position.getX(), position.getY(), 0, scale.multiply(radius), nativeMatrixFactory);
        return modelMatrices;
    }

    public void setDeleted() {
        deleted = true;
    }

    public boolean isNotDeleted() {
        return !deleted;
    }

    public boolean isCreated() {
        return originalId == null;
    }

    public int getOriginalId() {
        return originalId;
    }

    public boolean isDirty() {
        return dirty;
    }

    public TerrainObjectPosition createTerrainObjectPositionNoId() {
        return new TerrainObjectPosition().position(position).terrainObjectConfigId(terrainObjectId).scale(scale).rotation(rotation);
    }

    public TerrainObjectPosition createTerrainObjectPosition() {
        return new TerrainObjectPosition().id(originalId).position(position).terrainObjectConfigId(terrainObjectId).scale(scale).rotation(rotation);
    }
}
