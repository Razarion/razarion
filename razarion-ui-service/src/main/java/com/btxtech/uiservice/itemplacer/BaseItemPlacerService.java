package com.btxtech.uiservice.itemplacer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BaseItemPlacerConfig;

import jakarta.inject.Singleton;
import jakarta.inject.Provider;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 30.10.2016.
 */
@Singleton
public class BaseItemPlacerService {

    private Provider<BaseItemPlacer> provider;
    private BaseItemPlacer baseItemPlacer;
    private BaseItemPlacerPresenter baseItemPlacerPresenter;
    private Consumer<Collection<DecimalPosition>> executionCallback;
    private final Collection<BaseItemPlacerListener> listeners = new ArrayList<>();

    @Inject
    public BaseItemPlacerService(Provider<BaseItemPlacer> provider) {
        this.provider = provider;
    }

    public void init(BaseItemPlacerPresenter baseItemPlacerPresenter) {
        this.baseItemPlacerPresenter = baseItemPlacerPresenter;
    }

    public void activate(BaseItemPlacerConfig baseItemPlacerConfig, boolean canBeCanceled, Consumer<Collection<DecimalPosition>> executionCallback) {
        this.executionCallback = executionCallback;
        baseItemPlacer = provider.get().init(baseItemPlacerConfig, canBeCanceled, this::onPlace);
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
