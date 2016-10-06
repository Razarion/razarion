package com.btxtech.uiservice.mouse;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Group;
import com.btxtech.shared.datatypes.Ray3d;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.terrain.NoInterpolatedTerrainTriangleException;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.GroupSelectionFrame;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.cockpit.CockpitMode;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.task.selection.SelectionFrameRenderTask;
import com.btxtech.uiservice.renderer.task.startpoint.StartPointUiService;
import com.btxtech.uiservice.storyboard.StoryboardService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 12.07.2015.
 */
@ApplicationScoped
public class TerrainMouseHandler {
    private static final int MOUSE_WHEEL_DIVIDER = 60;
    private Logger logger = Logger.getLogger(TerrainMouseHandler.class.getName());
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private TerrainService terrainService;
    @Inject
    @Deprecated
    private Event<TerrainMouseMoveEvent> terrainMouseMoveEvent;
    @Inject
    @Deprecated
    private Event<TerrainMouseDownEvent> terrainMouseDownEvent;
    @Inject
    @Deprecated
    private Event<TerrainMouseUpEvent> terrainMouseUpEvent;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @Inject
    private StartPointUiService startPointUiService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private SelectionHandler selectionHandler;
    @Inject
    private StoryboardService storyboardService;
    @Inject
    private CommandService commandService;
    @Inject
    private ItemCockpitService itemCockpitService;
    @Inject
    private CockpitMode cockpitMode;
    @Inject
    private SelectionFrameRenderTask renderTask;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;

    public void onMouseMove(int x, int y, int width, int height, boolean primaryButtonDown) {
        try {
            terrainScrollHandler.handleMouseMoveScroll(x, y, width, height);
            // Send pick ray event
            Ray3d worldPickRay = setupTerrainRay3d(x, y, width, height);
            Vertex terrainPosition = terrainService.calculatePositionGroundMesh(worldPickRay);
            terrainMouseMoveEvent.fire(new TerrainMouseMoveEvent(worldPickRay, terrainPosition));

            if (primaryButtonDown) {
                GroupSelectionFrame groupSelectionFrame = cockpitMode.getGroupSelectionFrame();
                if (groupSelectionFrame != null) {
                    groupSelectionFrame.onMove(terrainPosition.toXY());
                    renderTask.onMove(groupSelectionFrame);
                } else {
                    logger.warning("TerrainMouseHandler.onMouseMove(): groupSelectionFrame != null");
                }
            }
        } catch (NoInterpolatedTerrainTriangleException e) {
            logger.warning(e.getMessage());
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

    public void onMouseDown(int x, int y, int width, int height, boolean primaryButtonPressed, boolean secondaryButtonPressed, boolean middleButtonPressed, boolean ctrlKey, boolean shiftKey) {
        try {
            Ray3d worldPickRay = setupTerrainRay3d(x, y, width, height);
            Vertex terrainPosition = terrainService.calculatePositionGroundMesh(worldPickRay);
            if (shiftKey) {
                logger.severe("Terrain Position: " + terrainPosition);
            }
            terrainMouseDownEvent.fire(new TerrainMouseDownEvent(worldPickRay, terrainPosition, primaryButtonPressed, secondaryButtonPressed, middleButtonPressed, ctrlKey));

            if (startPointUiService.isActive()) {
                if (primaryButtonPressed) {
                    startPointUiService.onMouseDownEvent(terrainPosition);
                }
                return;
            }

            if (primaryButtonPressed) {
                GroupSelectionFrame groupSelectionFrame = new GroupSelectionFrame(terrainPosition.toXY());
                cockpitMode.setGroupSelectionFrame(groupSelectionFrame);
                renderTask.startGroupSelection(groupSelectionFrame);
            } else if (secondaryButtonPressed) {
                SyncItem syncItem = syncItemContainerService.getItemAtPosition(terrainPosition.toXY());
                if (syncItem != null) {
                    // On item clicked
                    if (syncItem instanceof SyncResourceItem) {
                        selectionHandler.setTargetSelected(syncItem);
                    } else if (syncItem instanceof SyncBaseItem) {
                        SyncBaseItem syncBaseItem = (SyncBaseItem) syncItem;
                        if (!storyboardService.isMyOwnProperty(syncBaseItem)) {
                            selectionHandler.setTargetSelected(syncBaseItem);
                        }
                    } else if (syncItem instanceof SyncBoxItem) {
                        selectionHandler.setTargetSelected(syncItem);
                    } else {
                        throw new IllegalArgumentException(this + " onMouseDown: SyncItem not supported: " + syncItem);
                    }
                } else {
                    // On terrain clicked
                    executeMoveCommand(terrainPosition.toXY());
                }
            }
        } catch (NoInterpolatedTerrainTriangleException e) {
            logger.warning(e.getMessage());
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void onMouseUp(int x, int y, int width, int height, boolean primaryButtonReleased) {
        try {
            Ray3d worldPickRay = setupTerrainRay3d(x, y, width, height);
            Vertex terrainPosition = terrainService.calculatePositionGroundMesh(worldPickRay);
            terrainMouseUpEvent.fire(new TerrainMouseUpEvent(worldPickRay));

            if (primaryButtonReleased) {
                GroupSelectionFrame groupSelectionFrame = cockpitMode.getGroupSelectionFrame();
                if (groupSelectionFrame != null) {
                    renderTask.stop();
                    Rectangle2D rectangle = groupSelectionFrame.finished(terrainPosition.toXY());
                    if (rectangle != null) {
                        selectionHandler.selectRectangle(rectangle);
                    }
                    cockpitMode.setGroupSelectionFrame(null);
                } else {
                    logger.warning("TerrainMouseHandler.onMouseUp(): groupSelectionFrame != null");
                }
            }
        } catch (NoInterpolatedTerrainTriangleException e) {
            logger.warning(e.getMessage());
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

    private void executeMoveCommand(DecimalPosition position) {
        Group selection = selectionHandler.getOwnSelection();
        if (selection == null) {
            return;
        }

        if (!selection.canMove()) {
            return;
        }

        commandService.move(selection.getItems(), position);
    }
}
