package com.btxtech.server.persistence.server;

import com.btxtech.server.IgnoreOldArquillianTest;
import com.btxtech.server.TestHelper;
import com.btxtech.server.persistence.PlanetCrudPersistence;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.comparator.impl.ObjectComparatorIgnore;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 09.05.2017.
 */
@Ignore
public class RestServerGameEnginePersistenceTestBase extends IgnoreOldArquillianTest {
    @Inject
    private ServerGameEngineCrudPersistence serverGameEngineCrudPersistence;
    @Inject
    private PlanetCrudPersistence planetCrudPersistence;
    @PersistenceContext
    private EntityManager entityManager;

    @Before
    public void before() throws Exception {
        setupPlanetDb();
    }

    @After
    public void after() throws Exception {
        cleanPlanets();
    }

    // @Test
    // If orphan removal fails see: https://hibernate.atlassian.net/browse/HHH-9663
    public void crudBot() throws Exception {
        Assert.assertTrue(serverGameEngineCrudPersistence.readBotConfigs().isEmpty());
        ObjectComparatorIgnore.add(BotConfig.class, "id");
        Assert.assertTrue(serverGameEngineCrudPersistence.readBotConfigs().isEmpty());

        // Create first
        BotConfig expectedBotConfig1 = serverGameEngineCrudPersistence.getBotConfigCrud().create();
        setupServerBots1(expectedBotConfig1);
        serverGameEngineCrudPersistence.getBotConfigCrud().update(expectedBotConfig1);
        TestHelper.assertObjectNameIds(serverGameEngineCrudPersistence.getBotConfigCrud().readObjectNameIds(), "Int bot 1");
        int id = TestHelper.findIdForName(serverGameEngineCrudPersistence.getBotConfigCrud().readObjectNameIds(), "Int bot 1");
        BotConfig actualBotConfig1 = serverGameEngineCrudPersistence.getBotConfigCrud().read(id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig1, actualBotConfig1);
        actualBotConfig1 = TestHelper.findObjectForId(serverGameEngineCrudPersistence.readBotConfigs(), id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig1, actualBotConfig1);
        Assert.assertEquals(1, serverGameEngineCrudPersistence.readBotConfigs().size());
        // Create second
        BotConfig expectedBotConfig2 = serverGameEngineCrudPersistence.getBotConfigCrud().create();
        setupServerBots2(expectedBotConfig2);
        serverGameEngineCrudPersistence.getBotConfigCrud().update(expectedBotConfig2);
        TestHelper.assertObjectNameIds(serverGameEngineCrudPersistence.getBotConfigCrud().readObjectNameIds(), "Int bot 1", "Int bot 22");
        id = TestHelper.findIdForName(serverGameEngineCrudPersistence.getBotConfigCrud().readObjectNameIds(), "Int bot 1");
        actualBotConfig1 = serverGameEngineCrudPersistence.getBotConfigCrud().read(id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig1, actualBotConfig1);
        actualBotConfig1 = TestHelper.findObjectForId(serverGameEngineCrudPersistence.readBotConfigs(), id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig1, actualBotConfig1);
        Assert.assertEquals(2, serverGameEngineCrudPersistence.readBotConfigs().size());
        id = TestHelper.findIdForName(serverGameEngineCrudPersistence.getBotConfigCrud().readObjectNameIds(), "Int bot 22");
        BotConfig actualBotConfig2 = serverGameEngineCrudPersistence.getBotConfigCrud().read(id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig2, actualBotConfig2);
        actualBotConfig2 = TestHelper.findObjectForId(serverGameEngineCrudPersistence.readBotConfigs(), id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig2, actualBotConfig2);
        // Change first
        id = TestHelper.findIdForName(serverGameEngineCrudPersistence.getBotConfigCrud().readObjectNameIds(), "Int bot 1");
        expectedBotConfig1 = serverGameEngineCrudPersistence.getBotConfigCrud().read(id);
        setupServerBots2(expectedBotConfig1);
        expectedBotConfig1.setInternalName("bhfkdf");
        serverGameEngineCrudPersistence.getBotConfigCrud().update(expectedBotConfig1);
        TestHelper.assertObjectNameIds(serverGameEngineCrudPersistence.getBotConfigCrud().readObjectNameIds(), "bhfkdf", "Int bot 22");
        id = TestHelper.findIdForName(serverGameEngineCrudPersistence.getBotConfigCrud().readObjectNameIds(), "bhfkdf");
        actualBotConfig1 = serverGameEngineCrudPersistence.getBotConfigCrud().read(id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig1, actualBotConfig1);
        actualBotConfig1 = TestHelper.findObjectForId(serverGameEngineCrudPersistence.readBotConfigs(), id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig1, actualBotConfig1);
        Assert.assertEquals(2, serverGameEngineCrudPersistence.readBotConfigs().size());
        id = TestHelper.findIdForName(serverGameEngineCrudPersistence.getBotConfigCrud().readObjectNameIds(), "Int bot 22");
        actualBotConfig2 = serverGameEngineCrudPersistence.getBotConfigCrud().read(id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig2, actualBotConfig2);
        actualBotConfig2 = TestHelper.findObjectForId(serverGameEngineCrudPersistence.readBotConfigs(), id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig2, actualBotConfig2);
        // Delete second
        id = TestHelper.findIdForName(serverGameEngineCrudPersistence.getBotConfigCrud().readObjectNameIds(), "Int bot 22");
        serverGameEngineCrudPersistence.getBotConfigCrud().delete(id);
        TestHelper.assertObjectNameIds(serverGameEngineCrudPersistence.getBotConfigCrud().readObjectNameIds(), "bhfkdf");
        id = TestHelper.findIdForName(serverGameEngineCrudPersistence.getBotConfigCrud().readObjectNameIds(), "bhfkdf");
        actualBotConfig1 = serverGameEngineCrudPersistence.getBotConfigCrud().read(id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig1, actualBotConfig1);
        actualBotConfig1 = TestHelper.findObjectForId(serverGameEngineCrudPersistence.readBotConfigs(), id);
        ReflectionAssert.assertReflectionEquals(expectedBotConfig1, actualBotConfig1);
        Assert.assertEquals(1, serverGameEngineCrudPersistence.readBotConfigs().size());
        // Delete first
        id = TestHelper.findIdForName(serverGameEngineCrudPersistence.getBotConfigCrud().readObjectNameIds(), "bhfkdf");
        serverGameEngineCrudPersistence.getBotConfigCrud().delete(id);
        TestHelper.assertObjectNameIds(serverGameEngineCrudPersistence.getBotConfigCrud().readObjectNameIds());
        Assert.assertTrue(serverGameEngineCrudPersistence.readBotConfigs().isEmpty());

        ObjectComparatorIgnore.clear();

        Assert.assertEquals(0L, entityManager.createQuery("SELECT COUNT(b) FROM BotConfigEntity b").getSingleResult());
        Assert.assertEquals(0L, entityManager.createQuery("SELECT COUNT(b) FROM BotEnragementStateConfigEntity b").getSingleResult());
        Assert.assertEquals(0L, entityManager.createQuery("SELECT COUNT(b) FROM BotItemConfigEntity b").getSingleResult());
        Assert.assertEquals(0L, entityManager.createQuery("SELECT COUNT(p) FROM PlaceConfigEntity p").getSingleResult()); // If orphan removal fails see: https://hibernate.atlassian.net/browse/HHH-9663
        assertEmptyCountNative("SERVER_GAME_ENGINE_BOT_CONFIG");
    }

    @Test
    public void crudPlanetConfig() throws Exception {
//    TODO    Assert.assertEquals(PLANET_2_ID, serverGameEngineCrudPersistence.readPlanetConfig().getId());
//
//        int planetId = planetCrudPersistence.createPlanetConfig();
//        serverGameEngineCrudPersistence.updatePlanetConfig(planetId);
//        Assert.assertEquals(planetId, serverGameEngineCrudPersistence.readPlanetConfig().getId());
//        try {
//            planetCrudPersistence.deletePlanetConfig(planetId);
//            Assert.fail("Exception expected");
//        } catch (Exception t) {
//            // Expected
//        }
//        assertCount(3, PlanetEntity.class);
//
//        serverGameEngineCrudPersistence.updatePlanetConfig(null);
//        assertCount(3, PlanetEntity.class);
//
//        planetCrudPersistence.deletePlanetConfig(planetId);
//        assertCount(2, PlanetEntity.class);
    }

    private void setupServerBots1(BotConfig botConfig) {
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_FACTORY_ID).setCount(3).setCreateDirectly(true).setPlace(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER_ID).setCount(6).setPlace(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfig.setInternalName("Int bot 1").setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false);
    }

    private void setupServerBots2(BotConfig botConfig) {
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems1 = new ArrayList<>();
        botItems1.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_FACTORY_ID).setCount(3).setCreateDirectly(true).setPlace(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems1));
        List<BotItemConfig> botItems2 = new ArrayList<>();
        botItems2.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_TOWER_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(157, 88, 150, 151))));
        botItems2.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER_ID).setCount(2).setPlace(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(152, 82, 154, 155))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Norma2").setBotItems(botItems2));
        botConfig.setInternalName("Int bot 22").setActionDelay(300).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kennffffffy").setNpc(true);
    }

    private List<ResourceRegionConfig> setupResourceRegionConfigs1() {
        List<ResourceRegionConfig> resourceRegionConfigs = new ArrayList<>();
        resourceRegionConfigs.add(new ResourceRegionConfig().count(10).minDistanceToItems(2).resourceItemTypeId(RESOURCE_ITEM_TYPE_ID).region(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(160, 140, 80, 90))));
        return resourceRegionConfigs;
    }

}