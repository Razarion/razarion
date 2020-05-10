package com.btxtech.server.systemtests.testnormal;

import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;
import com.btxtech.shared.rest.TerrainShapeController;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.NotAuthorizedException;

import static org.junit.Assert.fail;

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
    @Ignore
    public void displayDifferences() throws Exception {
        String s = "{ \n" +
                "    \"employee\":\n" +
                "    {\n" +
                "        \"id\": \"1212\",\n" +
                "        \"fullName\":\"John Miles\",\n" +
                "        \"age\": 34,\n" +
                "        \"contact\":\n" +
                "        {\n" +
                "            \"email\": \"john@xyz.com\",\n" +
                "            \"phone\": \"9999999999\"\n" +
                "        }\n" +
                "    }\n" +
                "}";
        ObjectMapper mapper = new ObjectMapper();
        displayDifferences(mapper.readTree(s), mapper.readTree(s), JsonPointer.compile(null));
        fail();
    }

    @Test
    public void getTerrainShape() throws Exception {
        getDefaultRestConnection().loginAdmin();
        terrainShapeController.createTerrainShape(PLANET_1_ID);
        NativeTerrainShape nativeTerrainShape = terrainShapeController.getTerrainShape(PLANET_1_ID);
        assertViaJson("/systemtests/testnormal/TerrainShapeControllerTest_getTerrainShape.json",
                s -> s.replace("\"$SLOPE_ID$\"", Integer.toString(SLOPE_LAND_CONFIG_ENTITY_1)).replace("\"$RENDER_INNER_SLOPE_ID$\"", Integer.toString(SLOPE_LAND_CONFIG_ENTITY_1)),
                null,
                getClass(),
                nativeTerrainShape);
    }
}
