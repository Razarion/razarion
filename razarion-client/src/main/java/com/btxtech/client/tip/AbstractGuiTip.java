package com.btxtech.client.tip;

import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.imageservice.ImageUiService;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.uiservice.tip.visualization.GuiTipVisualization;
import com.google.gwt.dom.client.ImageElement;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Image;
import org.jboss.errai.ui.shared.api.annotations.DataField;

import javax.inject.Inject;

/**
 * Created by Beat
 * 15.12.2016.
 */
public abstract class AbstractGuiTip implements IsElement, ImageUiService.ImageListener {
    @Inject
    private ImageUiService imageUiService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField("tip")
    private Div div;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Image image;
    private GuiTipVisualization guiTipVisualization;
    private ImageElement imageElement;

    protected abstract void updatePosition(Index screenPosition);

    protected abstract String getCssClassName();

    public void init(GuiTipVisualization guiTipVisualization) {
        this.guiTipVisualization = guiTipVisualization;
        imageUiService.requestImage(guiTipVisualization.getImageId(), this);
        image.setSrc(RestUrl.getImageServiceUrlSafe(guiTipVisualization.getImageId()));
        div.getStyle().setProperty("z-index", Integer.toString(ZIndexConstants.TIP));
        image.setClassName(getCssClassName());
    }


    void cleanup() {
        imageUiService.removeListener(guiTipVisualization.getImageId(), this);
    }

    @Override
    public HTMLElement getElement() {
        return div;
    }

    @Override
    public void onLoaded(ImageElement imageElement) {
        this.imageElement = imageElement;
        guiTipVisualization.setPositionConsumer(this::updatePosition);
    }

    protected Div getDiv() {
        return div;
    }

    protected ImageElement getImageElement() {
        return imageElement;
    }

    protected Image getImage() {
        return image;
    }
}
