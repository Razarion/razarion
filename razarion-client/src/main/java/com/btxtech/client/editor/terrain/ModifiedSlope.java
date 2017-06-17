package com.btxtech.client.editor.terrain;

import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;

import java.util.stream.Collectors;

/**
 * Created by Beat
 * 09.05.2016.
 */
public class ModifiedSlope {
    private Integer originalId;
    private int slopeId;
    private Polygon2D polygon;
    private boolean hover;
    private boolean dirty;

    public ModifiedSlope(TerrainSlopePosition original) {
        originalId = original.getId();
        slopeId = original.getSlopeConfigId();
        polygon = new Polygon2D(original.getPolygon().stream().map(TerrainSlopeCorner::getPosition).collect(Collectors.toList()));
    }

    public ModifiedSlope(int slopeId, Polygon2D polygon) {
        this.slopeId = slopeId;
        this.polygon = polygon;
    }

    public Polygon2D getPolygon() {
        return polygon;
    }

    public TerrainSlopePosition createTerrainSlopePositionNoId() {
        return new TerrainSlopePosition().setSlopeConfigId(slopeId).setPolygon(polygon.getCorners().stream().map(position -> new TerrainSlopeCorner().setPosition(position)).collect(Collectors.toList()));
    }

    public TerrainSlopePosition createTerrainSlopePosition() {
        return new TerrainSlopePosition().setId(originalId).setSlopeConfigId(slopeId).setPolygon(polygon.getCorners().stream().map(position -> new TerrainSlopeCorner().setPosition(position)).collect(Collectors.toList()));
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

    public boolean isHover() {
        return hover;
    }

    public void setHover(boolean hover) {
        this.hover = hover;
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

    public boolean contains(Polygon2D cursor) {
        return polygon != null && polygon.adjoins(cursor);
    }
}
