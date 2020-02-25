package com.btxtech.client.renderer;

import com.btxtech.client.ClientTrackerService;
import com.btxtech.client.KeyboardEventHandler;
import com.btxtech.client.MainPanelService;
import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.uiservice.mouse.TerrainMouseHandler;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.google.gwt.user.client.Window;
import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.MouseEvent;
import elemental.events.WheelEvent;
import elemental2.core.Int8Array;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import elemental2.webgl.WebGLRenderingContext;
import jsinterop.base.Js;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

import static elemental2.dom.CSSProperties.HeightUnionType;
import static elemental2.dom.CSSProperties.WidthUnionType;
import static elemental2.dom.DomGlobal.RequestAnimationFrameCallbackFn;

/**
 * Created by Beat
 * 12.07.2015.
 */
@ApplicationScoped
public class GameCanvas {
    // @Inject does not work
    private Logger logger = Logger.getLogger(GameCanvas.class.getName());
    @Inject
    private MainPanelService mainPanelService;
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
    private WebGLRenderingContext ctx3d;
    private RequestAnimationFrameCallbackFn animationCallback;
    private int width;
    private int height;
    private HTMLCanvasElement canvasElement;
    private boolean running;
    private boolean playbackMode;

//    public GameCanvas() {
//        logger.severe("GameCanvas <init> called twice????");
//    }

    public void init() {
        canvasElement = (HTMLCanvasElement) DomGlobal.document.createElement("canvas");
        if (canvasElement == null) {
            throw new IllegalStateException("Canvas is not supported");
        }

        initCanvas();

        setupAnimationCallback();

        Window.addResizeHandler(event -> onWindowResized());

        initMouseHandler();
        keyboardEventHandler.init();

        mainPanelService.addToGamePanel(canvasElement);
    }

    private void onWindowResized() {
        if (!playbackMode) {
            logger.severe("This is wrong. FIX canvas width & height. Its not the Windows size");
            width = Window.getClientWidth();
            height = Window.getClientHeight();
            trackerService.onResizeCanvas(new Index(width, height));
        }
        resizeCanvas();
    }

    private void resizeCanvas() {
        canvasElement.width = width;
        canvasElement.height = height;
        projectionTransformation.setAspectRatio((double) width / (double) height);
    }

    private void initCanvas() {
        // CSS settings
        canvasElement.style.zIndex = ZIndexConstants.WEBGL_CANVAS;
        canvasElement.style.width = WidthUnionType.of("100%");
        canvasElement.style.height = HeightUnionType.of("100%");
        canvasElement.style.position = "absolute";
        // Create 3d context
        ctx3d = Js.cast(canvasElement.getContext("webgl"));
        if (ctx3d == null) {
            Browser.getWindow().alert("WebGL not supported ?!?!?!");
        }
        // Configure context
        ctx3d.clearColor(0.0f, 0.0f, 0.0f, 1.0f);
        ctx3d.clearDepth(1.0f);
        ctx3d.enable((int) WebGLRenderingContext.DEPTH_TEST);
        // logger.severe("GameCanvas initialized");
    }

    private void setupAnimationCallback() {
        animationCallback = timestamp -> {
            if (!running) {
                return null;
            }
            try {
                renderService.render();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "requestAnimationFrame() execute failed", t);
            }
            DomGlobal.requestAnimationFrame(animationCallback, canvasElement);
            return null;
        };
    }

    private void initMouseHandler() {
        canvasElement.addEventListener(Event.MOUSEMOVE, evt -> {
            if (playbackMode) {
                return;
            }
            MouseEvent mouseEvent = (MouseEvent) evt;
            terrainMouseHandler.onMouseMove(mouseEvent.getClientX(), mouseEvent.getClientY(), width, height, GwtUtils.isButtonDown(mouseEvent, 1));
        }, true);
        canvasElement.addEventListener(Event.MOUSEOUT, evt -> {
            if (playbackMode) {
                return;
            }
            terrainMouseHandler.onMouseOut();
        }, true);
        canvasElement.addEventListener(Event.MOUSEDOWN, evt -> {
            if (playbackMode) {
                return;
            }
            MouseEvent mouseEvent = (MouseEvent) evt;
            if (GwtUtils.isButtonResponsible4Event(mouseEvent, MouseEvent.Button.PRIMARY)) {
                terrainMouseHandler.onMouseDown(mouseEvent.getClientX(), mouseEvent.getClientY(), width, height, mouseEvent.isShiftKey());
            }
            if (mouseEvent.getButton() == MouseEvent.Button.PRIMARY && mouseEvent.isAltKey()) {
                Int8Array uint8Array = new Int8Array(4);
                ctx3d.readPixels(mouseEvent.getClientX(),
                        canvasElement.height - mouseEvent.getClientY(),
                        1, 1,
                        (int) WebGLRenderingContext.RGBA,
                        (int) WebGLRenderingContext.UNSIGNED_BYTE,
                        uint8Array);
                WebGlUtil.checkLastWebGlError("readPixels", ctx3d);
                logger.severe("Read screen pixel at " + mouseEvent.getClientX() + ":" + mouseEvent.getClientY() + " RGBA=" + uint8Array.buffer + "," + uint8Array.getAt(0) + "," + uint8Array.getAt(1) + "," + uint8Array.getAt(2) + "," + uint8Array.getAt(3) + "(if 0,0,0,0 -> {preserveDrawingBuffer: true})");
                double x = uint8Array.getAt(0) / 255.0 * 2.0 - 1.0;
                double y = uint8Array.getAt(1) / 255.0 * 2.0 - 1.0;
                double z = uint8Array.getAt(2) / 255.0 * 2.0 - 1.0;
                logger.severe("x=" + x + " y=" + y + " z=" + z);
            }
        }, true);
        canvasElement.addEventListener(Event.MOUSEUP, evt -> {
            if (playbackMode) {
                return;
            }
            MouseEvent mouseEvent = (MouseEvent) evt;
            if (GwtUtils.isButtonResponsible4Event(mouseEvent, MouseEvent.Button.PRIMARY)) {
                terrainMouseHandler.onMouseUp(mouseEvent.getClientX(), mouseEvent.getClientY(), width, height);
            }
        }, true);
        canvasElement.addEventListener("wheel", evt -> {
            if (playbackMode) {
                return;
            }
            WheelEvent wheelEvent = (WheelEvent) evt;
            terrainMouseHandler.onMouseWheel(GwtUtils.getDeltaYFromWheelEvent(wheelEvent));
            wheelEvent.preventDefault();
        }, true);
        GwtUtils.preventContextMenu(canvasElement);
    }

    public void startRenderLoop() {
        running = true;
        onWindowResized();
        DomGlobal.requestAnimationFrame(animationCallback, canvasElement);
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

    public void enterPlaybackMode() {
        playbackMode = true;
    }

    public void setPlaybackDimension(Index browserWindowDimension) {
        width = browserWindowDimension.getX();
        height = browserWindowDimension.getY();
        canvasElement.style.width = WidthUnionType.of(width + "px");
        canvasElement.style.height = HeightUnionType.of(height + "px");
        resizeCanvas();
    }

    public Index getWindowDimenionForPlayback() {
        return new Index(Window.getClientWidth(), Window.getClientHeight());
    }

    public void setCursor(String cursor) {
        canvasElement.style.cursor = cursor;
    }
}
