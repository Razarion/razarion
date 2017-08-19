package com.btxtech.client.cockpit.radar;

import com.btxtech.shared.datatypes.Rectangle2D;
import com.google.gwt.dom.client.Element;
import elemental.html.CanvasElement;
import elemental.html.CanvasRenderingContext2D;

/**
 * Created by Beat
 * on 16.06.2017.
 */
public class AbstractMiniMap {
    private CanvasElement canvasElement;
    private int width;
    private int height;
    private CanvasRenderingContext2D ctx;

    protected void init(Element canvasElement, int width, int height) {
        this.canvasElement = (CanvasElement) canvasElement;
        this.canvasElement.setWidth(width);
        this.canvasElement.setHeight(height);
        this.width = width;
        this.height = height;
        ctx = (CanvasRenderingContext2D) this.canvasElement.getContext("2d");
    }

    protected void scaleToPlayground(Rectangle2D playground) {
        float scale = calculateMinScale(playground);
        ctx.translate(0, height);
        ctx.scale(scale, -scale);
    }

    protected void scaleToNormal() {
        ctx.translate(0, height);
        ctx.scale(1, -1);
    }

    public float calculateMinScale(Rectangle2D playground) {
        return (float) Math.min((double) width / playground.width(), (double) height / playground.height());
    }

    protected CanvasRenderingContext2D getCtx() {
        return ctx;
    }

    protected CanvasElement getCanvasElement() {
        return canvasElement;
    }

    protected void clearCanvas() {
        ctx.save();
        ctx.setTransform(1, 0, 0, 1, 0, 0);
        ctx.clearRect(0, 0, canvasElement.getWidth(), canvasElement.getHeight());
        ctx.restore();
    }
}
