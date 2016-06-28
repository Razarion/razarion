package com.btxtech.client.renderer;

import com.btxtech.client.MouseEventHandler;
import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.WebGLRenderingContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 12.07.2015.
 */
@Singleton
public class GameCanvas {
    private WebGLRenderingContext ctx3d;
    private double lastTimestamp = 0;
    // @Inject does not work
    private Logger logger = Logger.getLogger(GameCanvas.class.getName());
    @Inject
    private RenderService renderService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    private int width;
    private int height;
    private Canvas canvas;

    public GameCanvas() {
        logger.severe("GameCanvas <init> called twice????");
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public void init() {
        if (canvas == null) {
            throw new IllegalStateException("Canvas not set");
        }
        initCanvas();
        resizeCanvas();

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                resizeCanvas();
            }
        });
    }

    private void resizeCanvas() {
        width = Window.getClientWidth();
        height = Window.getClientHeight();
        canvas.setCoordinateSpaceWidth(width);
        canvas.setCoordinateSpaceHeight(height);
        projectionTransformation.setAspectRatio((double) width / (double) height);
    }

    private void initCanvas() {
        // Create 3d context
        ctx3d = WebGlUtil.getContext(canvas.getCanvasElement(), "experimental-webgl");
        if (ctx3d == null) {
            ctx3d = WebGlUtil.getContext(canvas.getCanvasElement(), "webgl");
        }
        if (ctx3d == null) {
            Browser.getWindow().alert("WebGL not supported ?!?!?!");
        }
        // Configure context
        ctx3d.clearColor(0.0f, 0.0f, 0.0f, 1.0f);
        ctx3d.clearDepth(1.0f);
        ctx3d.enable(WebGLRenderingContext.DEPTH_TEST);
        logger.severe("GameCanvas initialized");
    }

    public void startRenderLoop() {
        // Start render tick
        AnimationScheduler.get().requestAnimationFrame(new AnimationScheduler.AnimationCallback() {
            @Override
            public void execute(double timestamp) {
                try {
                    if (lastTimestamp != 0) {
                        WebGlUtil.checkLastWebGlError("clear", ctx3d);
                        renderService.draw();
                    }
                    lastTimestamp = timestamp;
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "AnimationScheduler execute failed", t);
                }
                AnimationScheduler.get().requestAnimationFrame(this);
            }
        }, canvas.getCanvasElement());

    }

    public WebGLRenderingContext getCtx3d() {
        return ctx3d;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Element getCanvasElement() {
        return GwtUtils.castElementToElement(canvas.getElement());
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
