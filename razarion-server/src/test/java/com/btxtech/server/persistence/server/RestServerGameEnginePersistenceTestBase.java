package com.btxtech.server.persistence.server;

import com.btxtech.server.IgnoreOldArquillianTest;
import com.btxtech.server.persistence.PlanetCrudPersistence;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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

    private ServerGameEngineCrudPersistence serverGameEngineCrudPersistence;

    private PlanetCrudPersistence planetCrudPersistence;

    private EntityManager entityManager;

    @Inject
    public RestServerGameEnginePersistenceTestBase(PlanetCrudPersistence planetCrudPersistence, ServerGameEngineCrudPersistence serverGameEngineCrudPersistence) {
        this.planetCrudPersistence = planetCrudPersistence;
        this.serverGameEngineCrudPersistence = serverGameEngineCrudPersistence;
    }

    @Before
    public void before() throws Exception {
        setupPlanetDb();
    }

    @After
    public void after() throws Exception {
        cleanPlanets();
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
        botItems.add(new BotItemConfig().baseItemTypeId(BASE_ITEM_TYPE_FACTORY_ID).count(3).createDirectly(true).place(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botItems.add(new BotItemConfig().baseItemTypeId(BASE_ITEM_TYPE_ATTACKER_ID).count(6).place(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().name("Normal").botItems(botItems));
        botConfig.internalName("Int bot 1").actionDelay(3000).botEnragementStateConfigs(botEnragementStateConfigs).name("Kenny").npc(false);
    }

    private void setupServerBots2(BotConfig botConfig) {
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems1 = new ArrayList<>();
        botItems1.add(new BotItemConfig().baseItemTypeId(BASE_ITEM_TYPE_FACTORY_ID).count(3).createDirectly(true).place(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().name("Normal").botItems(botItems1));
        List<BotItemConfig> botItems2 = new ArrayList<>();
        botItems2.add(new BotItemConfig().baseItemTypeId(BASE_ITEM_TYPE_TOWER_ID).count(1).createDirectly(true).place(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(157, 88, 150, 151))));
        botItems2.add(new BotItemConfig().baseItemTypeId(BASE_ITEM_TYPE_BULLDOZER_ID).count(2).place(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(152, 82, 154, 155))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().name("Norma2").botItems(botItems2));
        botConfig.internalName("Int bot 22").actionDelay(300).botEnragementStateConfigs(botEnragementStateConfigs).name("Kennffffffy").npc(true);
    }

    private List<ResourceRegionConfig> setupResourceRegionConfigs1() {
        List<ResourceRegionConfig> resourceRegionConfigs = new ArrayList<>();
        resourceRegionConfigs.add(new ResourceRegionConfig().count(10).minDistanceToItems(2).resourceItemTypeId(RESOURCE_ITEM_TYPE_ID).region(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(160, 140, 80, 90))));
        return resourceRegionConfigs;
    }

}