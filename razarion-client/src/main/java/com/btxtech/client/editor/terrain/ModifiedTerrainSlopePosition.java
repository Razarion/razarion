package com.btxtech.client.editor.terrain;

import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.TerrainSlopePosition;

/**
 * Created by Beat
 * 09.05.2016.
 */
public class ModifiedTerrainSlopePosition {
    private Integer originalId;
    private int slopeId;
    private Polygon2D polygon;

    public ModifiedTerrainSlopePosition(TerrainSlopePosition original) {
        originalId = original.getId();
        slopeId = original.getSlopeId();
        polygon = new Polygon2D(original.getPolygon());
    }

    public ModifiedTerrainSlopePosition(int slopeId, Polygon2D polygon) {
        this.slopeId = slopeId;
        this.polygon = polygon;
    }

    public Polygon2D getPolygon() {
        return polygon;
    }

    public TerrainSlopePosition createRendererTerrainSlopePosition(int tmpId) {
        return new TerrainSlopePosition(tmpId, slopeId, polygon.getCorners());
    }

    public TerrainSlopePosition createServerTerrainSlopePosition() {
        if (polygon != null) {
            return new TerrainSlopePosition(originalId, slopeId, polygon.getCorners());
        } else {
            return new TerrainSlopePosition(originalId, slopeId, null);
        }
    }

    public Polygon2D combine(Polygon2D other) {
        polygon = polygon.combine(other);
        return polygon;
    }

    public Polygon2D remove(Polygon2D other) {
        polygon = polygon.remove(other);
        return polygon;
    }

    public boolean isValidForServer() {
        return polygon != null || originalId != null;
    }
}
