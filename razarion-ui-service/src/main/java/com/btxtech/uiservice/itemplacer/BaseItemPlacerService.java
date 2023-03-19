package com.btxtech.uiservice.itemplacer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.uiservice.mouse.CursorService;

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
    private CursorService cursorService;
    private boolean canBeCanceled;
    private BaseItemPlacer baseItemPlacer;
    private Consumer<Collection<DecimalPosition>> executionCallback;
    private Collection<BaseItemPlacerListener> listeners = new ArrayList<>();

    public void activate(BaseItemPlacerConfig baseItemPlacerConfig, boolean canBeCanceled, Consumer<Collection<DecimalPosition>> executionCallback) {
        cursorService.handleItemPlaceActivated();
        this.canBeCanceled = canBeCanceled;
        this.executionCallback = executionCallback;
        baseItemPlacer = instance.get().init(baseItemPlacerConfig);
        // TODO baseItemPlacerRenderTask.activate(baseItemPlacer);
        new ArrayList<>(listeners).forEach(baseItemPlacerListener -> baseItemPlacerListener.activatePlacer(baseItemPlacer));
    }

    public void deactivate() {
        deactivateInternal(true);
    }

    public void deactivateFriendly() {
        if (canBeCanceled) {
            deactivateInternal(true);
        }
    }

    public boolean isActive() {
        return baseItemPlacer != null;
    }

    public void onMouseUpEvent(DecimalPosition terrainPosition) {
        if (!isActive()) {
            return;
        }
        baseItemPlacer.onMove(terrainPosition);
        if (baseItemPlacer.isPositionValid()) {
            executionCallback.accept(baseItemPlacer.setupAbsolutePositions());
            deactivateInternal(false);
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

    private void deactivateInternal(boolean canceled) {
        baseItemPlacer = null;
        // TODO baseItemPlacerRenderTask.deactivate();
        new ArrayList<>(listeners).forEach(baseItemPlacerListener -> baseItemPlacerListener.deactivatePlacer(canceled));
    }
}
