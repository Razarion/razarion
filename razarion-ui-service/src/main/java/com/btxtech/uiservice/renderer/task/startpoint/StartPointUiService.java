package com.btxtech.uiservice.renderer.task.startpoint;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.StartPointConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.storyboard.StoryboardService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 05.09.2016.
 */
@ApplicationScoped
public class StartPointUiService extends AbstractRenderTask<StartPointItemPlacer> {
    // private Logger log = Logger.getLogger(StartPointUiService.class.getName());
    @Inject
    private Instance<StartPointItemPlacer> instance;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @Inject
    private BaseItemService baseItemService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private StoryboardService storyboardService;
    private StartPointItemPlacer startPointItemPlacer;

    @Override
    protected boolean isActive() {
        return startPointItemPlacer != null;
    }

    public void activate(StartPointConfig startPointConfig) {
        if (startPointConfig.getSuggestedPosition() != null) {
            // TODO terrainScrollHandler.moveToMiddle(startPointInfo.getSuggestedPosition());
        }
        startPointItemPlacer = instance.get().init(startPointConfig);
        ModelRenderer<StartPointItemPlacer, CommonRenderComposite<AbstractStartPointRendererUnit, StartPointItemPlacer>, AbstractStartPointRendererUnit, StartPointItemPlacer> modelRenderer = create();
        CommonRenderComposite<AbstractStartPointRendererUnit, StartPointItemPlacer> compositeRenderer = modelRenderer.create();
        compositeRenderer.init(startPointItemPlacer);
        compositeRenderer.setRenderUnit(AbstractStartPointRendererUnit.class);
        // TODO compositeRenderer.fillBuffers();
        add(modelRenderer);

        // TODO RadarPanel.getInstance().setLevelRadarMode(RadarMode.MAP_AND_UNITS);
        // TODO ClientDeadEndProtection.getInstance().stop();
    }

    public void deactivate() {
        startPointItemPlacer = null;
        // TODO RadarPanel.getInstance().setLevelRadarMode(ClientPlanetServices.getInstance().getPlanetInfo().getRadarMode());
        // TODO ClientDeadEndProtection.getInstance().start();
    }

    public StartPointItemPlacer getStartPointPlacer() {
        return startPointItemPlacer;
    }

    public void execute(DecimalPosition position) {
        startPointItemPlacer.onMove(position);
        if (startPointItemPlacer.isPositionValid()) {
            PlayerBase playerBase = baseItemService.createHumanBase(storyboardService.getUserContext().getName());
            try {
                baseItemService.spawnSyncBaseItem(startPointItemPlacer.getBaseItemType(), position, playerBase);
            } catch (Exception e) {
                exceptionHandler.handleException(e);
            }
            deactivate();
        }
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
