package com.btxtech.client.renderer;

import com.btxtech.client.ClientTrackerService;
import com.btxtech.client.KeyboardEventHandler;
import com.btxtech.client.gwtangular.GwtAngularService;
import com.btxtech.client.gwtangular.StatusProvider;
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
@Deprecated
@ApplicationScoped
public class GameCanvas {
    // @Inject does not work
    private Logger logger = Logger.getLogger(GameCanvas.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
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
    @Inject
    private StatusProvider statusProvider;
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
//        canvasElement = gwtAngularService.getCanvasElement();
//        if (canvasElement == null) {
//            throw new IllegalStateException("Canvas is not supported");
//        }
//
//        initCanvas();
//
//        setupAnimationCallback();
//
//        gwtAngularService.setCanvasResizeListener(this::onWindowResized);
//
//        initMouseHandler();
//        keyboardEventHandler.init();
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
