package com.btxtech.server.persistence.server;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.persistence.PlaceConfigEntity;
import com.btxtech.server.persistence.PlanetEntity;
import com.btxtech.server.persistence.PlanetPersistence;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.comparator.impl.ObjectComparatorIgnore;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 09.05.2017.
 */
public class ServerGameEnginePersistenceTest extends ArquillianBaseTest {
    @Inject
    private ServerGameEnginePersistence serverGameEnginePersistence;
    @Inject
    private PlanetPersistence planetPersistence;
    @PersistenceContext
    private EntityManager entityManager;

    @Before
    public void before() throws Exception {
        setupPlanets();
    }

    @After
    public void after() throws Exception {
        cleanPlanets();
    }

    @Test
    public void crudBot() throws Exception {
        Assert.assertTrue(serverGameEnginePersistence.readBotConfigs().isEmpty());

        List<BotConfig> expectedBots = setupServerBots1();
        serverGameEnginePersistence.updateBotConfigs(expectedBots);
        List<BotConfig> actualBots = new ArrayList<>(serverGameEnginePersistence.readBotConfigs());
        ObjectComparatorIgnore.add(BotConfig.class, "id");
        ReflectionAssert.assertReflectionEquals(expectedBots, actualBots);
        ObjectComparatorIgnore.clear();

        serverGameEnginePersistence.updateBotConfigs(new ArrayList<>());
        Assert.assertTrue(serverGameEnginePersistence.readBotConfigs().isEmpty());

        Assert.assertEquals(0L, entityManager.createQuery("SELECT COUNT(b) FROM BotConfigEntity b").getSingleResult());
        Assert.assertEquals(0L, entityManager.createQuery("SELECT COUNT(b) FROM BotEnragementStateConfigEntity b").getSingleResult());
        Assert.assertEquals(0L, entityManager.createQuery("SELECT COUNT(b) FROM BotItemConfigEntity b").getSingleResult());
        Assert.assertEquals(0L, entityManager.createQuery("SELECT COUNT(p) FROM PlaceConfigEntity p").getSingleResult());
        assertEmptyCountNative("SERVER_GAME_ENGINE_BOT_CONFIG");
    }

    @Test
    public void crudPlanetConfig() throws Exception {
        Assert.assertEquals(PLANET_2_ID, serverGameEnginePersistence.readPlanetConfig().getPlanetId());

        int planetId = planetPersistence.createPlanetConfig();
        serverGameEnginePersistence.updatePlanetConfig(planetId);
        Assert.assertEquals(planetId, serverGameEnginePersistence.readPlanetConfig().getPlanetId());
        try {
            planetPersistence.deletePlanetConfig(planetId);
            Assert.fail("Exception expected");
        } catch (Throwable t) {
            // Expected
        }
        assertCount(3, PlanetEntity.class);

        serverGameEnginePersistence.updatePlanetConfig(null);
        assertCount(3, PlanetEntity.class);

        planetPersistence.deletePlanetConfig(planetId);
        assertCount(2, PlanetEntity.class);
    }

    @Test
    public void crudSlavePlanetConfig() throws Exception {
        try {
            Polygon2D notExpected = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_4_ID).getStartRegion();
            Assert.fail("IllegalArgumentException expected: " + notExpected);
        } catch (IllegalArgumentException e) {
            // Expected
        }

        // Add first
        Polygon2D expectedStartRegion1 = new Polygon2D(Arrays.asList(new DecimalPosition(100, 100), new DecimalPosition(300, 100), new DecimalPosition(300, 300), new DecimalPosition(100, 300)));
        serverGameEnginePersistence.updateStartRegion(LEVEL_4_ID, expectedStartRegion1.getCorners());

        // Verify
        Polygon2D actualStartRegion = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_4_ID).getStartRegion();
        ReflectionAssert.assertReflectionEquals(expectedStartRegion1, actualStartRegion);

        actualStartRegion = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_5_ID).getStartRegion();
        ReflectionAssert.assertReflectionEquals(expectedStartRegion1, actualStartRegion);

        // Add second
        Polygon2D expectedStartRegion2 = new Polygon2D(Arrays.asList(new DecimalPosition(200, 200), new DecimalPosition(400, 200), new DecimalPosition(400, 400), new DecimalPosition(200, 400)));
        serverGameEnginePersistence.updateStartRegion(LEVEL_5_ID, expectedStartRegion2.getCorners());

        // Verify
        actualStartRegion = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_4_ID).getStartRegion();
        ReflectionAssert.assertReflectionEquals(expectedStartRegion1, actualStartRegion);

        actualStartRegion = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_5_ID).getStartRegion();
        ReflectionAssert.assertReflectionEquals(expectedStartRegion2, actualStartRegion);

        // Update second
        Polygon2D expectedStartRegion3 = new Polygon2D(Arrays.asList(new DecimalPosition(500, 600), new DecimalPosition(700, 600), new DecimalPosition(400, 1000)));
        serverGameEnginePersistence.updateStartRegion(LEVEL_5_ID, expectedStartRegion3.getCorners());

        // Verify
        actualStartRegion = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_4_ID).getStartRegion();
        ReflectionAssert.assertReflectionEquals(expectedStartRegion1, actualStartRegion);

        actualStartRegion = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_5_ID).getStartRegion();
        ReflectionAssert.assertReflectionEquals(expectedStartRegion3, actualStartRegion);

        // Remove second
        serverGameEnginePersistence.clearStartRegion(LEVEL_5_ID);

        // Verify
        actualStartRegion = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_4_ID).getStartRegion();
        ReflectionAssert.assertReflectionEquals(expectedStartRegion1, actualStartRegion);

        actualStartRegion = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_5_ID).getStartRegion();
        ReflectionAssert.assertReflectionEquals(expectedStartRegion1, actualStartRegion);

        // Remove first
        serverGameEnginePersistence.clearStartRegion(LEVEL_4_ID);

        // Verify
        try {
            Polygon2D notExpected = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_4_ID).getStartRegion();
            Assert.fail("IllegalArgumentException expected: " + notExpected);
        } catch (IllegalArgumentException e) {
            // Expected
        }
        try {
            Polygon2D notExpected = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_5_ID).getStartRegion();
            Assert.fail("IllegalArgumentException expected: " + notExpected);
        } catch (IllegalArgumentException e) {
            // Expected
        }

        assertEmptyCount(StartRegionLevelConfigEntity.class);
        assertEmptyCountNative("SERVER_START_REGION_LEVEL_CONFIG_POLYGON");
    }

    @Test
    public void crudMasterPlanetConfig() throws Exception {
        Assert.assertTrue(serverGameEnginePersistence.readMasterPlanetConfig().getResourceRegionConfigs().isEmpty());
        List<ResourceRegionConfig> expectedResourceRegionConfigs = setupResourceRegionConfigs1();
        serverGameEnginePersistence.updateResourceRegionConfigs(expectedResourceRegionConfigs);
        List<ResourceRegionConfig> actualResourceRegionConfigs = serverGameEnginePersistence.readMasterPlanetConfig().getResourceRegionConfigs();

        ReflectionAssert.assertReflectionEquals(expectedResourceRegionConfigs, actualResourceRegionConfigs);

        serverGameEnginePersistence.updateResourceRegionConfigs(new ArrayList<>());
        Assert.assertTrue(serverGameEnginePersistence.readMasterPlanetConfig().getResourceRegionConfigs().isEmpty());

        assertEmptyCount(PlaceConfigEntity.class);
        assertEmptyCount(ServerResourceRegionConfigEntity.class);
    }

    private List<BotConfig> setupServerBots1() {
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_FACTORY_ID).setCount(3).setCreateDirectly(true).setPlace(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER_ID).setCount(6).setPlace(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));
        return botConfigs;
    }

    private List<ResourceRegionConfig> setupResourceRegionConfigs1() {
        List<ResourceRegionConfig> resourceRegionConfigs = new ArrayList<>();
        resourceRegionConfigs.add(new ResourceRegionConfig().setCount(10).setMinDistanceToItems(2).setResourceItemTypeId(RESOURCE_ITEM_TYPE_ID).setRegion(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(160, 140, 80, 90))));
        return resourceRegionConfigs;
    }

}