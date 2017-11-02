package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.InsideCheckResult;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.planet.terrain.slope.DrivewayGameEngineHandler;

/**
 * Created by Beat
 * on 31.10.2017.
 */
public class DrivewayContext {
    public enum Type {
        FLAT_DRIVEWAY,
        SLOPE_DRIVEWAY
    }

    private DrivewayGameEngineHandler drivewayGameEngineHandler;
    private Type type;
    private TerrainType innerTerrainType;
    private double height;

    public DrivewayContext(DrivewayGameEngineHandler drivewayGameEngineHandler, Type type, TerrainType innerTerrainType, double height) {
        this.drivewayGameEngineHandler = drivewayGameEngineHandler;
        this.type = type;
        this.innerTerrainType = innerTerrainType;
        this.height = height;
    }

    public InsideCheckResult checkInside(Rectangle2D terrainRect) {
        if (type == Type.FLAT_DRIVEWAY) {
            return drivewayGameEngineHandler.checkInsideFlatPolygon(terrainRect);
        } else if (type == Type.SLOPE_DRIVEWAY) {
            return drivewayGameEngineHandler.checkInsideSlopePolygon(terrainRect);
        } else {
            throw new IllegalArgumentException("DrivewayContext.checkInside(Rectangle2D) unknown type: " + type);
        }
    }

    public boolean isInside(DecimalPosition position) {
        if (type == Type.FLAT_DRIVEWAY) {
            return drivewayGameEngineHandler.isInsideFlatPolygon(position);
        } else if (type == Type.SLOPE_DRIVEWAY) {
            return drivewayGameEngineHandler.isInsideSlopePolygon(position);
        } else {
            throw new IllegalArgumentException("DrivewayContext.checkInside(DecimalPosition) unknown type: " + type);
        }
    }

    public TerrainType getInnerTerrainType() {
        return innerTerrainType;
    }

    public double getHeight() {
        return height;
    }

    public Type getType() {
        return type;
    }

    public double[] getDrivewayHeights(Rectangle2D rectangle) {
        return drivewayGameEngineHandler.generateDrivewayHeights(rectangle);
    }
}
