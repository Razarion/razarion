package com.btxtech.client.cockpit.radar;

import com.btxtech.uiservice.renderer.ViewField;
import com.google.gwt.dom.client.Element;
import elemental.html.CanvasElement;
import elemental.html.CanvasRenderingContext2D;

import java.util.logging.Logger;

/**
 * Created by Beat
 * on 16.06.2017.
 */
public abstract class AbstractMiniMap {
    private Logger logger = Logger.getLogger(Logger.class.getName());
    private CanvasElement canvasElement;
    private int width;
    private int height;
    private CanvasRenderingContext2D ctx;
    private ViewField viewField;
    private double zoom;

    protected abstract void setupTransformation(double zoom, CanvasRenderingContext2D ctx, int width, int height);

    protected abstract void draw(CanvasRenderingContext2D ctx);

    public void init(Element canvasElement, int width, int height, double zoom) {
        this.canvasElement = (CanvasElement) canvasElement;
        this.canvasElement.setWidth(width);
        this.canvasElement.setHeight(height);
        this.width = width;
        this.height = height;
        ctx = (CanvasRenderingContext2D) this.canvasElement.getContext("2d");
        this.zoom = zoom;
    }

    public void update() {
        clearCanvas();

        ctx.save();
        setupTransformation(zoom, ctx, width, height);
        draw(ctx);
        ctx.restore();
    }

    protected CanvasRenderingContext2D getCtx() {
        return ctx;
    }

    protected void clearCanvas() {
        ctx.save();
        ctx.setTransform(1, 0, 0, 1, 0, 0);
        ctx.clearRect(0, 0, canvasElement.getWidth(), canvasElement.getHeight());
        ctx.restore();
    }

    public void setViewField(ViewField viewField) {
        this.viewField = viewField;
    }

    protected ViewField getViewField() {
        return viewField;
    }

    protected int getWidth() {
        return width;
    }

    protected int getHeight() {
        return height;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    protected double getZoom() {
        return zoom;
    }
}
