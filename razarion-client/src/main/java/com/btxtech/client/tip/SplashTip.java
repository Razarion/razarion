package com.btxtech.client.tip;

import com.btxtech.uiservice.tip.visualization.SplashTipVisualization;
import org.jboss.errai.ui.shared.api.annotations.Templated;

/**
 * Created by Beat
 * 14.12.2016.
 */
@Templated("GuiTip.html#tip")
public class SplashTip extends AbstractGuiTip {

    public void init(SplashTipVisualization splashTipVisualization) {
        super.init(splashTipVisualization, "tip-splash-animation-container", "tip-splash-animation-image");
        setVisible(splashTipVisualization.isVisible());
        splashTipVisualization.setVisibilityCallback(this::setVisible);
    }

    private void setVisible(boolean visible) {
        getImage().getStyle().setProperty("visibility", visible ? "visible" : "hidden");
    }

}
