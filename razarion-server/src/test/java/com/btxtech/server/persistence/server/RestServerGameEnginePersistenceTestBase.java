package com.btxtech.server.persistence.server;

import com.btxtech.server.RestServerTestBase;
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
public class RestServerGameEnginePersistenceTestBase extends RestServerTestBase {
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

    // @Test
    // If orphan removal fails see: https://hibernate.atlassian.net/browse/HHH-9663
    public void crudBot() throws Exception {
        Assert.assertTrue(serverGameEnginePersistence.readBotConfigs().isEmpty());
        ObjectComparatorIgnore.add(BotConfig.class, "id");
        Assert.assertTrue(serverGameEnginePersistence.readBotConfigs().isEmpty());

        // Create first
        BotConfig expectedBotConfig1 = serverGameEnginePersistence.getBotConfigCrud().create();
        setupServerBots1(expectedBotConfig1);
        serverGameEnginePersistence.getBotConfigCrud().update(expectedBotConfig1);
        TestHelper.assertObjectNameIds(serverGameEnginePersistence.getBotConfigCrud().readObjectNameIds(), "Int bot 1");
        int id = TestHelper.findIdForName(serverGameEnginePersistence.getBotConfigCrud().readObjectNameIds(), "Int bot 1");
        BotConfig actualBotConfig1 = serverGameEnginePersistence.getBotConfigCrud().read(id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig1, actualBotConfig1);
        actualBotConfig1 = TestHelper.findObjectForId(serverGameEnginePersistence.readBotConfigs(), id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig1, actualBotConfig1);
        Assert.assertEquals(1, serverGameEnginePersistence.readBotConfigs().size());
        // Create second
        BotConfig expectedBotConfig2 = serverGameEnginePersistence.getBotConfigCrud().create();
        setupServerBots2(expectedBotConfig2);
        serverGameEnginePersistence.getBotConfigCrud().update(expectedBotConfig2);
        TestHelper.assertObjectNameIds(serverGameEnginePersistence.getBotConfigCrud().readObjectNameIds(), "Int bot 1", "Int bot 22");
        id = TestHelper.findIdForName(serverGameEnginePersistence.getBotConfigCrud().readObjectNameIds(), "Int bot 1");
        actualBotConfig1 = serverGameEnginePersistence.getBotConfigCrud().read(id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig1, actualBotConfig1);
        actualBotConfig1 = TestHelper.findObjectForId(serverGameEnginePersistence.readBotConfigs(), id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig1, actualBotConfig1);
        Assert.assertEquals(2, serverGameEnginePersistence.readBotConfigs().size());
        id = TestHelper.findIdForName(serverGameEnginePersistence.getBotConfigCrud().readObjectNameIds(), "Int bot 22");
        BotConfig actualBotConfig2 = serverGameEnginePersistence.getBotConfigCrud().read(id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig2, actualBotConfig2);
        actualBotConfig2 = TestHelper.findObjectForId(serverGameEnginePersistence.readBotConfigs(), id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig2, actualBotConfig2);
        // Change first
        id = TestHelper.findIdForName(serverGameEnginePersistence.getBotConfigCrud().readObjectNameIds(), "Int bot 1");
        expectedBotConfig1 = serverGameEnginePersistence.getBotConfigCrud().read(id);
        setupServerBots2(expectedBotConfig1);
        expectedBotConfig1.setInternalName("bhfkdf");
        serverGameEnginePersistence.getBotConfigCrud().update(expectedBotConfig1);
        TestHelper.assertObjectNameIds(serverGameEnginePersistence.getBotConfigCrud().readObjectNameIds(), "bhfkdf", "Int bot 22");
        id = TestHelper.findIdForName(serverGameEnginePersistence.getBotConfigCrud().readObjectNameIds(), "bhfkdf");
        actualBotConfig1 = serverGameEnginePersistence.getBotConfigCrud().read(id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig1, actualBotConfig1);
        actualBotConfig1 = TestHelper.findObjectForId(serverGameEnginePersistence.readBotConfigs(), id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig1, actualBotConfig1);
        Assert.assertEquals(2, serverGameEnginePersistence.readBotConfigs().size());
        id = TestHelper.findIdForName(serverGameEnginePersistence.getBotConfigCrud().readObjectNameIds(), "Int bot 22");
        actualBotConfig2 = serverGameEnginePersistence.getBotConfigCrud().read(id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig2, actualBotConfig2);
        actualBotConfig2 = TestHelper.findObjectForId(serverGameEnginePersistence.readBotConfigs(), id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig2, actualBotConfig2);
        // Delete second
        id = TestHelper.findIdForName(serverGameEnginePersistence.getBotConfigCrud().readObjectNameIds(), "Int bot 22");
        serverGameEnginePersistence.getBotConfigCrud().delete(id);
        TestHelper.assertObjectNameIds(serverGameEnginePersistence.getBotConfigCrud().readObjectNameIds(), "bhfkdf");
        id = TestHelper.findIdForName(serverGameEnginePersistence.getBotConfigCrud().readObjectNameIds(), "bhfkdf");
        actualBotConfig1 = serverGameEnginePersistence.getBotConfigCrud().read(id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig1, actualBotConfig1);
        actualBotConfig1 = TestHelper.findObjectForId(serverGameEnginePersistence.readBotConfigs(), id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig1, actualBotConfig1);
        Assert.assertEquals(1, serverGameEnginePersistence.readBotConfigs().size());
        // Delete first
        id = TestHelper.findIdForName(serverGameEnginePersistence.getBotConfigCrud().readObjectNameIds(), "bhfkdf");
        serverGameEnginePersistence.getBotConfigCrud().delete(id);
        TestHelper.assertObjectNameIds(serverGameEnginePersistence.getBotConfigCrud().readObjectNameIds());
        Assert.assertTrue(serverGameEnginePersistence.readBotConfigs().isEmpty());

        ObjectComparatorIgnore.clear();

        Assert.assertEquals(0L, entityManager.createQuery("SELECT COUNT(b) FROM BotConfigEntity b").getSingleResult());
        Assert.assertEquals(0L, entityManager.createQuery("SELECT COUNT(b) FROM BotEnragementStateConfigEntity b").getSingleResult());
        Assert.assertEquals(0L, entityManager.createQuery("SELECT COUNT(b) FROM BotItemConfigEntity b").getSingleResult());
        Assert.assertEquals(0L, entityManager.createQuery("SELECT COUNT(p) FROM PlaceConfigEntity p").getSingleResult()); // If orphan removal fails see: https://hibernate.atlassian.net/browse/HHH-9663
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

        TestHelper.assertObjectNameIds(serverGameEnginePersistence.readStartRegionObjectNameIds(), "int name 1", "int name 2");
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

        TestHelper.assertObjectNameIds(serverGameEnginePersistence.readStartRegionObjectNameIds(), "int name 1", "int name 3");
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

        TestHelper.assertObjectNameIds(serverGameEnginePersistence.readStartRegionObjectNameIds(), "int name 4", "int name 3");
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

        TestHelper.assertObjectNameIds(serverGameEnginePersistence.readStartRegionObjectNameIds(), "int name 5", "int name 3");
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

        ObjectComparatorIgnore.add(ResourceRegionConfig.class, "id");
        ReflectionAssert.assertReflectionEquals(expectedResourceRegionConfigs, actualResourceRegionConfigs);
        ObjectComparatorIgnore.clear();

        serverGameEnginePersistence.updateResourceRegionConfigs(new ArrayList<>());
        Assert.assertTrue(serverGameEnginePersistence.readMasterPlanetConfig().getResourceRegionConfigs().isEmpty());

        assertEmptyCount(PlaceConfigEntity.class);
        assertEmptyCount(ServerResourceRegionConfigEntity.class);
    }

    // @Test
    // If orphan removal fails see: https://hibernate.atlassian.net/browse/HHH-9663
    public void testResourceRegionConfigCrud() throws Exception {
        Assert.assertTrue(serverGameEnginePersistence.getResourceRegionConfigCrud().readObjectNameIds().isEmpty());
        // Create first
        ResourceRegionConfig expectedResourceRegionConfig1 = serverGameEnginePersistence.getResourceRegionConfigCrud().create();
        expectedResourceRegionConfig1.setInternalName("res1").setCount(100).setMinDistanceToItems(15.789).setRegion(TestHelper.placeConfigPolygonFromRect(100, 200, 300, 400)).setResourceItemTypeId(RESOURCE_ITEM_TYPE_ID);
        serverGameEnginePersistence.getResourceRegionConfigCrud().update(expectedResourceRegionConfig1);
        // Verify
        TestHelper.assertObjectNameIds(serverGameEnginePersistence.getResourceRegionConfigCrud().readObjectNameIds(), "res1");
        int id = TestHelper.findIdForName(serverGameEnginePersistence.getResourceRegionConfigCrud().readObjectNameIds(), "res1");
        ResourceRegionConfig actualResourceRegionConfig1 = serverGameEnginePersistence.getResourceRegionConfigCrud().read(id);
        ReflectionAssert.assertReflectionEquals(expectedResourceRegionConfig1, actualResourceRegionConfig1);
        // Change
        id = TestHelper.findIdForName(serverGameEnginePersistence.getResourceRegionConfigCrud().readObjectNameIds(), "res1");
        ResourceRegionConfig expectedResourceRegionConfig2 = serverGameEnginePersistence.getResourceRegionConfigCrud().read(id);
        expectedResourceRegionConfig2.setInternalName("res1").setCount(254).setMinDistanceToItems(5653.77).setRegion(TestHelper.placeConfigPolygonFromRect(55, 44, 22, 77)).setResourceItemTypeId(RESOURCE_ITEM_TYPE_ID);
        serverGameEnginePersistence.getResourceRegionConfigCrud().update(expectedResourceRegionConfig2);
        // Verify
        TestHelper.assertObjectNameIds(serverGameEnginePersistence.getResourceRegionConfigCrud().readObjectNameIds(), "res1");
        id = TestHelper.findIdForName(serverGameEnginePersistence.getResourceRegionConfigCrud().readObjectNameIds(), "res1");
        ResourceRegionConfig actualResourceRegionConfig2 = serverGameEnginePersistence.getResourceRegionConfigCrud().read(id);
        ReflectionAssert.assertReflectionEquals(expectedResourceRegionConfig2, actualResourceRegionConfig2);
        // Change
        id = TestHelper.findIdForName(serverGameEnginePersistence.getResourceRegionConfigCrud().readObjectNameIds(), "res1");
        ResourceRegionConfig expectedResourceRegionConfig3 = serverGameEnginePersistence.getResourceRegionConfigCrud().read(id);
        expectedResourceRegionConfig3.setInternalName("xddfedsfdsfds afdasf").setCount(12).setMinDistanceToItems(1.78).setRegion(null).setResourceItemTypeId(null);
        serverGameEnginePersistence.getResourceRegionConfigCrud().update(expectedResourceRegionConfig3);
        // Verify
        TestHelper.assertObjectNameIds(serverGameEnginePersistence.getResourceRegionConfigCrud().readObjectNameIds(), "xddfedsfdsfds afdasf");
        id = TestHelper.findIdForName(serverGameEnginePersistence.getResourceRegionConfigCrud().readObjectNameIds(), "xddfedsfdsfds afdasf");
        ResourceRegionConfig actualResourceRegionConfig3 = serverGameEnginePersistence.getResourceRegionConfigCrud().read(id);
        ReflectionAssert.assertReflectionEquals(expectedResourceRegionConfig3, actualResourceRegionConfig3);
        // Create second
        ResourceRegionConfig expectedResourceRegionConfig4 = serverGameEnginePersistence.getResourceRegionConfigCrud().create();
        expectedResourceRegionConfig4.setInternalName("adfasdf lllll").setCount(12).setMinDistanceToItems(23.2).setRegion(TestHelper.placeConfigPolygonFromRect(44, 11, 56, 32)).setResourceItemTypeId(RESOURCE_ITEM_TYPE_ID);
        serverGameEnginePersistence.getResourceRegionConfigCrud().update(expectedResourceRegionConfig4);
        // Verify
        TestHelper.assertObjectNameIds(serverGameEnginePersistence.getResourceRegionConfigCrud().readObjectNameIds(), "xddfedsfdsfds afdasf", "adfasdf lllll");
        id = TestHelper.findIdForName(serverGameEnginePersistence.getResourceRegionConfigCrud().readObjectNameIds(), "xddfedsfdsfds afdasf");
        actualResourceRegionConfig3 = serverGameEnginePersistence.getResourceRegionConfigCrud().read(id);
        ReflectionAssert.assertReflectionEquals(expectedResourceRegionConfig3, actualResourceRegionConfig3);
        id = TestHelper.findIdForName(serverGameEnginePersistence.getResourceRegionConfigCrud().readObjectNameIds(), "adfasdf lllll");
        ResourceRegionConfig actualResourceRegionConfig4 = serverGameEnginePersistence.getResourceRegionConfigCrud().read(id);
        ReflectionAssert.assertReflectionEquals(expectedResourceRegionConfig4, actualResourceRegionConfig4);
        // Delete first
        id = TestHelper.findIdForName(serverGameEnginePersistence.getResourceRegionConfigCrud().readObjectNameIds(), "xddfedsfdsfds afdasf");
        serverGameEnginePersistence.getResourceRegionConfigCrud().delete(id);
        // Verify
        id = TestHelper.findIdForName(serverGameEnginePersistence.getResourceRegionConfigCrud().readObjectNameIds(), "adfasdf lllll");
        actualResourceRegionConfig4 = serverGameEnginePersistence.getResourceRegionConfigCrud().read(id);
        ReflectionAssert.assertReflectionEquals(expectedResourceRegionConfig4, actualResourceRegionConfig4);
        // Delete second
        id = TestHelper.findIdForName(serverGameEnginePersistence.getResourceRegionConfigCrud().readObjectNameIds(), "adfasdf lllll");
        serverGameEnginePersistence.getResourceRegionConfigCrud().delete(id);
        // Verify
        TestHelper.assertObjectNameIds(serverGameEnginePersistence.getResourceRegionConfigCrud().readObjectNameIds());

        assertEmptyCount(ServerResourceRegionConfigEntity.class);
        assertEmptyCount(PlaceConfigEntity.class); // If orphan removal fails see: https://hibernate.atlassian.net/browse/HHH-9663
        assertEmptyCountNative("PLACE_CONFIG_POSITION_POLYGON");
    }

    private void setupServerBots1(BotConfig botConfig) {
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_FACTORY_ID).setCount(3).setCreateDirectly(true).setPlace(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER_ID).setCount(6).setPlace(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfig.setInternalName("Int bot 1").setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false);
    }

    private void setupServerBots2(BotConfig botConfig) {
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems1 = new ArrayList<>();
        botItems1.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_FACTORY_ID).setCount(3).setCreateDirectly(true).setPlace(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems1));
        List<BotItemConfig> botItems2 = new ArrayList<>();
        botItems2.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_TOWER_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(157, 88, 150, 151))));
        botItems2.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER_ID).setCount(2).setPlace(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(152, 82, 154, 155))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Norma2").setBotItems(botItems2));
        botConfig.setInternalName("Int bot 22").setActionDelay(300).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kennffffffy").setNpc(true);
    }

    private List<ResourceRegionConfig> setupResourceRegionConfigs1() {
        List<ResourceRegionConfig> resourceRegionConfigs = new ArrayList<>();
        resourceRegionConfigs.add(new ResourceRegionConfig().setCount(10).setMinDistanceToItems(2).setResourceItemTypeId(RESOURCE_ITEM_TYPE_ID).setRegion(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(160, 140, 80, 90))));
        return resourceRegionConfigs;
    }

}