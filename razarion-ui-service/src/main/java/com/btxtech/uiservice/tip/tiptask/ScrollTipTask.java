package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.btxtech.uiservice.terrain.TerrainScrollListener;
import com.btxtech.uiservice.tip.visualization.AbstractGuiTipVisualization;
import com.btxtech.uiservice.tip.visualization.InGameDirectionVisualization;
import com.btxtech.uiservice.tip.visualization.SplashTipVisualization;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 16.12.2016.
 */
@Dependent
public class ScrollTipTask extends AbstractTipTask implements TerrainScrollListener {
    private static final long SCROLL_DELAY = 3000;
    private static final long TIMER_DELAY = 1000;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private SimpleExecutorService simpleExecutorService;
    private SimpleScheduledFuture simpleScheduledFuture;
    private DecimalPosition terrainPositionHint;
    private long lastScrollTimestamp;
    private SplashTipVisualization splashTipVisualization;
    private InGameDirectionVisualization inGameDirectionVisualization;
    private boolean splashVisible;

    public void init(DecimalPosition terrainPositionHint) {
        this.terrainPositionHint = terrainPositionHint;
    }

    @Override
    public boolean isFulfilled() {
        return false;
    }

    @Override
    protected void internalStart() {
        terrainScrollHandler.addTerrainScrollListener(this);
        lastScrollTimestamp = System.currentTimeMillis();
        startTimer();
    }

    @Override
    protected void internalCleanup() {
        terrainScrollHandler.removeTerrainScrollListener(this);
        stopTimer();
    }

    @Override
    public void onScroll(ViewField viewField) {
        lastScrollTimestamp = System.currentTimeMillis();
        setSplashVisible(false);
        startTimer();
    }

    private void onTimer() {
        if (lastScrollTimestamp + SCROLL_DELAY < System.currentTimeMillis()) {
            stopTimer();
            setSplashVisible(true);
        }
    }

    @Override
    public InGameDirectionVisualization createInGameDirectionVisualization() {
        inGameDirectionVisualization = new InGameDirectionVisualization(getGameTipVisualConfig().getDirectionShape3DId(), terrainPositionHint, !splashVisible);
        return inGameDirectionVisualization;
    }

    @Override
    public AbstractGuiTipVisualization createGuiTipVisualization() {
        splashTipVisualization = new SplashTipVisualization(getGameTipVisualConfig().getSplashScrollImageId(), splashVisible);
        return splashTipVisualization;
    }

    private void startTimer() {
        if (simpleScheduledFuture == null) {
            simpleScheduledFuture = simpleExecutorService.scheduleAtFixedRate(TIMER_DELAY, true, this::onTimer, SimpleExecutorService.Type.UNSPECIFIED);
        }
    }

    private void stopTimer() {
        if (simpleScheduledFuture != null) {
            simpleScheduledFuture.cancel();
            simpleScheduledFuture = null;
        }
    }

    private void setSplashVisible(boolean splashVisible) {
        if (this.splashVisible == splashVisible) {
            return;
        }
        this.splashVisible = splashVisible;
        splashTipVisualization.setVisible(splashVisible);
        inGameDirectionVisualization.setVisible(!splashVisible);
    }

}
