package com.btxtech.server.persistence.server;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.RazAssertTestHelper;
import com.btxtech.server.persistence.PlanetPersistence;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import org.junit.Assert;
import org.junit.Test;

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

    @Test
    public void crudBot() throws Exception {
        setupItemTypes();
        Assert.assertTrue(serverGameEnginePersistence.readBotConfigs().isEmpty());
        serverGameEnginePersistence.updateBotConfigs(setupServerBots1());
        // Assert bot config
        List<BotConfig> botConfigs = new ArrayList<>(serverGameEnginePersistence.readBotConfigs());
        Assert.assertEquals(1, botConfigs.size());
        BotConfig botConfig = botConfigs.get(0);
        Assert.assertFalse(botConfig.isNpc());
        Assert.assertEquals(3000, botConfig.getActionDelay());
        RazAssertTestHelper.assertPlaceConfig(Polygon2D.fromRectangle(200, 100, 50, 75).getCorners(), botConfig.getRealm());
        List<BotEnragementStateConfig> botEnragementStateConfigs = botConfig.getBotEnragementStateConfigs();
        Assert.assertEquals(1, botEnragementStateConfigs.size());
        BotEnragementStateConfig botEnragementStateConfig = botEnragementStateConfigs.get(0);
        Assert.assertEquals("Normal", botEnragementStateConfig.getName());
        Assert.assertNull(botEnragementStateConfig.getEnrageUpKills());
        List<BotItemConfig> botItems = botEnragementStateConfig.getBotItems();
        Assert.assertEquals(2, botItems.size());

        BotItemConfig factory = botItemConfig4BaseItemTypeId(BASE_ITEM_TYPE_FACTORY_ID, botItems);
        Assert.assertEquals(0, factory.getAngle(), 0.001);
        Assert.assertNull(factory.getIdleTtl());
        RazAssertTestHelper.assertPlaceConfig(Polygon2D.fromRectangle(150, 80, 150, 150).getCorners(), factory.getPlace());
        Assert.assertEquals(3, factory.getCount());
        Assert.assertTrue(factory.isCreateDirectly());

        BotItemConfig attacker = botItemConfig4BaseItemTypeId(BASE_ITEM_TYPE_ATTACKER_ID, botItems);
        Assert.assertEquals(0, attacker.getAngle(), 0.001);
        Assert.assertNull(attacker.getIdleTtl());
        RazAssertTestHelper.assertPlaceConfig(Polygon2D.fromRectangle(150, 80, 150, 150).getCorners(), attacker.getPlace());
        Assert.assertEquals(6, attacker.getCount());
        Assert.assertFalse(attacker.isCreateDirectly());

        // Remove all bots
        serverGameEnginePersistence.updateBotConfigs(new ArrayList<>());
        Assert.assertTrue(serverGameEnginePersistence.readBotConfigs().isEmpty());

        Assert.assertEquals(0L, entityManager.createQuery("SELECT COUNT(b) FROM BotConfigEntity b").getSingleResult());
        Assert.assertEquals(0L, entityManager.createQuery("SELECT COUNT(b) FROM BotEnragementStateConfigEntity b").getSingleResult());
        Assert.assertEquals(0L, entityManager.createQuery("SELECT COUNT(b) FROM BotItemConfigEntity b").getSingleResult());
        Assert.assertEquals(0L, entityManager.createQuery("SELECT COUNT(p) FROM PlaceConfigEntity p").getSingleResult());
        cleanItemTypes();
    }

    @Test
    public void crudPlanetConfig() throws Exception {
        Assert.assertNull(serverGameEnginePersistence.readPlanetConfig());
        int planetId = planetPersistence.createPlanetConfig();
        serverGameEnginePersistence.updatePlanetConfig(planetId);
        Assert.assertEquals(planetId, serverGameEnginePersistence.readPlanetConfig().getPlanetId());
        try {
            planetPersistence.deletePlanetConfig(planetId);
            Assert.fail("Exception expected");
        } catch (Throwable t) {
            // Expected
        }
        serverGameEnginePersistence.updatePlanetConfig(null);
        planetPersistence.deletePlanetConfig(planetId);
    }

    @Test
    public void crudSlavePlanetConfig() throws Exception {
        Assert.assertNull(serverGameEnginePersistence.readSlavePlanetConfig().getStartRegion());
        serverGameEnginePersistence.updateStartRegion(Arrays.asList(new DecimalPosition(1, 1), new DecimalPosition(2, 2), new DecimalPosition(3, 3)));
        RazAssertTestHelper.assertDecimalPositions(Arrays.asList(new DecimalPosition(1, 1), new DecimalPosition(2, 2), new DecimalPosition(3, 3)), serverGameEnginePersistence.readSlavePlanetConfig().getStartRegion().getCorners());
        serverGameEnginePersistence.updateStartRegion(new ArrayList<>());
        Assert.assertNull(serverGameEnginePersistence.readSlavePlanetConfig().getStartRegion());

        Assert.assertEquals(0, ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM SERVER_GAME_ENGINE_START_REGION").getSingleResult()).intValue());
    }

    @Test
    public void crudMasterPlanetConfig() throws Exception {
        setupItemTypes();
        Assert.assertTrue(serverGameEnginePersistence.readMasterPlanetConfig().getResourceRegionConfigs().isEmpty());
        List<ResourceRegionConfig> resourceRegionConfigs = new ArrayList<>();
        resourceRegionConfigs.add(new ResourceRegionConfig().setCount(4).setRegion(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))).setResourceItemTypeId(RESOURCE_ITEM_TYPE_ID).setMinDistanceToItems(9.9));
        resourceRegionConfigs.add(new ResourceRegionConfig().setCount(10).setRegion(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(250, 222, 111, 333))).setResourceItemTypeId(RESOURCE_ITEM_TYPE_ID).setMinDistanceToItems(8.9));
        serverGameEnginePersistence.updateResourceRegionConfigs(resourceRegionConfigs);
        resourceRegionConfigs = serverGameEnginePersistence.readMasterPlanetConfig().getResourceRegionConfigs();
        Assert.assertEquals(2, resourceRegionConfigs.size());
        ResourceRegionConfig resourceRegionConfig1;
        ResourceRegionConfig resourceRegionConfig2;
        if (resourceRegionConfigs.get(0).getCount() == 4) {
            resourceRegionConfig1 = resourceRegionConfigs.get(0);
            resourceRegionConfig2 = resourceRegionConfigs.get(1);
        } else if (resourceRegionConfigs.get(1).getCount() == 10) {
            resourceRegionConfig1 = resourceRegionConfigs.get(1);
            resourceRegionConfig2 = resourceRegionConfigs.get(0);
        } else {
            throw new IllegalStateException();
        }

        Assert.assertEquals(RESOURCE_ITEM_TYPE_ID, resourceRegionConfig1.getResourceItemTypeId());
        Assert.assertEquals(4, resourceRegionConfig1.getCount());
        Assert.assertEquals(9.9, resourceRegionConfig1.getMinDistanceToItems(), 0.0001);
        RazAssertTestHelper.assertPlaceConfig(Polygon2D.fromRectangle(150, 80, 150, 150).getCorners(), resourceRegionConfig1.getRegion());

        Assert.assertEquals(RESOURCE_ITEM_TYPE_ID, resourceRegionConfig2.getResourceItemTypeId());
        Assert.assertEquals(10, resourceRegionConfig2.getCount());
        Assert.assertEquals(8.9, resourceRegionConfig2.getMinDistanceToItems(), 0.0001);
        RazAssertTestHelper.assertPlaceConfig(Polygon2D.fromRectangle(250, 222, 111, 333).getCorners(), resourceRegionConfig2.getRegion());

        serverGameEnginePersistence.updateResourceRegionConfigs(new ArrayList<>());
        Assert.assertTrue(serverGameEnginePersistence.readMasterPlanetConfig().getResourceRegionConfigs().isEmpty());

        Assert.assertEquals(0L, entityManager.createQuery("SELECT COUNT(p) FROM PlaceConfigEntity p").getSingleResult());
        cleanItemTypes();
    }

    private List<BotConfig> setupServerBots1() {
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(ArquillianBaseTest.BASE_ITEM_TYPE_FACTORY_ID).setCount(3).setCreateDirectly(true).setPlace(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botItems.add(new BotItemConfig().setBaseItemTypeId(ArquillianBaseTest.BASE_ITEM_TYPE_ATTACKER_ID).setCount(6).setPlace(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setRealm(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(200, 100, 50, 75))).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));
        return botConfigs;
    }

    public BotItemConfig botItemConfig4BaseItemTypeId(int baseItemTypeId, List<BotItemConfig> botItemConfigs) {
        return botItemConfigs.stream().filter(botItemConfig -> botItemConfig.getBaseItemTypeId() == baseItemTypeId).findFirst().orElseThrow(() -> new IllegalArgumentException("No BotItemConfig for id: " + baseItemTypeId));
    }
}