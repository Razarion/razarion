package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.persistence.JsonProviderEmulator;
import com.btxtech.shared.gameengine.StaticGameInitEvent;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
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
        JsonProviderEmulator jsonProviderEmulator = new JsonProviderEmulator();
        StaticGameConfig staticGameConfig = jsonProviderEmulator.readFromFile(false).getStaticGameConfig();
        // StaticGameConfig staticGameConfig = jsonProviderEmulator.readGameEngineConfigFromFile("C:\\dev\\projects\\razarion\\code\\tmp\\TmpGameUiControlConfig.json");
        Weld weld = new Weld();
        weldContainer = weld.initialize();
        TerrainTypeService terrainTypeService = weldContainer.instance().select(TerrainTypeService.class).get();
        terrainTypeService.onGameEngineInit(new StaticGameInitEvent(staticGameConfig));
        TerrainService terrainService = weldContainer.instance().select(TerrainService.class).get();
        // TODO terrainService.setup(staticGameConfig.getPlanetConfig());
    }

    protected TerrainService getTerrainService() {
        return getBean(TerrainService.class);
    }

    protected <T> T getBean(Class<T> theClass) {
        return weldContainer.instance().select(theClass).get();
    }

    protected void drawObstacle(ExtendedGraphicsContext extendedGraphicsContext) {
//        ObstacleContainer obstacleContainer = getBean(ObstacleContainer.class);
//
//        for (int x = 0; x < obstacleContainer.getXCount(); x++) {
//            for (int y = 0; y < obstacleContainer.getYCount(); y++) {
//                Index index = new Index(x, y);
//                ObstacleContainerNode obstacleContainerNode = obstacleContainer.getObstacleContainerNode(index);
//                if (obstacleContainerNode != null && obstacleContainerNode.getObstacles() != null) {
//                    for (Obstacle obstacle : obstacleContainerNode.getObstacles()) {
//                        extendedGraphicsContext.drawObstacle(obstacle, Color.BROWN, Color.BROWN);
//                    }
//                }
//            }
//        }
    }
}
