package com.btxtech.uiservice.itemplacer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.uiservice.renderer.task.itemplacer.BaseItemPlacerRenderTask;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

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
    private BaseItemPlacer baseItemPlacer;
    private Consumer<Collection<DecimalPosition>> executionCallback;
    private Collection<BaseItemPlacerListener> listeners = new ArrayList<>();

    public void activate(BaseItemPlacerConfig baseItemPlacerConfig, Consumer<Collection<DecimalPosition>> executionCallback) {
        this.executionCallback = executionCallback;




//    TODO    if (baseItemPlacerConfig.getSuggestedPosition() != null) {
//    TODO        terrainScrollHandler.moveToMiddle(startPointInfo.getSuggestedPosition());
//    TODO    }
//    TODO CursorHandler.getInstance().noCursor();
//    TODO TerrainView.getInstance().setFocus();
        baseItemPlacer = instance.get().init(baseItemPlacerConfig);
        baseItemPlacerRenderTask.activate(baseItemPlacer);
        // TODO RadarPanel.getInstance().setLevelRadarMode(RadarMode.MAP_AND_UNITS);
        // TODO ClientDeadEndProtection.getInstance().stop();

        new ArrayList<>(listeners).forEach(baseItemPlacerListener -> baseItemPlacerListener.onStateChanged(baseItemPlacer));
    }

    public void deactivate() {
        baseItemPlacer = null;
        baseItemPlacerRenderTask.deactivate();
        // TODO RadarPanel.getInstance().setLevelRadarMode(ClientPlanetServices.getInstance().getPlanetInfo().getRadarMode());
        // TODO ClientDeadEndProtection.getInstance().start();
        new ArrayList<>(listeners).forEach(baseItemPlacerListener -> baseItemPlacerListener.onStateChanged(null));
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
            executionCallback.accept(baseItemPlacer.setupAbsolutePositions());
            deactivate();
        }
    }

    public void onMouseMoveEvent(DecimalPosition terrainPosition) {
        if (!isActive()) {
            return;
        }
        baseItemPlacer.onMove(terrainPosition);
    }

    public BaseItemPlacer getBaseItemPlacer() {
        return baseItemPlacer;
    }

    public void addListener(BaseItemPlacerListener baseItemPlacerListener) {
        listeners.add(baseItemPlacerListener);
    }

    public void removeListener(BaseItemPlacerListener baseItemPlacerListener) {
        listeners.remove(baseItemPlacerListener);
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
