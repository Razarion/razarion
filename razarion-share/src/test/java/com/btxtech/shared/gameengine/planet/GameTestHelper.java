package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleSlope;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.CollectionUtils;
import org.easymock.EasyMock;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 19.10.2017.
 */
public interface GameTestHelper {

    static SyncBaseItem createMockSyncBaseItem(int id, double radius, TerrainType terrainType, DecimalPosition position) {
        SyncBaseItem syncBaseItem = new SyncBaseItem();
        SyncPhysicalMovable syncPhysicalMovable = createSyncPhysicalMovable(radius, terrainType, position, null, null, 17);
        syncBaseItem.init(id, null, syncPhysicalMovable);
        SimpleTestEnvironment.injectService("syncItem", syncPhysicalMovable, SyncPhysicalArea.class, syncBaseItem);
        return syncBaseItem;
    }

    static SyncBaseItem createMockSyncBaseItem(double radius, TerrainType terrainType, DecimalPosition position) {
        return createMockSyncBaseItem(-99, radius, terrainType, position);
    }

    static SyncPhysicalMovable createSyncPhysicalMovable(double radius, TerrainType terrainType, DecimalPosition position, DecimalPosition velocity, DecimalPosition preferredVelocity, double maxSpeed) {
        SyncPhysicalMovable syncPhysicalMovable = new SyncPhysicalMovable();
        SimpleTestEnvironment.injectService("position2d", syncPhysicalMovable, SyncPhysicalArea.class, position);
        SimpleTestEnvironment.injectService("velocity", syncPhysicalMovable, SyncPhysicalMovable.class, velocity);
        SimpleTestEnvironment.injectService("preferredVelocity", syncPhysicalMovable, SyncPhysicalMovable.class, preferredVelocity);
        SimpleTestEnvironment.injectService("radius", syncPhysicalMovable, SyncPhysicalArea.class, radius);
        SimpleTestEnvironment.injectService("maxSpeed", syncPhysicalMovable, SyncPhysicalMovable.class, maxSpeed);
        SimpleTestEnvironment.injectService("terrainType", syncPhysicalMovable, SyncPhysicalArea.class, terrainType);
        SyncItemContainerService syncItemContainerService = EasyMock.createNiceMock(SyncItemContainerService.class);
        EasyMock.replay(syncItemContainerService);
        SimpleTestEnvironment.injectService("syncItemContainerService", syncPhysicalMovable, SyncPhysicalArea.class, syncItemContainerService);
        return syncPhysicalMovable;
    }

    static SyncPhysicalArea createSyncPhysicalArea(double radius, TerrainType terrainType, DecimalPosition position) {
        SyncPhysicalArea syncPhysicalArea = new SyncPhysicalArea();
        SimpleTestEnvironment.injectService("position2d", syncPhysicalArea, SyncPhysicalArea.class, position);
        SimpleTestEnvironment.injectService("radius", syncPhysicalArea, SyncPhysicalArea.class, radius);
        SimpleTestEnvironment.injectService("terrainType", syncPhysicalArea, SyncPhysicalArea.class, terrainType);
        SyncItemContainerService syncItemContainerService = EasyMock.createNiceMock(SyncItemContainerService.class);
        EasyMock.replay(syncItemContainerService);
        SimpleTestEnvironment.injectService("syncItemContainerService", syncPhysicalArea, SyncPhysicalArea.class, syncItemContainerService);
        return syncPhysicalArea;
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
        return new TerrainSlopeCorner().setPosition(new DecimalPosition(x, y)).setSlopeDrivewayId(slopeDrivewayId);
    }
}
