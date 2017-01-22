package com.btxtech.scenariongui.scenario;

import com.btxtech.persistence.GameUiControlProviderEmulator;
import com.btxtech.shared.dto.GameUiControlConfig;
import com.btxtech.shared.gameengine.GameEngineInitEvent;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.planet.PlanetActivationEvent;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

/**
 * Created by Beat
 * 22.01.2017.
 */
public abstract class AbstractTerrainScenario extends Scenario {
    private WeldContainer weldContainer;

    @Override
    public void init() {
        GameUiControlProviderEmulator gameUiControlProviderEmulator = new GameUiControlProviderEmulator();
        GameUiControlConfig gameUiControlConfig = gameUiControlProviderEmulator.readFromFile();
        Weld weld = new Weld();
        weldContainer = weld.initialize();
        TerrainTypeService terrainTypeService = weldContainer.instance().select(TerrainTypeService.class).get();
        terrainTypeService.onGameEngineInit(new GameEngineInitEvent(gameUiControlConfig.getGameEngineConfig()));
        TerrainService terrainService = weldContainer.instance().select(TerrainService.class).get();
        terrainService.onPlanetActivation(new PlanetActivationEvent(gameUiControlConfig.getGameEngineConfig().getPlanetConfig()));
    }

    protected TerrainService getTerrainService() {
        return getBean(TerrainService.class);
    }

    protected <T> T getBean(Class<T> theClass) {
        return weldContainer.instance().select(theClass).get();
    }
}
