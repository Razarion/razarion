package com.btxtech.client;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Ray3d;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.btxtech.uiservice.terrain.TerrainUiService;
import elemental.events.MouseEvent;
import elemental.events.WheelEvent;
import elemental.html.WebGLRenderingContext;
import elemental.js.html.JsUint8Array;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 12.07.2015.
 */
@Singleton
public class MouseEventHandler {
    private static final int MOUSE_WHEEL_DIVIDER = 60;
    private Logger logger = Logger.getLogger(MouseEventHandler.class.getName());
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private Event<TerrainMouseMoveEvent> terrainMouseMoveEvent;
    @Inject
    private Event<TerrainMouseDownEvent> terrainMouseDownEvent;
    @Inject
    private Event<TerrainMouseUpEvent> terrainMouseUpEvent;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;

    public void init() {
        gameCanvas.getCanvasElement().addEventListener(elemental.events.Event.MOUSEMOVE, evt -> {
            try {
                MouseEvent mouseEvent = (MouseEvent) evt;
                terrainScrollHandler.handleMouseMoveScroll(mouseEvent.getClientX(), mouseEvent.getClientY(), gameCanvas.getWidth(), gameCanvas.getHeight());
                // Send pick ray event
                Ray3d worldPickRay = setupTerrainRay3d(mouseEvent);
                terrainMouseMoveEvent.fire(new TerrainMouseMoveEvent(worldPickRay));
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        }, true);
        gameCanvas.getCanvasElement().addEventListener(elemental.events.Event.MOUSEOUT, evt -> {
            try {
                terrainScrollHandler.executeAutoScrollMouse(TerrainScrollHandler.ScrollDirection.STOP, TerrainScrollHandler.ScrollDirection.STOP);
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        }, true);
        gameCanvas.getCanvasElement().addEventListener(elemental.events.Event.MOUSEDOWN, evt -> {
            try {
                MouseEvent mouseEvent = (MouseEvent) evt;
                if (mouseEvent.getButton() == MouseEvent.Button.PRIMARY) {
                    if (mouseEvent.isShiftKey()) {
                        DecimalPosition webglPosition = new DecimalPosition((double) mouseEvent.getClientX() / (double) gameCanvas.getCanvas().getCoordinateSpaceWidth(), 1.0 - (double) mouseEvent.getClientY() / (double) gameCanvas.getCanvas().getCoordinateSpaceHeight());
                        webglPosition = webglPosition.multiply(2.0);
                        webglPosition = webglPosition.sub(1, 1);
                        Ray3d pickRay = projectionTransformation.createPickRay(webglPosition);
                        Ray3d worldPickRay = camera.toWorld(pickRay);
                        Vertex terrainPosition = terrainUiService.calculatePositionOnZeroLevel(worldPickRay);
                        logger.severe("Terrain Position: " + terrainPosition);
                    }
                    if (mouseEvent.isAltKey()) {
                        JsUint8Array uint8Array = WebGlUtil.createUint8Array(4);
                        gameCanvas.getCtx3d().readPixels(mouseEvent.getClientX(), gameCanvas.getCanvas().getCoordinateSpaceHeight() - mouseEvent.getClientY(), 1, 1, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, uint8Array);
                        WebGlUtil.checkLastWebGlError("readPixels", gameCanvas.getCtx3d());
                        logger.severe("Read screen pixel at " + mouseEvent.getClientX() + ":" + mouseEvent.getClientY() + " RGBA=" + uint8Array.getBuffer() + "," + uint8Array.numberAt(0) + "," + uint8Array.numberAt(1) + "," + uint8Array.numberAt(2) + "," + uint8Array.numberAt(3) + "(if 0,0,0,0 -> {preserveDrawingBuffer: true})");
                        double x = uint8Array.numberAt(0) / 255.0 * 2.0 - 1.0;
                        double y = uint8Array.numberAt(1) / 255.0 * 2.0 - 1.0;
                        double z = uint8Array.numberAt(2) / 255.0 * 2.0 - 1.0;
                        logger.severe("x=" + x + " y=" + y + " z=" + z);
                    }
                    Ray3d worldPickRay = setupTerrainRay3d(mouseEvent);
                    terrainMouseDownEvent.fire(new TerrainMouseDownEvent(worldPickRay, mouseEvent));
                }

            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        }, true);
        gameCanvas.getCanvasElement().addEventListener(elemental.events.Event.MOUSEUP, evt -> {
            MouseEvent mouseEvent = (MouseEvent) evt;
            Ray3d worldPickRay = setupTerrainRay3d(mouseEvent);
            terrainMouseUpEvent.fire(new TerrainMouseUpEvent(worldPickRay));
        }, true);

        gameCanvas.getCanvasElement().addEventListener("wheel", evt -> {
            try {
                WheelEvent wheelEvent = (WheelEvent) evt;
                projectionTransformation.setFovY(projectionTransformation.getFovY() - Math.toRadians(wheelEvent.getWheelDeltaY()) / MOUSE_WHEEL_DIVIDER);
                wheelEvent.preventDefault();
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        }, true);

    }

    private Ray3d setupTerrainRay3d(MouseEvent event) {
        DecimalPosition webglClipPosition = new DecimalPosition((double) event.getClientX() / (double) gameCanvas.getCanvas().getCoordinateSpaceWidth(), 1.0 - (double) event.getClientY() / (double) gameCanvas.getCanvas().getCoordinateSpaceHeight());
        webglClipPosition = webglClipPosition.multiply(2.0);
        webglClipPosition = webglClipPosition.sub(1, 1);
        Ray3d pickRay = projectionTransformation.createPickRay(webglClipPosition);
        return camera.toWorld(pickRay);
    }
}
