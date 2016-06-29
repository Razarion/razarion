package com.btxtech.client.editor.terrain;

import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.primitives.Polygon2I;

/**
 * Created by Beat
 * 09.05.2016.
 */
public class ModifiedTerrainSlopePosition {
    private Integer originalId;
    private int slopeId;
    private Polygon2I polygon;

    public ModifiedTerrainSlopePosition(TerrainSlopePosition original) {
        originalId = original.getId();
        slopeId = original.getSlopeId();
        polygon = new Polygon2I(original.getPolygon());
    }

    public ModifiedTerrainSlopePosition(int slopeId, Polygon2I polygon) {
        this.slopeId = slopeId;
        this.polygon = polygon;
    }

    public Polygon2I getPolygon2I() {
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

    public Polygon2I combine(Polygon2I other) {
        polygon = polygon.combine(other);
        return polygon;
    }

    public Polygon2I remove(Polygon2I other) {
        polygon = polygon.remove(other);
        return polygon;
    }

    public boolean isValidForServer() {
        return polygon != null || originalId != null;
    }
}
