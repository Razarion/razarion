package com.btxtech.client.tip;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.uiservice.tip.visualization.GuiPointingTipVisualization;
import com.google.gwt.dom.client.ImageElement;

/**
 * Created by Beat
 * 15.12.2016.
 */
public abstract class AbstractPointingGuiTip extends AbstractGuiTip {
    private GuiPointingTipVisualization guiPointingTipVisualization;

    protected abstract void updatePosition(Index screenPosition);

    protected abstract String getImageCss();

    public void init(GuiPointingTipVisualization guiPointingTipVisualization) {
        this.guiPointingTipVisualization = guiPointingTipVisualization;
        super.init(guiPointingTipVisualization, "tip-animation-container", getImageCss());
    }

    @Override
    public void onLoaded(ImageElement imageElement) {
        super.onLoaded(imageElement);
        guiPointingTipVisualization.setPositionConsumer(this::updatePosition);
    }
}
