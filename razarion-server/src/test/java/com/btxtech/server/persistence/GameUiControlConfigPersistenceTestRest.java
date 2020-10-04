package com.btxtech.server.persistence;

import com.btxtech.server.IgnoreOldArquillianTest;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.WarmGameUiContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Locale;

/**
 * Created by Beat
 * 10.05.2017.
 */
@Ignore
public class GameUiControlConfigPersistenceTestRest extends IgnoreOldArquillianTest {
    @Inject
    private GameUiContextCrudPersistence gameUiContextCrudPersistence;
    @Inject
    private UserService userService;

    @Before
    public void before() throws Exception {
        setupPlanetDb();
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
        ColdGameUiContext gameUiControlConfig = gameUiContextCrudPersistence.loadCold(new GameUiControlInput(), locale, userContext);
        Assert.assertEquals(PLANET_1_ID, gameUiControlConfig.getWarmGameUiContext().getPlanetConfig().getId());

        userContext = userService.getUserContextFromSession();
        userContext.setLevelId(LEVEL_2_ID);
        gameUiControlConfig = gameUiContextCrudPersistence.loadCold(new GameUiControlInput(), locale, userContext);
        Assert.assertEquals(PLANET_1_ID, gameUiControlConfig.getWarmGameUiContext().getPlanetConfig().getId());

        userContext = userService.getUserContextFromSession();
        userContext.setLevelId(LEVEL_3_ID);
        gameUiControlConfig = gameUiContextCrudPersistence.loadCold(new GameUiControlInput(), locale, userContext);
        Assert.assertEquals(PLANET_1_ID, gameUiControlConfig.getWarmGameUiContext().getPlanetConfig().getId());

        userContext = userService.getUserContextFromSession();
        userContext.setLevelId(LEVEL_4_ID);
        gameUiControlConfig = gameUiContextCrudPersistence.loadCold(new GameUiControlInput(), locale, userContext);
        Assert.assertEquals(PLANET_2_ID, gameUiControlConfig.getWarmGameUiContext().getPlanetConfig().getId());
    }

    @Test
    public void testLoadWarm() throws Exception {
        Locale locale = Locale.ENGLISH;

        UserContext userContext = userService.getUserContextFromSession();
        userContext.setLevelId(LEVEL_1_ID);
        WarmGameUiContext warmGameUiContext = gameUiContextCrudPersistence.loadWarm(locale, userContext);
        Assert.assertEquals(PLANET_1_ID, warmGameUiContext.getPlanetConfig().getId());

        userContext = userService.getUserContextFromSession();
        userContext.setLevelId(LEVEL_2_ID);
        warmGameUiContext = gameUiContextCrudPersistence.loadWarm(locale, userContext);
        Assert.assertEquals(PLANET_1_ID, warmGameUiContext.getPlanetConfig().getId());

        userContext = userService.getUserContextFromSession();
        userContext.setLevelId(LEVEL_3_ID);
        warmGameUiContext = gameUiContextCrudPersistence.loadWarm(locale, userContext);
        Assert.assertEquals(PLANET_1_ID, warmGameUiContext.getPlanetConfig().getId());

        userContext = userService.getUserContextFromSession();
        userContext.setLevelId(LEVEL_4_ID);
        warmGameUiContext = gameUiContextCrudPersistence.loadWarm(locale, userContext);
        Assert.assertEquals(PLANET_2_ID, warmGameUiContext.getPlanetConfig().getId());
    }
}