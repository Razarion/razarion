package com.btxtech.client.tip;

import com.btxtech.shared.datatypes.Index;
import org.jboss.errai.ui.shared.api.annotations.Templated;

/**
 * Created by Beat
 * 14.12.2016.
 */
@Templated("GuiTip.html#tip")
public class SouthGuiTip extends AbstractGuiTip {
    // Corresponds to the MOVE_DISTANCE in razarion.css
    private static final int MOVE_DISTANCE = 100;

    @Override
    protected String getCssClassName() {
        return "tip-south-animation-image";
    }

    protected void updatePosition(Index screenPosition) {
        getDiv().getStyle().setProperty("left", (screenPosition.getX() - getImageElement().getWidth() / 2) + "px");
        getDiv().getStyle().setProperty("top", (screenPosition.getY() - MOVE_DISTANCE - getImageElement().getHeight()) + "px");

        getDiv().getStyle().setProperty("width", getImageElement().getWidth() + "px");
        getDiv().getStyle().setProperty("height", (getImageElement().getHeight() + MOVE_DISTANCE) + "px");

        getImage().getStyle().setProperty("visibility", "visible");
    }
}
