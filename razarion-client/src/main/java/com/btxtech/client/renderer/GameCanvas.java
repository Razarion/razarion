package com.btxtech.client.renderer;

import com.btxtech.client.ClientTrackerService;
import com.btxtech.client.KeyboardEventHandler;
import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.uiservice.TrackerService;
import com.btxtech.uiservice.mouse.TerrainMouseHandler;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.MouseEvent;
import elemental.events.WheelEvent;
import elemental.html.WebGLRenderingContext;
import elemental.js.html.JsUint8Array;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 12.07.2015.
 */
@ApplicationScoped
public class GameCanvas {
    private WebGLRenderingContext ctx3d;
    // @Inject does not work
    private Logger logger = Logger.getLogger(GameCanvas.class.getName());
    @Inject
    private ClientRenderServiceImpl renderService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private TerrainMouseHandler terrainMouseHandler;
    @Inject
    private KeyboardEventHandler keyboardEventHandler;
    @Inject
    private ClientTrackerService trackerService;
    private int width;
    private int height;
    private Canvas canvas;
    private boolean running;

    public GameCanvas() {
        logger.severe("GameCanvas <init> called twice????");
    }

    public void init() {
        canvas = Canvas.createIfSupported();
        if (canvas == null) {
            throw new IllegalStateException("Canvas is not supported");
        }

        initCanvas();

        Window.addResizeHandler(event -> resizeCanvas());

        initMouseHandler();
        keyboardEventHandler.init();

        RootPanel.get().add(canvas);
    }

    private void resizeCanvas() {
        width = Window.getClientWidth();
        height = Window.getClientHeight();
        trackerService.onResizeCanvas(new DecimalPosition(width, height));
        canvas.setCoordinateSpaceWidth(width);
        canvas.setCoordinateSpaceHeight(height);
        projectionTransformation.setAspectRatio((double) width / (double) height);
    }

    private void initCanvas() {
        // CSS settings
        canvas.getElement().getStyle().setZIndex(ZIndexConstants.WEBGL_CANVAS);
        canvas.getElement().getStyle().setWidth(100, Style.Unit.PCT);
        canvas.getElement().getStyle().setHeight(100, Style.Unit.PCT);
        canvas.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
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

    private void initMouseHandler() {
        getCanvasElement().addEventListener(Event.MOUSEMOVE, evt -> {
            MouseEvent mouseEvent = (MouseEvent) evt;
            terrainMouseHandler.onMouseMove(mouseEvent.getClientX(), mouseEvent.getClientY(), width, height, GwtUtils.isButtonDown(mouseEvent, 1));
        }, true);
        getCanvasElement().addEventListener(Event.MOUSEOUT, evt -> terrainMouseHandler.onMouseOut(), true);
        getCanvasElement().addEventListener(Event.MOUSEDOWN, evt -> {
            MouseEvent mouseEvent = (MouseEvent) evt;
            terrainMouseHandler.onMouseDown(mouseEvent.getClientX(), mouseEvent.getClientY(), width, height,
                    GwtUtils.isButtonResponsible4Event(mouseEvent, MouseEvent.Button.PRIMARY), GwtUtils.isButtonResponsible4Event(mouseEvent, MouseEvent.Button.SECONDARY), GwtUtils.isButtonResponsible4Event(mouseEvent, MouseEvent.Button.AUXILIARY),
                    mouseEvent.isCtrlKey(), mouseEvent.isShiftKey());
            if (mouseEvent.getButton() == MouseEvent.Button.PRIMARY && mouseEvent.isAltKey()) {
                JsUint8Array uint8Array = WebGlUtil.createUint8Array(4);
                ctx3d.readPixels(mouseEvent.getClientX(), canvas.getCoordinateSpaceHeight() - mouseEvent.getClientY(), 1, 1, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, uint8Array);
                WebGlUtil.checkLastWebGlError("readPixels", ctx3d);
                logger.severe("Read screen pixel at " + mouseEvent.getClientX() + ":" + mouseEvent.getClientY() + " RGBA=" + uint8Array.getBuffer() + "," + uint8Array.numberAt(0) + "," + uint8Array.numberAt(1) + "," + uint8Array.numberAt(2) + "," + uint8Array.numberAt(3) + "(if 0,0,0,0 -> {preserveDrawingBuffer: true})");
                double x = uint8Array.numberAt(0) / 255.0 * 2.0 - 1.0;
                double y = uint8Array.numberAt(1) / 255.0 * 2.0 - 1.0;
                double z = uint8Array.numberAt(2) / 255.0 * 2.0 - 1.0;
                logger.severe("x=" + x + " y=" + y + " z=" + z);
            }
        }, true);
        getCanvasElement().addEventListener(Event.MOUSEUP, evt -> {
            MouseEvent mouseEvent = (MouseEvent) evt;
            terrainMouseHandler.onMouseUp(mouseEvent.getClientX(), mouseEvent.getClientY(), width, height,
                    GwtUtils.isButtonResponsible4Event(mouseEvent, MouseEvent.Button.PRIMARY));
        }, true);
        getCanvasElement().addEventListener("wheel", evt -> {
            WheelEvent wheelEvent = (WheelEvent) evt;
            terrainMouseHandler.onMouseWheel(wheelEvent.getWheelDeltaY());
            wheelEvent.preventDefault();
        }, true);
        GwtUtils.preventContextMenu(canvas);
    }

    public void startRenderLoop() {
        running = true;
        resizeCanvas();
        AnimationScheduler.get().requestAnimationFrame(new AnimationScheduler.AnimationCallback() {
            @Override
            public void execute(double timestamp) {
                if (!running) {
                    return;
                }
                try {
                    renderService.render();
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "AnimationScheduler execute failed", t);
                }
                AnimationScheduler.get().requestAnimationFrame(this);
            }
        }, canvas.getCanvasElement());

    }

    public void stopRenderLoop() {
        running = false;
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
}
