package com.btxtech.client;

import com.btxtech.client.math3d.WebGlUtil;
import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.GWT;
import elemental.client.Browser;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 12.07.2015.
 */
@Singleton
public class GameCanvas {
    private Canvas canvas;
    private WebGLRenderingContext ctx3d;
    private double lastTimestamp = 0;
    // @Inject does not work
    private Logger logger = Logger.getLogger(GameCanvas.class.getName());

    public interface RenderCallback {
        void doRender();
    }

    @PostConstruct
    public void createCanvas() {
        // Create canvas
        canvas = Canvas.createIfSupported();
        if (canvas == null) {
            throw new IllegalStateException("Canvas is not supported");
        }
        canvas.getElement().getStyle().setBackgroundColor("#000000");
        canvas.setCoordinateSpaceWidth(640);
        canvas.setCoordinateSpaceHeight(480);
        // Create 3d context
        ctx3d = (WebGLRenderingContext) canvas.getContext("experimental-webgl");
        if (ctx3d == null) {
            ctx3d = (WebGLRenderingContext) canvas.getContext("webgl");
        }
        if (ctx3d == null) {
            Browser.getWindow().alert("WebGL not supported ?!?!?!");
        }
        // Configure context
        ctx3d.clearColor(0.0f, 0.0f, 0.0f, 1.0f);
        ctx3d.clearDepth(1.0f);
        ctx3d.viewport(0, 0, 640, 480);
        ctx3d.enable(WebGLRenderingContext.DEPTH_TEST);
        ctx3d.depthFunc(WebGLRenderingContext.LEQUAL);
        logger.severe("GameCanvas initialized");
    }

    public void startRenderLoop(final RenderCallback renderCallback) {
        // Start render loop
        AnimationScheduler.get().requestAnimationFrame(new AnimationScheduler.AnimationCallback() {
            @Override
            public void execute(double timestamp) {
                try {
                    if (lastTimestamp != 0) {
                        ctx3d.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT);
                        WebGlUtil.checkLastWebGlError("clear", ctx3d);
                        renderCallback.doRender();
                    }
                    lastTimestamp = timestamp;
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "AnimationScheduler execute failed", t);
                }
                AnimationScheduler.get().requestAnimationFrame(this);
            }
        }, canvas.getCanvasElement());

    }

    public Canvas getCanvas() {
        return canvas;
    }

    public WebGLRenderingContext getCtx3d() {
        return ctx3d;
    }
}
