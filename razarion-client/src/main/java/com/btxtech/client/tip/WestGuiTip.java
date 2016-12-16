package com.btxtech.client.tip;

import com.btxtech.shared.datatypes.Index;
import org.jboss.errai.ui.shared.api.annotations.Templated;

/**
 * Created by Beat
 * 14.12.2016.
 */
@Templated("GuiTip.html#tip")
public class WestGuiTip extends AbstractPointingGuiTip {
    // Corresponds to the MOVE_DISTANCE in razarion.css
    private static final int MOVE_DISTANCE = 100;

    @Override
    protected String getImageCss() {
        return "tip-west-animation-image";
    }

    @Override
    protected void updatePosition(Index screenPosition) {
        getDiv().getStyle().setProperty("left", screenPosition.getX() + "px");
        getDiv().getStyle().setProperty("top", screenPosition.getY() - getImageElement().getHeight() / 2 + "px");

        getDiv().getStyle().setProperty("width", (getImageElement().getWidth() + MOVE_DISTANCE) + "px");
        getDiv().getStyle().setProperty("height", getImageElement().getHeight() + "px");

        getImage().getStyle().setProperty("visibility", "visible");
    }
}
