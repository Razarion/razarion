package com.btxtech.client.tip;

import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.imageservice.ImageUiService;
import com.btxtech.shared.CommonUrl;
import com.btxtech.uiservice.tip.visualization.AbstractGuiTipVisualization;
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
    private AbstractGuiTipVisualization guiTipVisualization;
    private ImageElement imageElement;

    public void init(AbstractGuiTipVisualization guiPointingTipVisualization, String containerCss, String imageCss) {
        this.guiTipVisualization = guiPointingTipVisualization;
        imageUiService.requestImage(guiPointingTipVisualization.getImageId(), this);
        image.setSrc(CommonUrl.getImageServiceUrlSafe(guiPointingTipVisualization.getImageId()));
        div.getStyle().setProperty("z-index", Integer.toString(ZIndexConstants.TIP));
        div.setClassName(containerCss);
        image.setClassName(imageCss);
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
    }

    protected Div getDiv() {
        return div;
    }

    ImageElement getImageElement() {
        return imageElement;
    }

    protected Image getImage() {
        return image;
    }
}
