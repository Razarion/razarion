package com.btxtech.server.persistence.terrain;

import com.btxtech.server.IgnoreOldArquillianTest;
import com.btxtech.server.rest.TerrainEditorControllerImpl;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.TerrainEditorUpdate;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.comparator.impl.ObjectComparatorIgnore;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * on 28.11.2017.
 */
@Ignore
public class TerrainPersistenceTestRest extends IgnoreOldArquillianTest {
    @Inject
    private TerrainEditorControllerImpl terrainEditorController;

    @Before
    public void before() throws Exception {
        setupPlanetDb();
        setupSlopeConfig();
    }

    @After
    public void after() throws Exception {
        cleanPlanets();
        cleanSlopeEntities();
    }

    @Test
    public void testSlopeCrud() {
        // Verify empty
        List<TerrainSlopePosition> terrainSlopePositions = terrainEditorController.readTerrainSlopePositions(PLANET_1_ID);
        Assert.assertTrue(terrainSlopePositions.isEmpty());
        // Create
        TerrainEditorUpdate terrainEditorUpdate = new TerrainEditorUpdate();
        List<TerrainSlopePosition> expectedTerrainSlopePosition = new ArrayList<>();
        TerrainSlopePosition terrainSlopePosition = new TerrainSlopePosition();
        terrainSlopePosition.slopeConfigId(SLOPE_LAND_CONFIG_ENTITY_1);
        terrainSlopePosition.polygon(Arrays.asList(createTSC(100, 200, null), createTSC(300, 200, null), createTSC(300, 500, null), createTSC(100, 500, null)));
        expectedTerrainSlopePosition.add(terrainSlopePosition);
        // TODO terrainEditorUpdate.setCreatedSlopes(expectedTerrainSlopePosition);
        terrainEditorController.updateTerrain(PLANET_1_ID, terrainEditorUpdate);
        // Verify
        terrainSlopePositions = terrainEditorController.readTerrainSlopePositions(PLANET_1_ID);
        ObjectComparatorIgnore.add(TerrainSlopePosition.class, "id");
        int firstSlopeId = terrainSlopePositions.get(0).getId();
        ReflectionAssert.assertReflectionEquals(expectedTerrainSlopePosition, terrainSlopePositions);
        ObjectComparatorIgnore.clear();
        // Modify
        terrainEditorUpdate = new TerrainEditorUpdate();
        expectedTerrainSlopePosition = new ArrayList<>();
        terrainSlopePosition = new TerrainSlopePosition();
        terrainSlopePosition.id(firstSlopeId);
        terrainSlopePosition.slopeConfigId(SLOPE_WATER_CONFIG_ENTITY_2);
        terrainSlopePosition.polygon(Arrays.asList(createTSC(100, 200, null), createTSC(300, 200, null), createTSC(400, 500, null), createTSC(800, 500, null), createTSC(100, 500, null)));
        expectedTerrainSlopePosition.add(terrainSlopePosition);
        // TODO terrainEditorUpdate.setUpdatedSlopes(expectedTerrainSlopePosition);
        terrainEditorController.updateTerrain(PLANET_1_ID, terrainEditorUpdate);
        // Verify
        terrainSlopePositions = terrainEditorController.readTerrainSlopePositions(PLANET_1_ID);
        ReflectionAssert.assertReflectionEquals(expectedTerrainSlopePosition, terrainSlopePositions);
        // Add child
        terrainEditorUpdate = new TerrainEditorUpdate();
        List<TerrainSlopePosition> expectedChildTerrainSlopePosition = new ArrayList<>();
        terrainSlopePosition = new TerrainSlopePosition();
        terrainSlopePosition.slopeConfigId(SLOPE_LAND_CONFIG_ENTITY_1);
        terrainSlopePosition.inverted(true);
        terrainSlopePosition.polygon(Arrays.asList(createTSC(200, 300, null), createTSC(200, 400, null), createTSC(300, 500, null)));
        expectedChildTerrainSlopePosition.add(terrainSlopePosition);
        // TODO terrainEditorUpdate.setCreatedSlopes(expectedChildTerrainSlopePosition);
        terrainEditorController.updateTerrain(PLANET_1_ID, terrainEditorUpdate);
        // Verify
        terrainSlopePositions = terrainEditorController.readTerrainSlopePositions(PLANET_1_ID);
        expectedTerrainSlopePosition.get(0).children(expectedChildTerrainSlopePosition);
        ObjectComparatorIgnore.add(TerrainSlopePosition.class, "id");
        ReflectionAssert.assertReflectionEquals(expectedTerrainSlopePosition, terrainSlopePositions);
        ObjectComparatorIgnore.clear();
        int firstSlopeChildId = terrainSlopePositions.get(0).getChildren().get(0).getId();
        // Update child
        terrainEditorUpdate = new TerrainEditorUpdate();
        expectedChildTerrainSlopePosition = new ArrayList<>();
        terrainSlopePosition = new TerrainSlopePosition();
        terrainSlopePosition.slopeConfigId(SLOPE_WATER_CONFIG_ENTITY_2);
        terrainSlopePosition.id(firstSlopeChildId);
        terrainSlopePosition.inverted(false);
        terrainSlopePosition.polygon(Arrays.asList(createTSC(201, 301, null), createTSC(201, 401, null), createTSC(300, 500, null), createTSC(302, 503, null)));
        expectedChildTerrainSlopePosition.add(terrainSlopePosition);
        // TODO terrainEditorUpdate.setUpdatedSlopes(expectedChildTerrainSlopePosition);
        terrainEditorController.updateTerrain(PLANET_1_ID, terrainEditorUpdate);
        // Verify
        terrainSlopePositions = terrainEditorController.readTerrainSlopePositions(PLANET_1_ID);
        expectedTerrainSlopePosition.get(0).children(expectedChildTerrainSlopePosition);
        ReflectionAssert.assertReflectionEquals(expectedTerrainSlopePosition, terrainSlopePositions);
        // Remove child
        terrainEditorUpdate = new TerrainEditorUpdate();
        // TODO terrainEditorUpdate.setDeletedSlopeIds(Collections.singletonList(firstSlopeChildId));
        terrainEditorController.updateTerrain(PLANET_1_ID, terrainEditorUpdate);
        // Verify
        terrainSlopePositions = terrainEditorController.readTerrainSlopePositions(PLANET_1_ID);
        expectedTerrainSlopePosition.get(0).children(null);
        ReflectionAssert.assertReflectionEquals(expectedTerrainSlopePosition, terrainSlopePositions);
        // Remove
        terrainEditorUpdate = new TerrainEditorUpdate();
        // TODO terrainEditorUpdate.setDeletedSlopeIds(Collections.singletonList(firstSlopeId));
        terrainEditorController.updateTerrain(PLANET_1_ID, terrainEditorUpdate);
        // Verify empty
        terrainSlopePositions = terrainEditorController.readTerrainSlopePositions(PLANET_1_ID);
        Assert.assertTrue(terrainSlopePositions.isEmpty());
    }

    static TerrainSlopeCorner createTSC(double x, double y, Integer slopeDrivewayId) {
        return new TerrainSlopeCorner().position(new DecimalPosition(x, y)).slopeDrivewayId(slopeDrivewayId);
    }
}
