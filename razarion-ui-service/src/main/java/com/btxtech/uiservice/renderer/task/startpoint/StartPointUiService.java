package com.btxtech.uiservice.renderer.task.startpoint;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.dto.StartPointConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.mouse.TerrainMouseDownEvent;
import com.btxtech.uiservice.mouse.TerrainMouseMoveEvent;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.AbstractVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderUnitControl;
import com.btxtech.uiservice.storyboard.StoryboardService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 05.09.2016.
 */
@ApplicationScoped
public class StartPointUiService extends AbstractRenderTask<StartPointItemPlacer> {
    private Logger logger = Logger.getLogger(StartPointUiService.class.getName());
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
    @Inject
    private Shape3DUiService shape3DUiService;
    private StartPointItemPlacer startPointItemPlacer;

    @Override
    protected boolean isActive() {
        return startPointItemPlacer != null;
    }

    public void activate(StartPointConfig startPointConfig) {
//    TODO    if (startPointConfig.getSuggestedPosition() != null) {
//    TODO        terrainScrollHandler.moveToMiddle(startPointInfo.getSuggestedPosition());
//    TODO    }
        StartPointItemPlacer startPointItemPlacer = instance.get().init(startPointConfig);
        setupCircle(startPointItemPlacer);
        setupItem(startPointItemPlacer);
        this.startPointItemPlacer = startPointItemPlacer;
        // TODO RadarPanel.getInstance().setLevelRadarMode(RadarMode.MAP_AND_UNITS);
        // TODO ClientDeadEndProtection.getInstance().stop();
    }

    private void setupCircle(StartPointItemPlacer startPointItemPlacer) {
        ModelRenderer<StartPointItemPlacer, CommonRenderComposite<AbstractStartPointCircleRendererUnit, StartPointItemPlacer>, AbstractStartPointCircleRendererUnit, StartPointItemPlacer> modelRenderer = create();
        modelRenderer.init(startPointItemPlacer, startPointItemPlacer::provideCircleModelMatrices);
        CommonRenderComposite<AbstractStartPointCircleRendererUnit, StartPointItemPlacer> compositeRenderer = modelRenderer.create();
        compositeRenderer.init(startPointItemPlacer);
        compositeRenderer.setRenderUnit(AbstractStartPointCircleRendererUnit.class);
        modelRenderer.add(RenderUnitControl.START_POINT_CIRCLE, compositeRenderer);
        add(modelRenderer);
        compositeRenderer.fillBuffers();
    }

    private void setupItem(StartPointItemPlacer startPointItemPlacer) {
        if (startPointItemPlacer.getBaseItemType().getShape3DId() == null) {
            logger.warning("StartPointUiService: no shape3DId for BaseItemType: " + startPointItemPlacer.getBaseItemType());
            return;
        }

        ModelRenderer<BaseItemType, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
        modelRenderer.init(startPointItemPlacer.getBaseItemType(), startPointItemPlacer::provideItemModelMatrices);

        Shape3D shape3D = shape3DUiService.getShape3D(startPointItemPlacer.getBaseItemType().getShape3DId());
        for (Element3D element3D : shape3D.getElement3Ds()) {
            for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer> compositeRenderer = modelRenderer.create();
                compositeRenderer.init(vertexContainer);
                compositeRenderer.setRenderUnit(AbstractVertexContainerRenderUnit.class);
                compositeRenderer.setupAnimation(shape3D, element3D, vertexContainer.getShapeTransform());
                compositeRenderer.setDepthBufferRenderUnit(AbstractVertexContainerRenderUnit.class);
                compositeRenderer.setNormRenderUnit(AbstractVertexContainerRenderUnit.class);
                modelRenderer.add(RenderUnitControl.START_POINT_ITEM, compositeRenderer);
                compositeRenderer.fillBuffers();
            }
        }
        add(modelRenderer);
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
