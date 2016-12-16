package com.btxtech.client.tip;

import com.btxtech.uiservice.tip.visualization.AbstractGuiTipVisualization;
import com.btxtech.uiservice.tip.visualization.GuiPointingTipVisualization;
import com.btxtech.uiservice.tip.visualization.GuiTipVisualizationService;
import com.btxtech.uiservice.tip.visualization.SplashTipVisualization;
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
    public void activate(AbstractGuiTipVisualization guiTipVisualization) {
        deactivate();

        if (guiTipVisualization instanceof GuiPointingTipVisualization) {
            GuiPointingTipVisualization guiPointingTipVisualization = (GuiPointingTipVisualization) guiTipVisualization;
            AbstractPointingGuiTip abstractPointingGuiTip;
            switch (guiPointingTipVisualization.getDirection()) {
                case NORTH:
                    throw new UnsupportedOperationException();
                case EAST:
                    throw new UnsupportedOperationException();
                case SOUTH:
                    abstractPointingGuiTip = instance.select(SouthGuiTip.class).get();
                    break;
                case WEST:
                    abstractPointingGuiTip = instance.select(WestGuiTip.class).get();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown direction: " + guiPointingTipVisualization.getDirection());
            }

            abstractPointingGuiTip.init(guiPointingTipVisualization);
            current = abstractPointingGuiTip;
        } else if (guiTipVisualization instanceof SplashTipVisualization) {
            SplashTip splashTip = instance.select(SplashTip.class).get();
            splashTip.init((SplashTipVisualization) guiTipVisualization);
            current = splashTip;
        } else {
            throw new IllegalArgumentException("Unknown AbstractGuiTipVisualization: " + guiTipVisualization);
        }
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
