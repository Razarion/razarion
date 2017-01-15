package com.btxtech.client.editor.terrain;

import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.TerrainSlopePosition;

/**
 * Created by Beat
 * 09.05.2016.
 */
public class ModifiedSlope {
    private Integer originalId;
    private int slopeId;
    private Polygon2D polygon;
    private boolean selected;
    private boolean dirty;

    public ModifiedSlope(TerrainSlopePosition original) {
        originalId = original.getId();
        slopeId = original.getSlopeId();
        polygon = new Polygon2D(original.getPolygon());
    }

    public ModifiedSlope(int slopeId, Polygon2D polygon) {
        this.slopeId = slopeId;
        this.polygon = polygon;
    }

    public Polygon2D getPolygon() {
        return polygon;
    }

    public TerrainSlopePosition createServerTerrainSlopePositionNoId() {
        return new TerrainSlopePosition().setSlopeId(slopeId).setPolygon(polygon.getCorners());
    }

    public TerrainSlopePosition createServerTerrainSlopePosition() {
        return new TerrainSlopePosition().setId(originalId).setSlopeId(slopeId).setPolygon(polygon.getCorners());
    }

    public Polygon2D combine(Polygon2D other) {
        polygon = polygon.combine(other);
        dirty = true;
        return polygon;
    }

    public Polygon2D remove(Polygon2D other) {
        polygon = polygon.remove(other);
        dirty = true;
        return polygon;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isCreated() {
        return originalId == null;
    }

    public boolean isEmpty() {
        return polygon == null;
    }

    public boolean isDirty() {
        return dirty;
    }

    public int getOriginalId() {
        return originalId;
    }
}
