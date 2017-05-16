package com.btxtech.server.persistence;

import com.btxtech.server.ArquillianBaseTest;
import org.junit.Test;

import javax.inject.Inject;

/**
 * Created by Beat
 * 10.05.2017.
 */
public class GameUiControlConfigPersistenceTest extends ArquillianBaseTest {
    @Inject
    private GameUiControlConfigPersistence gameUiControlConfigPersistence;

    @Test
    public void testLoad() throws Exception {
//        setupPlanets();
//
//        UserContext userContext = new UserContext().setLevelId(LEVEL_1_ID);
//        ColdGameUiControlConfig gameUiControlConfig = gameUiControlConfigPersistence.load(userContext);
//        Assert.assertEquals(PLANET_1_ID, gameUiControlConfig.getPlanetConfig().getPlanetId());
//
//        userContext = new UserContext().setLevelId(LEVEL_2_ID);
//        gameUiControlConfig = gameUiControlConfigPersistence.load(userContext);
//        Assert.assertEquals(PLANET_1_ID, gameUiControlConfig.getPlanetConfig().getPlanetId());
//
//        userContext = new UserContext().setLevelId(LEVEL_3_ID);
//        gameUiControlConfig = gameUiControlConfigPersistence.load(userContext);
//        Assert.assertEquals(PLANET_1_ID, gameUiControlConfig.getPlanetConfig().getPlanetId());
//
//        userContext = new UserContext().setLevelId(LEVEL_4_ID);
//        gameUiControlConfig = gameUiControlConfigPersistence.load(userContext);
//        Assert.assertEquals(PLANET_2_ID, gameUiControlConfig.getPlanetConfig().getPlanetId());
//
//        cleanPlanets();
    }
}