package com.btxtech.uiservice.mouse;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line3d;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.gameengine.planet.terrain.NoInterpolatedTerrainTriangleException;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.Group;
import com.btxtech.uiservice.GroupSelectionFrame;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.TerrainEditor;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.cockpit.CockpitMode;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.task.selection.SelectionFrameRenderTask;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
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
    private TerrainUiService terrainUiService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private CursorService cursorService;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @Inject
    private BaseItemPlacerService baseItemPlacerService;
    @Inject
    private SelectionHandler selectionHandler;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private GameEngineControl gameEngineControl;
    @Inject
    private ItemCockpitService itemCockpitService;
    @Inject
    private CockpitMode cockpitMode;
    @Inject
    private SelectionFrameRenderTask renderTask;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private ResourceUiService resourceUiService;
    @Inject
    private BoxUiService boxUiService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private AudioService audioService;
    private GroupSelectionFrame groupSelectionFrame;
    private TerrainEditor terrainEditor;

    public void onMouseMove(int x, int y, int width, int height, boolean primaryButtonDown) {
        try {
            terrainScrollHandler.handleMouseMoveScroll(x, y, width, height);

            Vertex terrainPosition = setupTerrainPosition(x, y, width, height);
            if (terrainEditor != null) {
                terrainEditor.onMouseMove(terrainPosition);
                return;
            }

            if (baseItemPlacerService.isActive()) {
                baseItemPlacerService.onMouseMoveEvent(terrainPosition);
                return;
            }

            if (primaryButtonDown) {
                if (groupSelectionFrame != null) {
                    groupSelectionFrame.onMove(terrainPosition.toXY());
                    renderTask.onMove(groupSelectionFrame);
                } else {
                    logger.warning("TerrainMouseHandler.onMouseMove(): groupSelectionFrame != null");
                }
            } else {
                SyncBaseItemSimpleDto syncBaseItem = baseItemUiService.findItemAtPosition(terrainPosition.toXY());
                if (syncBaseItem != null) {
                    cursorService.handleMouseOverBaseItem(syncBaseItem, terrainPosition.toXY());
                    return;
                }
                SyncResourceItemSimpleDto syncResourceItem = resourceUiService.findItemAtPosition(terrainPosition.toXY());
                if (syncResourceItem != null) {
                    cursorService.handleMouseOverResourceItem();
                    return;
                }
                SyncBoxItemSimpleDto syncBoxItem = boxUiService.findItemAtPosition(terrainPosition.toXY());
                if (syncBoxItem != null) {
                    cursorService.handleMouseOverBoxItem();
                    return;
                }
                cursorService.handleMouseOverTerrain(terrainPosition.toXY());
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
            Vertex terrainPosition = setupTerrainPosition(x, y, width, height);
            if (shiftKey) {
                logger.severe("Terrain Position: " + terrainPosition);
            }
            if (terrainEditor != null) {
                terrainEditor.onMouseDown(terrainPosition);
                return;
            }

            if (baseItemPlacerService.isActive()) {
                if (primaryButtonPressed) {
                    baseItemPlacerService.onMouseDownEvent(terrainPosition);
                }
                return;
            }

            if (primaryButtonPressed) {
                groupSelectionFrame = new GroupSelectionFrame(terrainPosition.toXY());
                renderTask.startGroupSelection(groupSelectionFrame);
            } else if (secondaryButtonPressed) {
                if (!selectionHandler.hasOwnSelection()) {
                    return;
                }
                SyncBaseItemSimpleDto syncBaseItem = baseItemUiService.findItemAtPosition(terrainPosition.toXY());
                if (syncBaseItem != null) {
                    if (baseItemUiService.isMyOwnProperty(syncBaseItem)) {
                        if (!syncBaseItem.checkBuildup()) {
                            Collection<SyncBaseItemSimpleDto> builders = selectionHandler.getOwnSelection().getBuilders(syncBaseItem.getItemTypeId());
                            if (!builders.isEmpty()) {
                                audioService.onCommandSent();
                                gameEngineControl.finalizeBuildCmd(builders, syncBaseItem);
                            }
                        }
                    } else {
                        if (baseItemUiService.isEnemy(syncBaseItem)) {
                            Collection<SyncBaseItemSimpleDto> attackers = selectionHandler.getOwnSelection().getAttackers(syncBaseItem);
                            if (!attackers.isEmpty()) {
                                audioService.onCommandSent();
                                gameEngineControl.attackCmd(attackers, syncBaseItem);
                            }
                        }
                    }
                    return;
                }
                SyncResourceItemSimpleDto syncResourceItem = resourceUiService.findItemAtPosition(terrainPosition.toXY());
                if (syncResourceItem != null) {
                    Collection<SyncBaseItemSimpleDto> harvesters = selectionHandler.getOwnSelection().getHarvesters();
                    if (!harvesters.isEmpty()) {
                        audioService.onCommandSent();
                        gameEngineControl.harvestCmd(harvesters, syncResourceItem);
                    }
                    return;
                }
                SyncBoxItemSimpleDto syncBoxItem = boxUiService.findItemAtPosition(terrainPosition.toXY());
                if (syncBoxItem != null) {
                    Collection<SyncBaseItemSimpleDto> pickers = selectionHandler.getOwnSelection().getMovables();
                    if (!pickers.isEmpty()) {
                        audioService.onCommandSent();
                        gameEngineControl.pickBoxCmd(pickers, syncBoxItem);
                    }
                    return;
                }
                executeMoveCommand(terrainPosition.toXY());
            }
        } catch (NoInterpolatedTerrainTriangleException e) {
            logger.warning(e.getMessage());
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void onMouseUp(int x, int y, int width, int height, boolean primaryButtonReleased) {
        try {
            Vertex terrainPosition = setupTerrainPosition(x, y, width, height);

            if (terrainEditor != null) {
                terrainEditor.onMouseUp();
                return;
            }

            if (primaryButtonReleased) {
                if (groupSelectionFrame != null) {
                    renderTask.stop();
                    groupSelectionFrame.onMove(terrainPosition.toXY());
                    if (groupSelectionFrame.getRectangle() != null) {
                        selectionHandler.selectRectangle(groupSelectionFrame.getRectangle());
                    } else {
                        selectionHandler.selectPosition(groupSelectionFrame.getStart());
                    }
                    groupSelectionFrame = null;
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
            projectionTransformation.setConstrainedFovY(projectionTransformation.getFovY() - Math.toRadians(deltaY) / MOUSE_WHEEL_DIVIDER);
            terrainScrollHandler.updateViewField();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void setTerrainEditor(TerrainEditor terrainEditor) {
        this.terrainEditor = terrainEditor;
    }

    private Vertex setupTerrainPosition(int x, int y, int width, int height) {
        DecimalPosition webglClipPosition = new DecimalPosition((double) x / (double) width, 1.0 - (double) y / (double) height);
        webglClipPosition = webglClipPosition.multiply(2.0).sub(1, 1);
        Line3d pickRay = projectionTransformation.createPickRay(webglClipPosition);
        Line3d worldPickRay = camera.toWorld(pickRay);

        return terrainUiService.calculatePositionGroundMesh(worldPickRay);
    }

    private void executeMoveCommand(DecimalPosition position) {
        Group selection = selectionHandler.getOwnSelection();
        if (selection == null) {
            return;
        }

        Collection<SyncBaseItemSimpleDto> movables = selection.getMovables();
        if (movables.isEmpty()) {
            return;
        }

        audioService.onCommandSent();
        gameEngineControl.moveCmd(movables, position);
    }
}
