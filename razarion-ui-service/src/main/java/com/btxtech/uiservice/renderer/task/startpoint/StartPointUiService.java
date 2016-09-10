package com.btxtech.uiservice.renderer.task.startpoint;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Ray3d;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.StartPointConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.mouse.TerrainMouseDownEvent;
import com.btxtech.uiservice.mouse.TerrainMouseMoveEvent;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderOrder;
import com.btxtech.uiservice.storyboard.StoryboardService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
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
//    TODO    if (startPointConfig.getSuggestedPosition() != null) {
//    TODO        terrainScrollHandler.moveToMiddle(startPointInfo.getSuggestedPosition());
//    TODO    }
        startPointItemPlacer = instance.get().init(startPointConfig);
        ModelRenderer<StartPointItemPlacer, CommonRenderComposite<AbstractStartPointRendererUnit, StartPointItemPlacer>, AbstractStartPointRendererUnit, StartPointItemPlacer> modelRenderer = create();
        modelRenderer.init(startPointItemPlacer, startPointItemPlacer::provideModelMatrices);
        CommonRenderComposite<AbstractStartPointRendererUnit, StartPointItemPlacer> compositeRenderer = modelRenderer.create();
        compositeRenderer.init(startPointItemPlacer);
        compositeRenderer.setRenderUnit(AbstractStartPointRendererUnit.class);
        modelRenderer.add(RenderOrder.START_POINT, compositeRenderer);
        add(modelRenderer);
        compositeRenderer.fillBuffers();

        // TODO RadarPanel.getInstance().setLevelRadarMode(RadarMode.MAP_AND_UNITS);
        // TODO ClientDeadEndProtection.getInstance().stop();
    }

    public void deactivate() {
        startPointItemPlacer = null;
        // TODO RadarPanel.getInstance().setLevelRadarMode(ClientPlanetServices.getInstance().getPlanetInfo().getRadarMode());
        // TODO ClientDeadEndProtection.getInstance().start();
    }

    public void onMouseDownEvent(@Observes TerrainMouseDownEvent terrainMouseDownEvent) {
        if (!isActive()) {
            return;
        }
        startPointItemPlacer.onMove(terrainMouseDownEvent.getTerrainPosition());
        if (startPointItemPlacer.isPositionValid()) {
            PlayerBase playerBase = baseItemService.createHumanBase(storyboardService.getUserContext());
            try {
                baseItemService.spawnSyncBaseItem(startPointItemPlacer.getBaseItemType(), terrainMouseDownEvent.getTerrainPosition(), playerBase);
                deactivate();
            } catch (Exception e) {
                exceptionHandler.handleException(e);
            }
        }
    }

    public void onMouseMoveEvent(@Observes TerrainMouseMoveEvent terrainMouseMoveEvent) {
        if (!isActive()) {
            return;
        }
        startPointItemPlacer.onMove(terrainMouseMoveEvent.getTerrainPosition());
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
