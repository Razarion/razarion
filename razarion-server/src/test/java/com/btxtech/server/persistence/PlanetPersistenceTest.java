package com.btxtech.server.persistence;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.PlanetVisualConfig;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import javax.inject.Inject;
import java.util.Locale;

/**
 * Created by Beat
 * 25.05.2017.
 */
public class PlanetPersistenceTest extends ArquillianBaseTest {
    @Inject
    private PlanetPersistence planetPersistence;
    @Inject
    private GameUiControlConfigPersistence gameUiControlConfigPersistence;

    @Test
    public void loadStaticGameConfig() throws Exception {
        setupPlanets();

        PlanetVisualConfig expectedPlanetVisualConfig = new PlanetVisualConfig().setShadowAlpha(0.2).setShadowRotationX(0.33).setShadowRotationY(-0.45);
        expectedPlanetVisualConfig.setShape3DLightRotateX(-0.23).setShape3DLightRotateY(-0.77);
        planetPersistence.updatePlanetVisualConfig(PLANET_1_ID, expectedPlanetVisualConfig);

        PlanetVisualConfig actualPlanetVisualConfig = gameUiControlConfigPersistence.loadWarm(Locale.ENGLISH, new UserContext().setLevelId(LEVEL_1_ID)).getPlanetVisualConfig();

        ReflectionAssert.assertReflectionEquals(expectedPlanetVisualConfig, actualPlanetVisualConfig);

        cleanPlanets();
    }


}