package com.btxtech.uiservice.itemplacer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.renderer.task.itemplacer.BaseItemPlacerRenderTask;
import com.btxtech.uiservice.storyboard.StoryboardService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 30.10.2016.
 */
@ApplicationScoped
public class BaseItemPlacerService {
    @Inject
    private Instance<BaseItemPlacer> instance;
    @Inject
    private BaseItemPlacerRenderTask baseItemPlacerRenderTask;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private StoryboardService storyboardService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    private BaseItemPlacer baseItemPlacer;

    public void activate(BaseItemPlacerConfig baseItemPlacerConfig) {
//    TODO    if (baseItemPlacerConfig.getSuggestedPosition() != null) {
//    TODO        terrainScrollHandler.moveToMiddle(startPointInfo.getSuggestedPosition());
//    TODO    }
//    TODO CursorHandler.getInstance().noCursor();
//    TODO TerrainView.getInstance().setFocus();
        baseItemPlacer = instance.get().init(baseItemPlacerConfig);
        baseItemPlacerRenderTask.activate(baseItemPlacer);
        // TODO RadarPanel.getInstance().setLevelRadarMode(RadarMode.MAP_AND_UNITS);
        // TODO ClientDeadEndProtection.getInstance().stop();
    }

    public void deactivate() {
        baseItemPlacer = null;
        baseItemPlacerRenderTask.deactivate();
        // TODO RadarPanel.getInstance().setLevelRadarMode(ClientPlanetServices.getInstance().getPlanetInfo().getRadarMode());
        // TODO ClientDeadEndProtection.getInstance().start();
    }

    public boolean isActive() {
        return baseItemPlacer != null;
    }

    public void onMouseDownEvent(DecimalPosition terrainPosition) {
        if (!isActive()) {
            return;
        }
        baseItemPlacer.onMove(terrainPosition);
        if (baseItemPlacer.isPositionValid()) {
            PlayerBase playerBase = baseItemService.createHumanBase(storyboardService.getUserContext());
            try {
                for (DecimalPosition position : baseItemPlacer.setupAbsolutePositions()) {
                    baseItemService.spawnSyncBaseItem(baseItemPlacer.getBaseItemType(), position, playerBase, false);
                }
                deactivate();
            } catch (Exception e) {
                exceptionHandler.handleException(e);
            }
        }
    }

    public void onMouseMoveEvent(DecimalPosition terrainPosition) {
        if (!isActive()) {
            return;
        }
        baseItemPlacer.onMove(terrainPosition);
    }

    //   TODO public void onBaseLost(BaseLostPacket baseLostPacket) {
//   TODO     if (isActive()) {
//   TODO         log.warning("StartPointMode.onBaseLost() is already active");
//   TODO     }
//   TODO
//   TODO     RealDeltaStartupTask.setCommon(baseLostPacket.getRealGameInfo());
//   TODO     ClientBase.getInstance().recalculateOnwItems();
//   TODO     SideCockpit.getInstance().updateItemLimit();
//   TODO     activate(baseLostPacket.getRealGameInfo().getStartPointInfo());
//    }

}
