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
    private Instance<AbstractGuiTip> instance;
    private AbstractGuiTip current;

    @Override
    public void activate(GuiTipVisualization guiTipVisualization) {
        deactivate();
        switch (guiTipVisualization.getDirection()) {
            case NORTH:
                throw new UnsupportedOperationException();
            case EAST:
                throw new UnsupportedOperationException();
            case SOUTH:
                current = instance.select(SouthGuiTip.class).get();
                break;
            case WEST:
                current = instance.select(WestGuiTip.class).get();
                break;
            default:
                throw new IllegalArgumentException("Unknown direction: " + guiTipVisualization.getDirection());
        }

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
