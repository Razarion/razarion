package com.btxtech.webglemulator.razarion;

import com.btxtech.uiservice.tip.visualization.GuiTipVisualization;
import com.btxtech.uiservice.tip.visualization.GuiTipVisualizationService;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 14.12.2016.
 */
@ApplicationScoped
public class DevToolGuiTipVisualizationService implements GuiTipVisualizationService {
    @Override
    public void activate(GuiTipVisualization guiTipVisualization) {
        System.out.println("+++++++ DevToolGuiTipVisualizationService.activate()");
    }

    @Override
    public void deactivate() {
        System.out.println("+++++++ DevToolGuiTipVisualizationService.deactivate()");
    }
}
