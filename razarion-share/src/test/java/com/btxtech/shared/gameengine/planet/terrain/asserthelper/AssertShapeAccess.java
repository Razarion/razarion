package com.btxtech.shared.gameengine.planet.terrain.asserthelper;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.test.JsonAssert;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * on 10.11.2017.
 */
public interface AssertShapeAccess {
    static void assertShape(TerrainService terrainService, DecimalPosition from, DecimalPosition to, Class theClass, String resourceName) {
        assertShape(terrainService, from, to, theClass, resourceName, false);
    }

    static void assertShape(TerrainService terrainService, DecimalPosition from, DecimalPosition to, Class theClass, String resourceName, boolean save) {
        Map<DecimalPosition, ShapeAccessTypeContainer> actualMap = new HashMap<>();

//        SurfaceAccess surfaceAccess = terrainService.getSurfaceAccess();
//        PathingAccess pathingAccess = terrainService.getPathingAccess();
//        for (double x = from.getX(); x < to.getX(); x++) {
//            for (double y = from.getY(); y < to.getY(); y++) {
//                DecimalPosition samplePosition = new DecimalPosition(x + 0.5, y + 0.5);
//                actualMap.put(samplePosition, new ShapeAccessTypeContainer()
//                        .height(surfaceAccess.getInterpolatedZ(samplePosition))
//                        .norm(surfaceAccess.getInterpolatedNorm(samplePosition))
//                        .terrainType(pathingAccess.getTerrainType(samplePosition)));
//            }
//        }

        JsonAssert.TEST_RESOURCE_FOLDER = AssertTerrainTile.SAVE_DIRECTORY;
        JsonAssert.assertViaJson(resourceName, null, null, theClass, actualMap, save);
    }
}
