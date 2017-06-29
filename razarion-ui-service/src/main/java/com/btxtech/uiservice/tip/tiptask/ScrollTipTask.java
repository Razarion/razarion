package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.renderer.ViewService;
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
public class ScrollTipTask extends AbstractTipTask implements ViewService.ViewFieldListener {
    private static final long SCROLL_DELAY = 3000;
    private static final long TIMER_DELAY = 1000;
    @Inject
    private ViewService viewService;
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
        viewService.addViewFieldListeners(this);
        lastScrollTimestamp = System.currentTimeMillis();
        startTimer();
    }

    @Override
    protected void internalCleanup() {
        viewService.removeViewFieldListeners(this);
        stopTimer();
    }

    @Override
    public void onViewChanged(ViewField viewField, Rectangle2D absAabbRect) {
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
        inGameDirectionVisualization = new InGameDirectionVisualization(getGameTipVisualConfig().getDirectionShape3DId(), terrainPositionHint, !splashVisible, getNativeMatrixFactory());
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
