package com.btxtech.client.cockpit.radar;

import com.btxtech.uiservice.renderer.ViewField;
import elemental.html.CanvasRenderingContext2D;

import javax.enterprise.context.Dependent;

/**
 * Created by Beat
 * on 16.06.2017.
 */
@Dependent
public class MiniViewField extends AbstractGameCoordinates {
    private static final double LINE_WIDTH = 2;

    @Override
    protected void draw(CanvasRenderingContext2D ctx) {
        ViewField viewField = getViewField();

        getCtx().setLineWidth(toCanvasPixel(LINE_WIDTH));

        getCtx().setStrokeStyle("#fff");
        getCtx().beginPath();
        getCtx().moveTo((float) viewField.getBottomLeft().getX(), (float) viewField.getBottomLeft().getY());
        getCtx().lineTo((float) viewField.getBottomRight().getX(), (float) viewField.getBottomRight().getY());
        getCtx().lineTo((float) viewField.getTopRight().getX(), (float) viewField.getTopRight().getY());
        getCtx().lineTo((float) viewField.getTopLeft().getX(), (float) viewField.getTopLeft().getY());
        getCtx().closePath();
        getCtx().stroke();
    }
}
