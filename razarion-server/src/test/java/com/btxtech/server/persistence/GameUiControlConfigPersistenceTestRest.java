package com.btxtech.server.persistence;

import com.btxtech.server.RestServerTestBase;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiControlConfig;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.WarmGameUiControlConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Locale;

/**
 * Created by Beat
 * 10.05.2017.
 */
public class GameUiControlConfigPersistenceTestRest extends RestServerTestBase {
    @Inject
    private GameUiControlConfigPersistence gameUiControlConfigPersistence;
    @Inject
    private UserService userService;

    @Before
    public void before() throws Exception {
        setupPlanets();
    }

    @After
    public void after() throws Exception {
        cleanPlanets();
    }

    @Test
    public void testLoad() throws Exception {
        Locale locale = Locale.ENGLISH;

        UserContext userContext = userService.getUserContextFromSession();
        userContext.setLevelId(LEVEL_1_ID);
        ColdGameUiControlConfig gameUiControlConfig = gameUiControlConfigPersistence.load(new GameUiControlInput(), locale, userContext);
        Assert.assertEquals(PLANET_1_ID, gameUiControlConfig.getWarmGameUiControlConfig().getPlanetConfig().getPlanetId());

        userContext = userService.getUserContextFromSession();
        userContext.setLevelId(LEVEL_2_ID);
        gameUiControlConfig = gameUiControlConfigPersistence.load(new GameUiControlInput(), locale, userContext);
        Assert.assertEquals(PLANET_1_ID, gameUiControlConfig.getWarmGameUiControlConfig().getPlanetConfig().getPlanetId());

        userContext = userService.getUserContextFromSession();
        userContext.setLevelId(LEVEL_3_ID);
        gameUiControlConfig = gameUiControlConfigPersistence.load(new GameUiControlInput(), locale, userContext);
        Assert.assertEquals(PLANET_1_ID, gameUiControlConfig.getWarmGameUiControlConfig().getPlanetConfig().getPlanetId());

        userContext = userService.getUserContextFromSession();
        userContext.setLevelId(LEVEL_4_ID);
        gameUiControlConfig = gameUiControlConfigPersistence.load(new GameUiControlInput(), locale, userContext);
        Assert.assertEquals(PLANET_2_ID, gameUiControlConfig.getWarmGameUiControlConfig().getPlanetConfig().getPlanetId());
    }

    @Test
    public void testLoadWarm() throws Exception {
        Locale locale = Locale.ENGLISH;

        UserContext userContext = userService.getUserContextFromSession();
        userContext.setLevelId(LEVEL_1_ID);
        WarmGameUiControlConfig warmGameUiControlConfig = gameUiControlConfigPersistence.loadWarm(locale, userContext);
        Assert.assertEquals(PLANET_1_ID, warmGameUiControlConfig.getPlanetConfig().getPlanetId());

        userContext = userService.getUserContextFromSession();
        userContext.setLevelId(LEVEL_2_ID);
        warmGameUiControlConfig = gameUiControlConfigPersistence.loadWarm(locale, userContext);
        Assert.assertEquals(PLANET_1_ID, warmGameUiControlConfig.getPlanetConfig().getPlanetId());

        userContext = userService.getUserContextFromSession();
        userContext.setLevelId(LEVEL_3_ID);
        warmGameUiControlConfig = gameUiControlConfigPersistence.loadWarm(locale, userContext);
        Assert.assertEquals(PLANET_1_ID, warmGameUiControlConfig.getPlanetConfig().getPlanetId());

        userContext = userService.getUserContextFromSession();
        userContext.setLevelId(LEVEL_4_ID);
        warmGameUiControlConfig = gameUiControlConfigPersistence.loadWarm(locale, userContext);
        Assert.assertEquals(PLANET_2_ID, warmGameUiControlConfig.getPlanetConfig().getPlanetId());
    }
}