package com.btxtech.server.persistence;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiControlConfig;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.WarmGameUiControlConfig;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Locale;

/**
 * Created by Beat
 * 10.05.2017.
 */
public class GameUiControlConfigPersistenceTest extends ArquillianBaseTest {
    @Inject
    private GameUiControlConfigPersistence gameUiControlConfigPersistence;

    @Test
    public void testLoad() throws Exception {
        setupPlanets();

        Locale locale = Locale.ENGLISH;

        UserContext userContext = new UserContext().setLevelId(LEVEL_1_ID);
        ColdGameUiControlConfig gameUiControlConfig = gameUiControlConfigPersistence.load(new GameUiControlInput(), locale, userContext);
        Assert.assertEquals(PLANET_1_ID, gameUiControlConfig.getWarmGameUiControlConfig().getPlanetConfig().getPlanetId());

        userContext = new UserContext().setLevelId(LEVEL_2_ID);
        gameUiControlConfig = gameUiControlConfigPersistence.load(new GameUiControlInput(), locale, userContext);
        Assert.assertEquals(PLANET_1_ID, gameUiControlConfig.getWarmGameUiControlConfig().getPlanetConfig().getPlanetId());

        userContext = new UserContext().setLevelId(LEVEL_3_ID);
        gameUiControlConfig = gameUiControlConfigPersistence.load(new GameUiControlInput(), locale, userContext);
        Assert.assertEquals(PLANET_1_ID, gameUiControlConfig.getWarmGameUiControlConfig().getPlanetConfig().getPlanetId());

        userContext = new UserContext().setLevelId(LEVEL_4_ID);
        gameUiControlConfig = gameUiControlConfigPersistence.load(new GameUiControlInput(), locale, userContext);
        Assert.assertEquals(PLANET_2_ID, gameUiControlConfig.getWarmGameUiControlConfig().getPlanetConfig().getPlanetId());

        cleanPlanets();
    }

    @Test
    public void testLoadWarm() throws Exception {
        setupPlanets();

        Locale locale = Locale.ENGLISH;

        UserContext userContext = new UserContext().setLevelId(LEVEL_1_ID);
        WarmGameUiControlConfig warmGameUiControlConfig = gameUiControlConfigPersistence.loadWarm(locale, userContext);
        Assert.assertEquals(PLANET_1_ID, warmGameUiControlConfig.getPlanetConfig().getPlanetId());

        userContext = new UserContext().setLevelId(LEVEL_2_ID);
        warmGameUiControlConfig = gameUiControlConfigPersistence.loadWarm(locale, userContext);
        Assert.assertEquals(PLANET_1_ID, warmGameUiControlConfig.getPlanetConfig().getPlanetId());

        userContext = new UserContext().setLevelId(LEVEL_3_ID);
        warmGameUiControlConfig = gameUiControlConfigPersistence.loadWarm(locale, userContext);
        Assert.assertEquals(PLANET_1_ID, warmGameUiControlConfig.getPlanetConfig().getPlanetId());

        userContext = new UserContext().setLevelId(LEVEL_4_ID);
        warmGameUiControlConfig = gameUiControlConfigPersistence.loadWarm(locale, userContext);
        Assert.assertEquals(PLANET_2_ID, warmGameUiControlConfig.getPlanetConfig().getPlanetId());

        cleanPlanets();
    }
}