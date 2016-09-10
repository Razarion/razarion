package com.btxtech.uiservice.mouse;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Ray3d;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;

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
    private TerrainService terrainService;
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


    public void onMouseMove(int x, int y, int width, int height) {
        try {
            terrainScrollHandler.handleMouseMoveScroll(x, y, width, height);
            // Send pick ray event
            Ray3d worldPickRay = setupTerrainRay3d(x, y, width, height);
            Vertex terrainPosition = terrainService.calculatePositionGroundMesh(worldPickRay);
            terrainMouseMoveEvent.fire(new TerrainMouseMoveEvent(worldPickRay, terrainPosition));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void onMouseOut() {
        try {
            terrainScrollHandler.executeAutoScrollMouse(TerrainScrollHandler.ScrollDirection.STOP, TerrainScrollHandler.ScrollDirection.STOP);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void onMouseDown(int x, int y, int width, int height, boolean primary, boolean ctrlKey, boolean shiftKey) {
        try {
            if (primary) {
                Ray3d worldPickRay = setupTerrainRay3d(x, y, width, height);
                Vertex terrainPosition = terrainService.calculatePositionGroundMesh(worldPickRay);
                if (shiftKey) {
                    logger.severe("Terrain Position: " + terrainPosition);
                }
                terrainMouseDownEvent.fire(new TerrainMouseDownEvent(worldPickRay, ctrlKey, terrainPosition));
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void onMouseUp(int x, int y, int width, int height) {
        try {
            Ray3d worldPickRay = setupTerrainRay3d(x, y, width, height);
            terrainMouseUpEvent.fire(new TerrainMouseUpEvent(worldPickRay));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void onMouseWheel(double deltaY) {
        try {
            projectionTransformation.setFovY(projectionTransformation.getFovY() - Math.toRadians(deltaY) / MOUSE_WHEEL_DIVIDER);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    private Ray3d setupTerrainRay3d(int x, int y, int width, int height) {
        DecimalPosition webglClipPosition = new DecimalPosition((double) x / (double) width, 1.0 - (double) y / (double) height);
        webglClipPosition = webglClipPosition.multiply(2.0);
        webglClipPosition = webglClipPosition.sub(1, 1);
        Ray3d pickRay = projectionTransformation.createPickRay(webglClipPosition);
        return camera.toWorld(pickRay);
    }
}
