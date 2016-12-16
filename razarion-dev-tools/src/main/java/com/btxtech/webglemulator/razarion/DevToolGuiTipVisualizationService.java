package com.btxtech.webglemulator.razarion;

import com.btxtech.uiservice.tip.visualization.AbstractGuiTipVisualization;
import com.btxtech.uiservice.tip.visualization.GuiTipVisualizationService;
import com.btxtech.uiservice.tip.visualization.SplashTipVisualization;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 14.12.2016.
 */
@ApplicationScoped
public class DevToolGuiTipVisualizationService implements GuiTipVisualizationService {
    @Override
    public void activate(AbstractGuiTipVisualization guiTipVisualization) {
        System.out.println("+++++++ DevToolGuiTipVisualizationService.activate(): " + guiTipVisualization);
        if (guiTipVisualization instanceof SplashTipVisualization) {
            SplashTipVisualization splashTipVisualization = (SplashTipVisualization) guiTipVisualization;
            setSplashVisible(splashTipVisualization.isVisible());
            splashTipVisualization.setVisibilityCallback(this::setSplashVisible);
        }
    }

    @Override
    public void deactivate() {
        System.out.println("+++++++ DevToolGuiTipVisualizationService.deactivate()");
    }

    private void setSplashVisible(boolean visible) {
        System.out.println("+++++ SplashTipVisualization: " + visible);
    }
}
