package com.btxtech.client;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.primitives.Ray3d;
import com.btxtech.shared.primitives.Vertex;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
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
public class ViewFieldMover {
    private Logger logger = Logger.getLogger(ViewFieldMover.class.getName());
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private Event<TerrainMouseMoveEvent> terrainMouseMoveEvent;
    @Inject
    private Event<TerrainMouseDownEvent> terrainMouseDownEvent;
    @Inject
    private Event<TerrainMouseUpEvent> terrainMouseUpEvent;
    private Index startMoveXY;
    private Integer startMoveZ;
    private double factor = 0.5;

    public void activate(final Canvas canvas) {
        canvas.addMouseMoveHandler(new MouseMoveHandler() {
            @Override
            public void onMouseMove(MouseMoveEvent event) {
                if ((eventGetButton(event.getNativeEvent()) & NativeEvent.BUTTON_LEFT) == NativeEvent.BUTTON_LEFT) {
                    if (startMoveXY != null) {
                        Index endMove = new Index(event.getX(), event.getY());
                        Index delta = endMove.sub(startMoveXY);
                        if (delta.isNull()) {
                            return;
                        }
                        camera.setTranslateX(camera.getTranslateX() + factor * (double) -delta.getX());
                        camera.setTranslateY(camera.getTranslateY() + factor * (double) delta.getY());
                        startMoveXY = endMove;
                    }
                } else {
                    startMoveXY = null;
                }
                if ((eventGetButton(event.getNativeEvent()) & NativeEvent.BUTTON_RIGHT) == NativeEvent.BUTTON_RIGHT) {
                    if (startMoveZ != null) {
                        int deltaZ = event.getY() - startMoveZ;
                        camera.setTranslateZ(camera.getTranslateZ() + factor * (double) deltaZ);
                        startMoveZ = event.getY();
                    }
                } else {
                    startMoveZ = null;
                }
                // Send pick ray event
                Ray3d worldPickRay = setupTerrainRay3d(event, canvas);
                terrainMouseMoveEvent.fire(new TerrainMouseMoveEvent(worldPickRay));
            }
        });
        canvas.addMouseDownHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                if ((eventGetButton(event.getNativeEvent()) & NativeEvent.BUTTON_LEFT) == NativeEvent.BUTTON_LEFT) {
                    startMoveXY = new Index(event.getX(), event.getY());
                    if (eventIsShiftPressed(event.getNativeEvent())) {
                        DecimalPosition webglPosition = new DecimalPosition((double) event.getX() / (double) canvas.getCoordinateSpaceWidth(), 1.0 - (double) event.getY() / (double) canvas.getCoordinateSpaceHeight());
                        webglPosition = webglPosition.multiply(2.0);
                        webglPosition = webglPosition.sub(1, 1);
                        Ray3d pickRay = projectionTransformation.createPickRay(webglPosition);
                        Ray3d worldPickRay = camera.toWorld(pickRay);
                        Vertex terrainPosition = terrainSurface.calculatePositionOnZeroLevel(worldPickRay);
                        logger.severe("Terrain Position: " + terrainPosition);
                    }
                    if (eventIsAltPressed(event.getNativeEvent())) {
                        JsUint8Array uint8Array = WebGlUtil.createUint8Array(4);
                        gameCanvas.getCtx3d().readPixels(event.getX(), canvas.getCoordinateSpaceHeight() - event.getY(), 1, 1, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, uint8Array);
                        WebGlUtil.checkLastWebGlError("readPixels", gameCanvas.getCtx3d());
                        logger.severe("Read screen pixel at " + event.getX() + ":" + event.getY() + " RGBA=" + uint8Array.getBuffer() + "," + uint8Array.numberAt(0) + "," + uint8Array.numberAt(1) + "," + uint8Array.numberAt(2) + "," + uint8Array.numberAt(3) + "(if 0,0,0,0 -> {preserveDrawingBuffer: true})");
                        double x = uint8Array.numberAt(0) / 255.0 * 2.0 - 1.0;
                        double y = uint8Array.numberAt(1) / 255.0 * 2.0 - 1.0;
                        double z = uint8Array.numberAt(2) / 255.0 * 2.0 - 1.0;
                        logger.severe("x=" + x + " y=" + y + " z=" + z);
                    }
                    Ray3d worldPickRay = setupTerrainRay3d(event, canvas);
                    terrainMouseDownEvent.fire(new TerrainMouseDownEvent(worldPickRay, event));
                } else if ((eventGetButton(event.getNativeEvent()) & NativeEvent.BUTTON_RIGHT) == NativeEvent.BUTTON_RIGHT) {
                    startMoveZ = event.getY();
                }
            }
        });
        canvas.addMouseUpHandler(new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {
                Ray3d worldPickRay = setupTerrainRay3d(event, canvas);
                terrainMouseUpEvent.fire(new TerrainMouseUpEvent(worldPickRay));
            }
        });

        canvas.addMouseWheelHandler(new MouseWheelHandler() {
            @Override
            public void onMouseWheel(MouseWheelEvent event) {
                if ((eventGetButton(event.getNativeEvent()) & NativeEvent.BUTTON_LEFT) == NativeEvent.BUTTON_LEFT) {
                    double newAngleX = camera.getRotateX() + Math.toRadians(event.getDeltaY());
                    if (newAngleX < 0) {
                        newAngleX = 0;
                    } else if (newAngleX > MathHelper.QUARTER_RADIANT) {
                        newAngleX = MathHelper.QUARTER_RADIANT;
                    }
                    camera.setRotateX(newAngleX);
                } else if ((eventGetButton(event.getNativeEvent()) & NativeEvent.BUTTON_RIGHT) == NativeEvent.BUTTON_RIGHT) {
                    double newAngleZ = camera.getRotateZ() + Math.toRadians(event.getDeltaY());
                    if (newAngleZ < -MathHelper.ONE_RADIANT) {
                        newAngleZ = -MathHelper.ONE_RADIANT;
                    } else if (newAngleZ > MathHelper.ONE_RADIANT) {
                        newAngleZ = MathHelper.ONE_RADIANT;
                    }
                    camera.setRotateZ(newAngleZ);
                } else {
                    projectionTransformation.setFovY(projectionTransformation.getFovY() + Math.toRadians(event.getDeltaY()));
                    event.preventDefault();
                }
            }
        });
    }

    private Ray3d setupTerrainRay3d(MouseEvent event, Canvas canvas) {
        DecimalPosition webglPosition = new DecimalPosition((double) event.getX() / (double) canvas.getCoordinateSpaceWidth(), 1.0 - (double) event.getY() / (double) canvas.getCoordinateSpaceHeight());
        webglPosition = webglPosition.multiply(2.0);
        webglPosition = webglPosition.sub(1, 1);
        Ray3d pickRay = projectionTransformation.createPickRay(webglPosition);
        return camera.toWorld(pickRay);
    }

    public static native int eventGetButton(NativeEvent evt) /*-{
        return evt.buttons;
    }-*/;

    public static native boolean eventIsShiftPressed(NativeEvent evt) /*-{
        return evt.shiftKey;
    }-*/;

    public static native boolean eventIsAltPressed(NativeEvent evt) /*-{
        return evt.altKey;
    }-*/;

}
