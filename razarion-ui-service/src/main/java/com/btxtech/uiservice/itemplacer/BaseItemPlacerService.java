package com.btxtech.uiservice.itemplacer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BaseItemPlacerConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Provider;
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
    private Provider<BaseItemPlacer> instance;
    private BaseItemPlacer baseItemPlacer;
    private BaseItemPlacerPresenter baseItemPlacerPresenter;
    private Consumer<Collection<DecimalPosition>> executionCallback;
    private final Collection<BaseItemPlacerListener> listeners = new ArrayList<>();

    public void init(BaseItemPlacerPresenter baseItemPlacerPresenter) {
        this.baseItemPlacerPresenter = baseItemPlacerPresenter;
    }

    public void activate(BaseItemPlacerConfig baseItemPlacerConfig, boolean canBeCanceled, Consumer<Collection<DecimalPosition>> executionCallback) {
        this.executionCallback = executionCallback;
        baseItemPlacer = instance.get().init(baseItemPlacerConfig, canBeCanceled, this::onPlace);
        baseItemPlacerPresenter.activate(baseItemPlacer);
        new ArrayList<>(listeners).forEach(baseItemPlacerListener -> baseItemPlacerListener.activatePlacer(baseItemPlacer));
    }

    public void deactivate() {
        deactivateInternal(true);
    }

    public boolean isActive() {
        return baseItemPlacer != null;
    }

    public void onPlace(DecimalPosition terrainPosition) {
        if (baseItemPlacer.isPositionValid()) {
            executionCallback.accept(baseItemPlacer.setupAbsolutePositions(terrainPosition));
            deactivateInternal(false);
        }
    }

    private void deactivateInternal(boolean canceled) {
        baseItemPlacer = null;
        baseItemPlacerPresenter.deactivate();
        new ArrayList<>(listeners).forEach(baseItemPlacerListener -> baseItemPlacerListener.deactivatePlacer(canceled));
    }
}
