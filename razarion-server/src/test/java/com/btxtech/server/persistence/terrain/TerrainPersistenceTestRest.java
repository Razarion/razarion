package com.btxtech.server.persistence.terrain;

import com.btxtech.server.IgnoreOldArquillianTest;
import com.btxtech.server.rest.PlanetEditorProviderImpl;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.TerrainEditorLoad;
import com.btxtech.shared.dto.TerrainEditorUpdate;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.comparator.impl.ObjectComparatorIgnore;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 28.11.2017.
 */
public class TerrainPersistenceTestRest extends IgnoreOldArquillianTest {
    @Inject
    private PlanetEditorProviderImpl planetEditorProvider;

    @Before
    public void before() throws Exception {
        setupPlanets();
        setupSlopeConfigEntities();
    }

    @After
    public void after() throws Exception {
        cleanPlanets();
        cleanSlopeEntities();
    }

    @Test
    public void testSlopeCrud() {
        // Verify empty
        TerrainEditorLoad terrainEditorLoad = planetEditorProvider.readTerrainEditorLoad(PLANET_1_ID);
        Assert.assertTrue(terrainEditorLoad.getSlopes().isEmpty());
        Assert.assertTrue(terrainEditorLoad.getTerrainObjects().isEmpty());
        // Create
        TerrainEditorUpdate terrainEditorUpdate = new TerrainEditorUpdate();
        List<TerrainSlopePosition> expectedTerrainSlopePosition = new ArrayList<>();
        TerrainSlopePosition terrainSlopePosition = new TerrainSlopePosition();
        terrainSlopePosition.setSlopeConfigId(SLOPE_LAND_CONFIG_ENTITY_1);
        terrainSlopePosition.setPolygon(Arrays.asList(createTSC(100, 200, null), createTSC(300, 200, null), createTSC(300, 500, null), createTSC(100, 500, null)));
        expectedTerrainSlopePosition.add(terrainSlopePosition);
        terrainEditorUpdate.setCreatedSlopes(expectedTerrainSlopePosition);
        planetEditorProvider.updateTerrain(PLANET_1_ID, terrainEditorUpdate);
        // Verify
        terrainEditorLoad = planetEditorProvider.readTerrainEditorLoad(PLANET_1_ID);
        Assert.assertTrue(terrainEditorLoad.getTerrainObjects().isEmpty());
        ObjectComparatorIgnore.add(TerrainSlopePosition.class, "id");
        int firstSlopeId = terrainEditorLoad.getSlopes().get(0).getId();
        ReflectionAssert.assertReflectionEquals(expectedTerrainSlopePosition, terrainEditorLoad.getSlopes());
        ObjectComparatorIgnore.clear();
        // Modify
        terrainEditorUpdate = new TerrainEditorUpdate();
        expectedTerrainSlopePosition = new ArrayList<>();
        terrainSlopePosition = new TerrainSlopePosition();
        terrainSlopePosition.setId(firstSlopeId);
        terrainSlopePosition.setSlopeConfigId(SLOPE_WATER_CONFIG_ENTITY_2);
        terrainSlopePosition.setPolygon(Arrays.asList(createTSC(100, 200, null), createTSC(300, 200, null), createTSC(400, 500, null), createTSC(800, 500, null), createTSC(100, 500, null)));
        expectedTerrainSlopePosition.add(terrainSlopePosition);
        terrainEditorUpdate.setUpdatedSlopes(expectedTerrainSlopePosition);
        planetEditorProvider.updateTerrain(PLANET_1_ID, terrainEditorUpdate);
        // Verify
        terrainEditorLoad = planetEditorProvider.readTerrainEditorLoad(PLANET_1_ID);
        Assert.assertTrue(terrainEditorLoad.getTerrainObjects().isEmpty());
        ReflectionAssert.assertReflectionEquals(expectedTerrainSlopePosition, terrainEditorLoad.getSlopes());
        // Add child
        terrainEditorUpdate = new TerrainEditorUpdate();
        List<TerrainSlopePosition> expectedChildTerrainSlopePosition = new ArrayList<>();
        terrainSlopePosition = new TerrainSlopePosition();
        terrainSlopePosition.setSlopeConfigId(SLOPE_LAND_CONFIG_ENTITY_1);
        terrainSlopePosition.setEditorParentId(firstSlopeId);
        terrainSlopePosition.setInverted(true);
        terrainSlopePosition.setPolygon(Arrays.asList(createTSC(200, 300, null), createTSC(200, 400, null), createTSC(300, 500, null)));
        expectedChildTerrainSlopePosition.add(terrainSlopePosition);
        terrainEditorUpdate.setCreatedSlopes(expectedChildTerrainSlopePosition);
        planetEditorProvider.updateTerrain(PLANET_1_ID, terrainEditorUpdate);
        // Verify
        terrainEditorLoad = planetEditorProvider.readTerrainEditorLoad(PLANET_1_ID);
        Assert.assertTrue(terrainEditorLoad.getTerrainObjects().isEmpty());
        expectedChildTerrainSlopePosition.get(0).setEditorParentId(null);
        expectedTerrainSlopePosition.get(0).setChildren(expectedChildTerrainSlopePosition);
        ObjectComparatorIgnore.add(TerrainSlopePosition.class, "id");
        ReflectionAssert.assertReflectionEquals(expectedTerrainSlopePosition, terrainEditorLoad.getSlopes());
        ObjectComparatorIgnore.clear();
        int firstSlopeChildId = terrainEditorLoad.getSlopes().get(0).getChildren().get(0).getId();
        // Update child
        terrainEditorUpdate = new TerrainEditorUpdate();
        expectedChildTerrainSlopePosition = new ArrayList<>();
        terrainSlopePosition = new TerrainSlopePosition();
        terrainSlopePosition.setSlopeConfigId(SLOPE_WATER_CONFIG_ENTITY_2);
        terrainSlopePosition.setId(firstSlopeChildId);
        terrainSlopePosition.setInverted(false);
        terrainSlopePosition.setPolygon(Arrays.asList(createTSC(201, 301, null), createTSC(201, 401, null), createTSC(300, 500, null), createTSC(302, 503, null)));
        expectedChildTerrainSlopePosition.add(terrainSlopePosition);
        terrainEditorUpdate.setUpdatedSlopes(expectedChildTerrainSlopePosition);
        planetEditorProvider.updateTerrain(PLANET_1_ID, terrainEditorUpdate);
        // Verify
        terrainEditorLoad = planetEditorProvider.readTerrainEditorLoad(PLANET_1_ID);
        Assert.assertTrue(terrainEditorLoad.getTerrainObjects().isEmpty());
        expectedTerrainSlopePosition.get(0).setChildren(expectedChildTerrainSlopePosition);
        ReflectionAssert.assertReflectionEquals(expectedTerrainSlopePosition, terrainEditorLoad.getSlopes());
        // Remove child
        terrainEditorUpdate = new TerrainEditorUpdate();
        terrainEditorUpdate.setDeletedSlopeIds(Collections.singletonList(firstSlopeChildId));
        planetEditorProvider.updateTerrain(PLANET_1_ID, terrainEditorUpdate);
        // Verify
        terrainEditorLoad = planetEditorProvider.readTerrainEditorLoad(PLANET_1_ID);
        Assert.assertTrue(terrainEditorLoad.getTerrainObjects().isEmpty());
        expectedTerrainSlopePosition.get(0).setChildren(null);
        ReflectionAssert.assertReflectionEquals(expectedTerrainSlopePosition, terrainEditorLoad.getSlopes());
        // Remove
        terrainEditorUpdate = new TerrainEditorUpdate();
        terrainEditorUpdate.setDeletedSlopeIds(Collections.singletonList(firstSlopeId));
        planetEditorProvider.updateTerrain(PLANET_1_ID, terrainEditorUpdate);
        // Verify empty
        terrainEditorLoad = planetEditorProvider.readTerrainEditorLoad(PLANET_1_ID);
        Assert.assertTrue(terrainEditorLoad.getSlopes().isEmpty());
        Assert.assertTrue(terrainEditorLoad.getTerrainObjects().isEmpty());
    }

    static TerrainSlopeCorner createTSC(double x, double y, Integer slopeDrivewayId) {
        return new TerrainSlopeCorner().setPosition(new DecimalPosition(x, y)).setSlopeDrivewayId(slopeDrivewayId);
    }
}
