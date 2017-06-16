package com.btxtech.client.cockpit.radar;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.uiservice.renderer.ViewField;
import com.google.gwt.dom.client.Element;

/**
 * Created by Beat
 * on 16.06.2017.
 */
public class MiniViewField extends AbstractMiniMap {
    public MiniViewField(Element canvasElement, int width, int height) {
        super(canvasElement, width, height);
        scaleToNormal();
    }

    public void onViewChanged(ViewField viewField, Rectangle2D playGround) {
        clearCanvas();

        float scale = calculateMinScale(playGround);

        getCtx().setStrokeStyle("#fff");

        getCtx().beginPath();

        DecimalPosition bottomLeft = convert(viewField.getBottomLeft(), playGround, scale);
        getCtx().moveTo((float) bottomLeft.getX(), (float) bottomLeft.getY());
        DecimalPosition bottomRight = convert(viewField.getBottomRight(), playGround, scale);
        getCtx().lineTo((float) bottomRight.getX(), (float) bottomRight.getY());
        DecimalPosition topRight = convert(viewField.getTopRight(), playGround, scale);
        getCtx().lineTo((float) topRight.getX(), (float) topRight.getY());
        DecimalPosition topLeft = convert(viewField.getTopLeft(), playGround, scale);
        getCtx().lineTo((float) topLeft.getX(), (float) topLeft.getY());
        getCtx().closePath();
        getCtx().stroke();
    }

    private DecimalPosition convert(DecimalPosition input, Rectangle2D playGround, double scale) {
        return input.sub(playGround.getStart()).multiply(scale);
    }
}
