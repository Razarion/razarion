package com.btxtech.uiservice.tip.visualization;

import java.util.function.Consumer;

/**
 * Created by Beat
 * 16.12.2016.
 */
public class SplashTipVisualization extends AbstractGuiTipVisualization {
    private Consumer<Boolean> visibilityCallback;
    private boolean visible;

    public SplashTipVisualization(Integer imageId, boolean visible) {
        super(imageId);
        this.visible = visible;
    }

    public void setVisibilityCallback(Consumer<Boolean> visibilityCallback) {
        this.visibilityCallback = visibilityCallback;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        visibilityCallback.accept(visible);
    }

    public boolean isVisible() {
        return visible;
    }
}
