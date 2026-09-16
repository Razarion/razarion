package com.btxtech.uiservice.itemplacer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BaseItemPlacerConfig;

import jakarta.inject.Singleton;
import jakarta.inject.Provider;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * Created by Beat
 * 30.10.2016.
 */
@Singleton
public class BaseItemPlacerService {

    private Provider<BaseItemPlacer> provider;
    private BaseItemPlacer baseItemPlacer;
    private BaseItemPlacerPresenter baseItemPlacerPresenter;
    private BiConsumer<Collection<DecimalPosition>, DecimalPosition> executionCallback;
    private final Collection<BaseItemPlacerListener> listeners = new ArrayList<>();

    @Inject
    public BaseItemPlacerService(Provider<BaseItemPlacer> provider) {
        this.provider = provider;
    }

    public void init(BaseItemPlacerPresenter baseItemPlacerPresenter) {
        this.baseItemPlacerPresenter = baseItemPlacerPresenter;
    }

    public void activate(BaseItemPlacerConfig baseItemPlacerConfig, boolean canBeCanceled, BiConsumer<Collection<DecimalPosition>, DecimalPosition> executionCallback) {
        if (isActive()) {
            deactivateInternal(true);
        }
        this.executionCallback = executionCallback;
        baseItemPlacer = provider.get().init(baseItemPlacerConfig, canBeCanceled, this::onPlace, this::deactivate);
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
            executionCallback.accept(baseItemPlacer.setupAbsolutePositions(terrainPosition), baseItemPlacer.getAbsoluteRallyPosition(terrainPosition));
            deactivateInternal(false);
            return;
        }
        /*
         * The authoritative check refused after the presenter had already committed - the deploy
         * bubble is gone, the placement is reported as confirmed, and nothing is built. This used
         * to return in silence, which is how it stayed invisible: no building, no message, no log,
         * and the build tip waiting for a construction site that was never ordered.
         *
         * The presenter now re-checks at the exact deploy position immediately before committing,
         * so this should no longer be reachable. Saying so out loud is the point: if it happens
         * again, the reason is in the log instead of in a player's memory.
         */
        baseItemPlacer.onInvalidPlaceAttempt();
    }

    private void deactivateInternal(boolean canceled) {
        baseItemPlacer = null;
        baseItemPlacerPresenter.deactivate();
        new ArrayList<>(listeners).forEach(baseItemPlacerListener -> baseItemPlacerListener.deactivatePlacer(canceled));
    }
}
