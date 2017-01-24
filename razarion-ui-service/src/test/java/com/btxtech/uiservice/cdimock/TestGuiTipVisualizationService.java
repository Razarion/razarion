package com.btxtech.uiservice.cdimock;

import com.btxtech.uiservice.tip.visualization.AbstractGuiTipVisualization;
import com.btxtech.uiservice.tip.visualization.GuiTipVisualizationService;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 24.01.2017.
 */
@ApplicationScoped
public class TestGuiTipVisualizationService implements GuiTipVisualizationService {
    @Override
    public void activate(AbstractGuiTipVisualization guiTipVisualization) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deactivate() {
        throw new UnsupportedOperationException();
    }
}
