package com.btxtech.server.persistence.server;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.TestHelper;
import com.btxtech.server.persistence.PlaceConfigEntity;
import com.btxtech.server.persistence.PlanetEntity;
import com.btxtech.server.persistence.PlanetPersistence;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.dto.StartRegionConfig;
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
        } catch (Exception t) {
            // Expected
        }
        assertCount(3, PlanetEntity.class);

        serverGameEnginePersistence.updatePlanetConfig(null);
        assertCount(3, PlanetEntity.class);

        planetPersistence.deletePlanetConfig(planetId);
        assertCount(2, PlanetEntity.class);
    }

    @Test
    public void crudStartRegion() throws Exception {
        Assert.assertNull(serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_4_ID).getStartRegion());
        Assert.assertNull(serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_5_ID).getStartRegion());
        Assert.assertTrue(serverGameEnginePersistence.readStartRegionObjectNameIds().isEmpty());

        // Add first
        Polygon2D expectedStartRegion1 = new Polygon2D(Arrays.asList(new DecimalPosition(100, 100), new DecimalPosition(300, 100), new DecimalPosition(300, 300), new DecimalPosition(100, 300)));
        StartRegionConfig expectedStartRegionConfig1 = serverGameEnginePersistence.createStartRegionConfig();
        expectedStartRegionConfig1.setMinimalLevelId(LEVEL_4_ID).setRegion(expectedStartRegion1).setInternalName("int name 1");
        serverGameEnginePersistence.updateStartRegionConfig(expectedStartRegionConfig1);

        // Verify
        Polygon2D actualStartRegion = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_4_ID).getStartRegion();
        ReflectionAssert.assertReflectionEquals(expectedStartRegion1, actualStartRegion);

        actualStartRegion = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_5_ID).getStartRegion();
        ReflectionAssert.assertReflectionEquals(expectedStartRegion1, actualStartRegion);

        TestHelper.assertObjectNameIds(serverGameEnginePersistence.readStartRegionObjectNameIds(), "int name 1");
        int id = TestHelper.findIdForName(serverGameEnginePersistence.readStartRegionObjectNameIds(), "int name 1");
        StartRegionConfig actualStartRegionConfig1 = serverGameEnginePersistence.readStartRegionConfig(id);
        ReflectionAssert.assertReflectionEquals(expectedStartRegionConfig1, actualStartRegionConfig1);

        // Add second
        Polygon2D expectedStartRegion2 = new Polygon2D(Arrays.asList(new DecimalPosition(200, 200), new DecimalPosition(400, 200), new DecimalPosition(400, 400), new DecimalPosition(200, 400)));
        StartRegionConfig expectedStartRegionConfig2 = serverGameEnginePersistence.createStartRegionConfig();
        expectedStartRegionConfig2.setMinimalLevelId(LEVEL_5_ID).setRegion(expectedStartRegion2).setInternalName("int name 2");
        serverGameEnginePersistence.updateStartRegionConfig(expectedStartRegionConfig2);

        // Verify
        actualStartRegion = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_4_ID).getStartRegion();
        ReflectionAssert.assertReflectionEquals(expectedStartRegion1, actualStartRegion);

        actualStartRegion = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_5_ID).getStartRegion();
        ReflectionAssert.assertReflectionEquals(expectedStartRegion2, actualStartRegion);

        TestHelper.assertObjectNameIds(serverGameEnginePersistence.readStartRegionObjectNameIds(), "int name 1","int name 2");
        id = TestHelper.findIdForName(serverGameEnginePersistence.readStartRegionObjectNameIds(), "int name 1");
        actualStartRegionConfig1 = serverGameEnginePersistence.readStartRegionConfig(id);
        ReflectionAssert.assertReflectionEquals(expectedStartRegionConfig1, actualStartRegionConfig1);
        id = TestHelper.findIdForName(serverGameEnginePersistence.readStartRegionObjectNameIds(), "int name 2");
        StartRegionConfig actualStartRegionConfig2 = serverGameEnginePersistence.readStartRegionConfig(id);
        ReflectionAssert.assertReflectionEquals(expectedStartRegionConfig2, actualStartRegionConfig2);

        // Update second
        Polygon2D expectedStartRegion3 = new Polygon2D(Arrays.asList(new DecimalPosition(500, 600), new DecimalPosition(700, 600), new DecimalPosition(400, 1000)));
        expectedStartRegionConfig2.setRegion(expectedStartRegion3).setInternalName("int name 3");
        serverGameEnginePersistence.updateStartRegionConfig(expectedStartRegionConfig2);

        // Verify
        actualStartRegion = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_4_ID).getStartRegion();
        ReflectionAssert.assertReflectionEquals(expectedStartRegion1, actualStartRegion);

        actualStartRegion = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_5_ID).getStartRegion();
        ReflectionAssert.assertReflectionEquals(expectedStartRegion3, actualStartRegion);

        TestHelper.assertObjectNameIds(serverGameEnginePersistence.readStartRegionObjectNameIds(), "int name 1","int name 3");
        id = TestHelper.findIdForName(serverGameEnginePersistence.readStartRegionObjectNameIds(), "int name 1");
        actualStartRegionConfig1 = serverGameEnginePersistence.readStartRegionConfig(id);
        ReflectionAssert.assertReflectionEquals(expectedStartRegionConfig1, actualStartRegionConfig1);
        id = TestHelper.findIdForName(serverGameEnginePersistence.readStartRegionObjectNameIds(), "int name 3");
        actualStartRegionConfig2 = serverGameEnginePersistence.readStartRegionConfig(id);
        ReflectionAssert.assertReflectionEquals(expectedStartRegionConfig2, actualStartRegionConfig2);

        // Update first
        expectedStartRegionConfig1.setRegion(null).setInternalName("int name 4");
        serverGameEnginePersistence.updateStartRegionConfig(expectedStartRegionConfig1);

        // Verify
        Assert.assertNull(serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_4_ID).getStartRegion());

        actualStartRegion = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_5_ID).getStartRegion();
        ReflectionAssert.assertReflectionEquals(expectedStartRegion3, actualStartRegion);

        TestHelper.assertObjectNameIds(serverGameEnginePersistence.readStartRegionObjectNameIds(), "int name 4","int name 3");
        id = TestHelper.findIdForName(serverGameEnginePersistence.readStartRegionObjectNameIds(), "int name 4");
        actualStartRegionConfig1 = serverGameEnginePersistence.readStartRegionConfig(id);
        ReflectionAssert.assertReflectionEquals(expectedStartRegionConfig1, actualStartRegionConfig1);
        id = TestHelper.findIdForName(serverGameEnginePersistence.readStartRegionObjectNameIds(), "int name 3");
        actualStartRegionConfig2 = serverGameEnginePersistence.readStartRegionConfig(id);
        ReflectionAssert.assertReflectionEquals(expectedStartRegionConfig2, actualStartRegionConfig2);

        // Update first
        expectedStartRegionConfig1.setMinimalLevelId(null).setRegion(expectedStartRegion1).setInternalName("int name 5");
        serverGameEnginePersistence.updateStartRegionConfig(expectedStartRegionConfig1);

        // Verify
        Assert.assertNull(serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_4_ID).getStartRegion());

        actualStartRegion = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_5_ID).getStartRegion();
        ReflectionAssert.assertReflectionEquals(expectedStartRegion3, actualStartRegion);

        TestHelper.assertObjectNameIds(serverGameEnginePersistence.readStartRegionObjectNameIds(), "int name 5","int name 3");
        id = TestHelper.findIdForName(serverGameEnginePersistence.readStartRegionObjectNameIds(), "int name 5");
        actualStartRegionConfig1 = serverGameEnginePersistence.readStartRegionConfig(id);
        ReflectionAssert.assertReflectionEquals(expectedStartRegionConfig1, actualStartRegionConfig1);
        id = TestHelper.findIdForName(serverGameEnginePersistence.readStartRegionObjectNameIds(), "int name 3");
        actualStartRegionConfig2 = serverGameEnginePersistence.readStartRegionConfig(id);
        ReflectionAssert.assertReflectionEquals(expectedStartRegionConfig2, actualStartRegionConfig2);

        // Remove second
        expectedStartRegionConfig1.setMinimalLevelId(LEVEL_4_ID).setRegion(expectedStartRegion1).setInternalName("int name 5");
        serverGameEnginePersistence.updateStartRegionConfig(expectedStartRegionConfig1);
        serverGameEnginePersistence.deleteStartRegion(actualStartRegionConfig2.getId());

        // Verify
        actualStartRegion = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_4_ID).getStartRegion();
        ReflectionAssert.assertReflectionEquals(expectedStartRegion1, actualStartRegion);

        actualStartRegion = serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_5_ID).getStartRegion();
        ReflectionAssert.assertReflectionEquals(expectedStartRegion1, actualStartRegion);

        TestHelper.assertObjectNameIds(serverGameEnginePersistence.readStartRegionObjectNameIds(), "int name 5");
        id = TestHelper.findIdForName(serverGameEnginePersistence.readStartRegionObjectNameIds(), "int name 5");
        actualStartRegionConfig1 = serverGameEnginePersistence.readStartRegionConfig(id);
        ReflectionAssert.assertReflectionEquals(expectedStartRegionConfig1, actualStartRegionConfig1);

        // Remove first
        serverGameEnginePersistence.deleteStartRegion(actualStartRegionConfig1.getId());

        Assert.assertNull(serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_4_ID).getStartRegion());
        Assert.assertNull(serverGameEnginePersistence.readSlavePlanetConfig(LEVEL_5_ID).getStartRegion());

        TestHelper.assertObjectNameIds(serverGameEnginePersistence.readStartRegionObjectNameIds());

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