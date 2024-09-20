package com.btxtech.server.persistence;

import com.btxtech.server.IgnoreOldArquillianTest;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.PlanetVisualConfig;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Locale;

/**
 * Created by Beat
 * 25.05.2017.
 */
@Ignore
public class PlanetCrudPersistenceTestRest extends IgnoreOldArquillianTest {
    @Inject
    private PlanetCrudPersistence planetCrudPersistence;
    @Inject
    private GameUiContextCrudPersistence gameUiContextCrudPersistence;

    @Test
    public void loadStaticGameConfig() throws Exception {
        setupPlanetDb();

        PlanetVisualConfig expectedPlanetVisualConfig = new PlanetVisualConfig().setShadowAlpha(0.2);
        expectedPlanetVisualConfig.setLightDirection(new Vertex(0, 0, -1));
        planetCrudPersistence.updatePlanetVisualConfig(PLANET_1_ID, expectedPlanetVisualConfig);

        PlanetVisualConfig actualPlanetVisualConfig = gameUiContextCrudPersistence.loadWarm(Locale.ENGLISH, new UserContext().levelId(LEVEL_1_ID)).getPlanetVisualConfig();

        // ReflectionAssert.assertReflectionEquals(expectedPlanetVisualConfig, actualPlanetVisualConfig);

        cleanPlanets();
    }


}