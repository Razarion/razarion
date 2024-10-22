package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleSlope;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.anyObject;

/**
 * Created by Beat
 * on 19.10.2017.
 */
public interface GameTestHelper {

    static SyncBaseItem createMockSyncBaseItem(int id, double radius, TerrainType terrainType, DecimalPosition position) {
//        SyncBaseItem syncBaseItem = new SyncBaseItem();
//        SyncPhysicalMovable syncPhysicalMovable = createSyncPhysicalMovable(radius, terrainType, position, null);
//        syncBaseItem.init(id, null, syncPhysicalMovable);
//        SimpleTestEnvironment.injectService("syncItem", syncPhysicalMovable, SyncPhysicalArea.class, syncBaseItem);
//        return syncBaseItem;
        throw new UnsupportedOperationException();
    }

    static SyncBaseItem createMockSyncBaseItem(double radius, TerrainType terrainType, DecimalPosition position) {
        return createMockSyncBaseItem(-99, radius, terrainType, position);
    }

    static SyncPhysicalMovable createSyncPhysicalMovable(double radius, TerrainType terrainType, DecimalPosition position, DecimalPosition preferredVelocity) {
//        SyncPhysicalMovable syncPhysicalMovable = new SyncPhysicalMovable();
//        SimpleTestEnvironment.injectService("position", syncPhysicalMovable, SyncPhysicalArea.class, position);
//        SimpleTestEnvironment.injectService("preferredVelocity", syncPhysicalMovable, SyncPhysicalMovable.class, preferredVelocity);
//        SimpleTestEnvironment.injectService("radius", syncPhysicalMovable, SyncPhysicalArea.class, radius);
//        SimpleTestEnvironment.injectService("terrainType", syncPhysicalMovable, SyncPhysicalArea.class, terrainType);
//        SimpleTestEnvironment.injectService("maxSpeed", syncPhysicalMovable, SyncPhysicalMovable.class, 17);
//        SimpleTestEnvironment.injectService("acceleration", syncPhysicalMovable, SyncPhysicalMovable.class, 5.0);
//        SimpleTestEnvironment.injectService("angularVelocity", syncPhysicalMovable, SyncPhysicalMovable.class, Math.toRadians(180));
//        SyncItemContainerService syncItemContainerService = EasyMock.createNiceMock(SyncItemContainerServiceImpl.class);
//        EasyMock.replay(syncItemContainerService);
//        SimpleTestEnvironment.injectService("syncItemContainerService", syncPhysicalMovable, SyncPhysicalArea.class, syncItemContainerService);
//        return syncPhysicalMovable;
        throw new UnsupportedOperationException();
    }

    static SyncPhysicalMovable createSyncPhysicalMovable(double radius, int syncItemId, TerrainType terrainType, DecimalPosition position, DecimalPosition velocity, List<DecimalPosition> wayPositions) {
        throw new UnsupportedOperationException();
//        PathingAccess pathingAccess = EasyMock.createNiceMock(PathingAccess.class);
//        expect(pathingAccess.isInSight(anyObject(), anyDouble(), anyObject())).andReturn(true);
//        TerrainService terrainService = EasyMock.createNiceMock(TerrainService.class);
//        expect(terrainService.getPathingAccess()).andReturn(pathingAccess);
//        EasyMock.replay(pathingAccess, terrainService);
//        SyncPhysicalMovable syncPhysicalMovable = new SyncPhysicalMovable();
//        if (wayPositions != null) {
//            Path path = new Path();
//            path.init(new SimplePath().wayPositions(wayPositions));
//            SimpleTestEnvironment.injectService("path", syncPhysicalMovable, SyncPhysicalMovable.class, path);
//            SimpleTestEnvironment.injectService("terrainService", path, Path.class, terrainService);
//        }
//        SimpleTestEnvironment.injectService("position2d", syncPhysicalMovable, SyncPhysicalArea.class, position);
//        SimpleTestEnvironment.injectService("maxSpeed", syncPhysicalMovable, SyncPhysicalMovable.class, 17);
//        SimpleTestEnvironment.injectService("acceleration", syncPhysicalMovable, SyncPhysicalMovable.class, 5.0);
//        SimpleTestEnvironment.injectService("angularVelocity", syncPhysicalMovable, SyncPhysicalMovable.class, Math.toRadians(180));
//        SimpleTestEnvironment.injectService("velocity", syncPhysicalMovable, SyncPhysicalMovable.class, velocity);
//        SimpleTestEnvironment.injectService("radius", syncPhysicalMovable, SyncPhysicalArea.class, radius);
//        SimpleTestEnvironment.injectService("terrainType", syncPhysicalMovable, SyncPhysicalArea.class, terrainType);
//        SyncBaseItem syncItem = new SyncBaseItem();
//        syncItem.init(syncItemId, null, syncPhysicalMovable);
//        SimpleTestEnvironment.injectService("syncItem", syncPhysicalMovable, SyncPhysicalArea.class, syncItem);
//        SyncItemContainerService syncItemContainerService = EasyMock.createNiceMock(SyncItemContainerServiceImpl.class);
//        EasyMock.replay(syncItemContainerService);
//        SimpleTestEnvironment.injectService("syncItemContainerService", syncPhysicalMovable, SyncPhysicalArea.class, syncItemContainerService);
//        syncPhysicalMovable.setupPreferredVelocity();
//        return syncPhysicalMovable;
    }

    static SyncPhysicalArea createSyncPhysicalArea(double radius, TerrainType terrainType, DecimalPosition position) {
//        SyncPhysicalArea syncPhysicalArea = new SyncPhysicalArea();
//        SimpleTestEnvironment.injectService("position2d", syncPhysicalArea, SyncPhysicalArea.class, position);
//        SimpleTestEnvironment.injectService("radius", syncPhysicalArea, SyncPhysicalArea.class, radius);
//        SimpleTestEnvironment.injectService("terrainType", syncPhysicalArea, SyncPhysicalArea.class, terrainType);
//        SyncItemContainerService syncItemContainerService = EasyMock.createNiceMock(SyncItemContainerServiceImpl.class);
//        EasyMock.replay(syncItemContainerService);
//        SimpleTestEnvironment.injectService("syncItemContainerService", syncPhysicalArea, SyncPhysicalArea.class, syncItemContainerService);
//        return syncPhysicalArea;
        throw new UnsupportedOperationException();
    }

    static List<ObstacleSlope> createObstacleSlopes(DecimalPosition... polygon) {
        List<ObstacleSlope> obstacleSlopes = new ArrayList<>();
        for (int i = 0; i < polygon.length; i++) {
            DecimalPosition previous = CollectionUtils.getCorrectedElement(i - 1, polygon);
            DecimalPosition point1 = polygon[i];
            DecimalPosition point2 = CollectionUtils.getCorrectedElement(i + 1, polygon);
            DecimalPosition next = CollectionUtils.getCorrectedElement(i + 2, polygon);
            obstacleSlopes.add(new ObstacleSlope(point1, point2, previous, next));
        }
        return obstacleSlopes;
    }

    static TerrainSlopeCorner createTerrainSlopeCorner(double x, double y, Integer slopeDrivewayId) {
        return new TerrainSlopeCorner().position(new DecimalPosition(x, y)).slopeDrivewayId(slopeDrivewayId);
    }
}
