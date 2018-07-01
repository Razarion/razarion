package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleSlope;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import org.easymock.EasyMock;

/**
 * Created by Beat
 * on 19.10.2017.
 */
public interface GameTestHelper {

    static SyncBaseItem createMockSyncBaseItem(double radius, TerrainType terrainType, DecimalPosition position) {
        SyncBaseItem syncBaseItem = new SyncBaseItem();
        syncBaseItem.init(-99, null, createSyncPhysicalMovable(radius, terrainType, position, null, null, 17));
        return syncBaseItem;
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

    static ObstacleSlope createObstacleSlope(DecimalPosition point1, DecimalPosition point2, DecimalPosition previous, DecimalPosition next) {
        return new ObstacleSlope(point1, point2, previous, next);
    }

    static SlopeNode createSlopeNode(double x, double z, double slopeFactor) {
        return new SlopeNode().setPosition(new Vertex(x, 0, z)).setSlopeFactor(slopeFactor);
    }

    static TerrainSlopeCorner createTerrainSlopeCorner(double x, double y, Integer slopeDrivewayId) {
        return new TerrainSlopeCorner().setPosition(new DecimalPosition(x, y)).setSlopeDrivewayId(slopeDrivewayId);
    }
}
