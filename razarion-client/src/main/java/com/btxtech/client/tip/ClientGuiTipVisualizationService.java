package com.btxtech.client.tip;

import com.btxtech.uiservice.tip.visualization.GuiTipVisualization;
import com.btxtech.uiservice.tip.visualization.GuiTipVisualizationService;
import org.jboss.errai.common.client.dom.Window;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 14.12.2016.
 */
@ApplicationScoped
public class ClientGuiTipVisualizationService implements GuiTipVisualizationService {
    @Inject
    private Instance<HorizontalGuiTip> instance;
    private HorizontalGuiTip current;

    @Override
    public void activate(GuiTipVisualization guiTipVisualization) {
        deactivate();
        current = instance.get();
        current.init(guiTipVisualization);
        Window.getDocument().getBody().appendChild(current.getElement());
    }

    @Override
    public void deactivate() {
        if (current != null) {
            current.cleanup();
            Window.getDocument().getBody().removeChild(current.getElement());
            current = null;
        }
    }

}
