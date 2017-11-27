package com.btxtech.client.editor.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.DrivewayConfig;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 09.05.2016.
 */
public class ModifiedSlope{
    private Integer originalId;
    private int slopeId;
    private Polygon2D polygon;
    private boolean hover;
    private boolean dirty;
    private Map<DecimalPosition, Integer> drivewayPositions = new HashMap<>();

    public ModifiedSlope(TerrainSlopePosition original) {
        originalId = original.getId();
        slopeId = original.getSlopeConfigId();
        List<DecimalPosition> positions = new ArrayList<>();
        for (TerrainSlopeCorner terrainSlopeCorner : original.getPolygon()) {
            positions.add(terrainSlopeCorner.getPosition());
            if (terrainSlopeCorner.getSlopeDrivewayId() != null) {
                drivewayPositions.put(terrainSlopeCorner.getPosition(), terrainSlopeCorner.getSlopeDrivewayId());
            }
        }
        polygon = new Polygon2D(positions);
    }

    public ModifiedSlope(int slopeId, Polygon2D polygon) {
        this.slopeId = slopeId;
        this.polygon = polygon;
    }

    public Polygon2D getPolygon() {
        return polygon;
    }

    public TerrainSlopePosition createTerrainSlopePositionNoId() {
        return new TerrainSlopePosition().setSlopeConfigId(slopeId).setPolygon(polygon.getCorners().stream().map(this::createTerrainSlopeCorner).collect(Collectors.toList()));
    }

    public TerrainSlopePosition createTerrainSlopePosition() {
        return new TerrainSlopePosition().setId(originalId).setSlopeConfigId(slopeId).setPolygon(polygon.getCorners().stream().map(this::createTerrainSlopeCorner).collect(Collectors.toList()));
    }

    private TerrainSlopeCorner createTerrainSlopeCorner(DecimalPosition position) {
        TerrainSlopeCorner terrainSlopeCorner = new TerrainSlopeCorner();
        terrainSlopeCorner.setPosition(position);
        Integer drivewayConfigId = drivewayPositions.get(position);
        if (drivewayConfigId != null) {
            terrainSlopeCorner.setSlopeDrivewayId(drivewayConfigId);
        }
        return terrainSlopeCorner;
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

    public void increaseDriveway(Polygon2D cursor, DrivewayConfig drivewayConfig) {
        Integer foundDrivewayConfigId = null;
        for (DecimalPosition drivewayPosition : cursor.getCorners()) {
            Integer drivewayConfigId = drivewayPositions.get(drivewayPosition);
            if (drivewayConfigId == null) {
                break;
            }
            if (foundDrivewayConfigId == null) {
                foundDrivewayConfigId = drivewayConfigId;
            } else if (foundDrivewayConfigId.equals(drivewayConfigId)) {
                return;
            }
        }
        if (foundDrivewayConfigId == null) {
            foundDrivewayConfigId = drivewayConfig.getId();
        }
        for (DecimalPosition slopePosition : polygon.getCorners()) {
            if (cursor.isInside(slopePosition)) {
                drivewayPositions.put(slopePosition, foundDrivewayConfigId);
                dirty = true;
            }
        }
    }

    public void decreaseDriveway(Polygon2D cursor) {
        for (DecimalPosition slopePosition : polygon.getCorners()) {
            if (cursor.isInside(slopePosition)) {
                drivewayPositions.remove(slopePosition);
                dirty = true;
            }
        }
    }

    public boolean isPositionInDriveway(DecimalPosition position) {
        return drivewayPositions.containsKey(position);
    }
}
