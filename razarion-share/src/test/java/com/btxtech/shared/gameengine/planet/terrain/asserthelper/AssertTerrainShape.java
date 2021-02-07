package com.btxtech.shared.gameengine.planet.terrain.asserthelper;

import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeManager;
import com.btxtech.test.JsonAssert;

/**
 * Created by Beat
 * on 02.10.2017.
 */
public interface AssertTerrainShape {
    static void assertTerrainShape(Class theClass, String resourceName, TerrainShapeManager actualTerrainShape) {
        assertTerrainShape(theClass, resourceName, actualTerrainShape, false);
    }

    static void assertTerrainShape(Class theClass, String resourceName, TerrainShapeManager actualTerrainShape, boolean save) {
        JsonAssert.TEST_RESOURCE_FOLDER = AssertTerrainTile.SAVE_DIRECTORY;
        JsonAssert.assertViaJson(resourceName, null, null, theClass, actualTerrainShape.toNativeTerrainShape(), save);
    }
}
