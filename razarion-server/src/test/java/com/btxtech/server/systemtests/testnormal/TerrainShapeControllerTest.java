package com.btxtech.server.systemtests.testnormal;

import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShape;
import com.btxtech.shared.rest.TerrainShapeController;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.NotAuthorizedException;

public class TerrainShapeControllerTest extends AbstractSystemTest {
    private TerrainShapeController terrainShapeController;

    @Before
    public void setup() {
        setupDb();
        terrainShapeController = setupRestAccess(TerrainShapeController.class);
    }

    @Test(expected = NotAuthorizedException.class)
    public void createTerrainShape() {
        getDefaultRestConnection().logout();
        terrainShapeController.createTerrainShape(PLANET_1_ID);
    }

    @Test(expected = NotAuthorizedException.class)
    public void createTerrainShapeUser() {
        getDefaultRestConnection().loginUser();
        terrainShapeController.createTerrainShape(PLANET_1_ID);
    }

    @Test
    public void createTerrainShapeAdmin() {
        getDefaultRestConnection().loginAdmin();
        terrainShapeController.createTerrainShape(PLANET_1_ID);
    }

    @Test
    public void getTerrainShape() {
        getDefaultRestConnection().loginAdmin();
        terrainShapeController.createTerrainShape(PLANET_1_ID);
        NativeTerrainShape nativeTerrainShape = terrainShapeController.getTerrainShape(PLANET_1_ID);
        nativeTerrainShape = nativeTerrainShape;
    }
}
