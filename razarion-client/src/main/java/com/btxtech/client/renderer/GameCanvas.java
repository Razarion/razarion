package com.btxtech.client.renderer;

import com.btxtech.client.ClientTrackerService;
import com.btxtech.client.gwtangular.GwtAngularService;
import com.btxtech.client.KeyboardEventHandler;
import com.btxtech.client.MainPanelService;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.mouse.TerrainMouseHandler;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import elemental2.core.JsObject;
import elemental2.core.Uint8Array;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.MouseEvent;
import elemental2.dom.WheelEvent;
import elemental2.webgl.WebGLRenderingContext;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.btxtech.client.utils.DomConstants.Event.MOUSEDOWN;
import static com.btxtech.client.utils.DomConstants.Event.MOUSEMOVE;
import static com.btxtech.client.utils.DomConstants.Event.MOUSEOUT;
import static com.btxtech.client.utils.DomConstants.Event.MOUSEUP;
import static com.btxtech.client.utils.DomConstants.Event.WHEEL;
import static com.btxtech.client.utils.DomConstants.Mouse.BUTTON_MAIN;
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
    private ExceptionHandler exceptionHandler;
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
    @Inject
    private GwtAngularService gwtAngularService;
    private WebGLRenderingContext ctx3d;
    private RequestAnimationFrameCallbackFn animationCallback;
    private double width;
    private double height;
    private HTMLCanvasElement canvasElement;
    private boolean running;
    private boolean playbackMode;

//    public GameCanvas() {
//        logger.severe("GameCanvas <display> called twice????");
//    }

    public void init() {
        canvasElement = gwtAngularService.getCanvasElement();
        if (canvasElement == null) {
            throw new IllegalStateException("Canvas is not supported");
        }

        initCanvas();

        setupAnimationCallback();

        mainPanelService.addResizeListener(this::onWindowResized);

        initMouseHandler();
        keyboardEventHandler.init();
    }

    private void onWindowResized() {
        if (!playbackMode) {
            width = canvasElement.offsetWidth;
            height = canvasElement.offsetHeight;
            trackerService.onResizeCanvas(new Index((int) width, (int) height));
        }
        resizeCanvas();
    }

    private void resizeCanvas() {
        canvasElement.width = width;
        canvasElement.height = height;
        projectionTransformation.setAspectRatio(width / height);
    }

    private void initCanvas() {
        // Create 3d context
        JsPropertyMap<Object> args = JsPropertyMap.of();
        // WEBGL TRANSPARENCY AND ALPHA BLENDING PROBLEM
        // {alpha:false}
        // http://in2gpu.com/2014/04/11/webgl-transparency/
        // {preserveDrawingBuffer: true}
        // http://stackoverflow.com/questions/7156971/webgl-readpixels-is-always-returning-0-0-0-0
        args.set("alpha", false);
        args.set("preserveDrawingBuffer", true);
        args.set("antialias", true);
        ctx3d = Js.cast(canvasElement.getContext("webgl", (JsObject) args));
        if (ctx3d == null) {
            DomGlobal.window.alert("WebGL not supported ?!?!?!");
        }
        // Configure context
        ctx3d.clearColor(0.0f, 0.0f, 0.0f, 1.0f);
        ctx3d.clearDepth(1.0f);
        ctx3d.enable((int) WebGLRenderingContext.DEPTH_TEST);
        // logger.severe("GameCanvas initialized");
    }

    private void setupAnimationCallback() {
        animationCallback = timestamp -> {
            try {
                if (!running) {
                    return null;
                }
                try {
                    renderService.render();
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "requestAnimationFrame() execute failed", t);
                }
                DomGlobal.requestAnimationFrame(animationCallback, canvasElement);
            } catch (Throwable throwable) {
                exceptionHandler.handleException(throwable);
            }
            return null;
        };
    }

    private void initMouseHandler() {
        canvasElement.addEventListener(MOUSEMOVE, evt -> {
            try {
                if (playbackMode) {
                    return;
                }
                MouseEvent mouseEvent = (MouseEvent) evt;
                terrainMouseHandler.onMouseMove((int) mouseEvent.offsetX,
                        (int) mouseEvent.offsetY,
                        (int) width,
                        (int) height,
                        GwtUtils.isButtonDown(mouseEvent, 1));
            } catch (Throwable throwable) {
                exceptionHandler.handleException(throwable);
            }
        }, true);
        canvasElement.addEventListener(MOUSEOUT, evt -> {
            try {
                if (playbackMode) {
                    return;
                }
                terrainMouseHandler.onMouseOut();
            } catch (Throwable throwable) {
                exceptionHandler.handleException(throwable);
            }
        }, true);
        canvasElement.addEventListener(MOUSEDOWN, evt -> {
            try {
                if (playbackMode) {
                    return;
                }
                MouseEvent mouseEvent = (MouseEvent) evt;
                if (GwtUtils.isButtonResponsible4Event(mouseEvent, BUTTON_MAIN)) {
                    terrainMouseHandler.onMouseDown((int) mouseEvent.offsetX,
                            (int) mouseEvent.offsetY,
                            (int) width,
                            (int) height,
                            mouseEvent.shiftKey);
                }
                if (mouseEvent.button == BUTTON_MAIN && mouseEvent.altKey) {
                    Uint8Array uint8Array = new Uint8Array(4);
                    ctx3d.readPixels(mouseEvent.offsetX,
                            canvasElement.height - mouseEvent.offsetY,
                            1, 1,
                            WebGLRenderingContext.RGBA,
                            WebGLRenderingContext.UNSIGNED_BYTE,
                            uint8Array);
                    WebGlUtil.checkLastWebGlError("readPixels", ctx3d);
                    double x = uint8Array.getAt(0) / 255.0 * 2.0 - 1.0;
                    double y = uint8Array.getAt(1) / 255.0 * 2.0 - 1.0;
                    double z = uint8Array.getAt(2) / 255.0 * 2.0 - 1.0;
                    logger.severe("Read screen pixel at " + mouseEvent.offsetX + ":" + mouseEvent.offsetY + " Vector=(" + x + "," + y + "," + z + ") RGBA=(" + uint8Array.getAt(0) + "," + uint8Array.getAt(1) + "," + uint8Array.getAt(2) + "," + uint8Array.getAt(3) + ") (if 0,0,0,0 -> {preserveDrawingBuffer: true})");
                }
            } catch (Throwable throwable) {
                exceptionHandler.handleException(throwable);
            }
        }, true);
        canvasElement.addEventListener(MOUSEUP, evt -> {
            try {
                if (playbackMode) {
                    return;
                }
                MouseEvent mouseEvent = (MouseEvent) evt;
                if (GwtUtils.isButtonResponsible4Event(mouseEvent, BUTTON_MAIN)) {
                    terrainMouseHandler.onMouseUp(
                            (int) mouseEvent.offsetX,
                            (int) mouseEvent.offsetY,
                            (int) width,
                            (int) height);
                }
            } catch (Throwable throwable) {
                exceptionHandler.handleException(throwable);
            }
        }, true);
        canvasElement.addEventListener(WHEEL, evt -> {
            try {
                if (playbackMode) {
                    return;
                }
                WheelEvent wheelEvent = (WheelEvent) evt;
                terrainMouseHandler.onMouseWheel(wheelEvent.deltaY);
                wheelEvent.preventDefault();
            } catch (Throwable throwable) {
                exceptionHandler.handleException(throwable);
            }
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
        return (int) width;
    }

    public int getHeight() {
        return (int) height;
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
        return new Index(getWidth(), getHeight());
    }

    public void setCursor(String cursor) {
        canvasElement.style.cursor = cursor;
    }
}
