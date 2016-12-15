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
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 14.12.2016.
 */
@Templated("HorizontalGuiTip.html#tip")
public class HorizontalGuiTip implements IsElement, ImageUiService.ImageListener {
    // Corresponds to the MOVE_DISTANCE in razarion.css
    private static final int MOVE_DISTANCE = 100;
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

    public void init(GuiTipVisualization guiTipVisualization) {
        this.guiTipVisualization = guiTipVisualization;
        imageUiService.requestImage(guiTipVisualization.getImageId(), this);
        image.setSrc(RestUrl.getImageServiceUrlSafe(guiTipVisualization.getImageId()));
        div.getStyle().setProperty("z-index", Integer.toString(ZIndexConstants.TIP));
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

    private void updatePosition(Index screenPosition) {
        div.getStyle().setProperty("left", screenPosition.getX() + "px");
        div.getStyle().setProperty("top", screenPosition.getY() - imageElement.getHeight() / 2 + "px");

        div.getStyle().setProperty("width", (imageElement.getWidth() + MOVE_DISTANCE) + "px");
        div.getStyle().setProperty("height", imageElement.getHeight() + "px");

        image.getStyle().setProperty("visibility", "visible");
    }
}
