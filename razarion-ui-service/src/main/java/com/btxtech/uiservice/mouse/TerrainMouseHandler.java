package com.btxtech.uiservice.mouse;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line3d;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemContainerType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.gameengine.planet.model.SyncItemContainer;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.EditorMouseListener;
import com.btxtech.uiservice.Group;
import com.btxtech.uiservice.GroupSelectionFrame;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.cockpit.CockpitMode;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ItemMarkerService;
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
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 12.07.2015.
 */
@ApplicationScoped
public class TerrainMouseHandler {
    private static final double FOV_Y_STEP = Math.toRadians(4);
    private Logger logger = Logger.getLogger(TerrainMouseHandler.class.getName());
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private CursorService cursorService;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @Inject
    private BaseItemPlacerService baseItemPlacerService;
    @Inject
    private SelectionHandler selectionHandler;
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
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private AudioService audioService;
    @Inject
    private ItemMarkerService itemMarkerService;
    private GroupSelectionFrame groupSelectionFrame;
    private EditorMouseListener editorMouseListener;

    public void clear() {
        groupSelectionFrame = null;
    }

    public void onMouseMove(int x, int y, int width, int height, boolean primaryButtonDown) {
        try {
            terrainScrollHandler.handleMouseMoveScroll(x, y, width, height);
            Vertex terrainPosition = setupTerrainPosition(x, y, width, height);
            if (terrainPosition == null) {
                return;
            }
            if (editorMouseListener != null) {
                editorMouseListener.onMouseMove(terrainPosition, primaryButtonDown);
                return;
            }

            if (baseItemPlacerService.isActive()) {
                baseItemPlacerService.onMouseMoveEvent(terrainPosition);
                return;
            }

            if (primaryButtonDown) {
                if (groupSelectionFrame != null) {
                    groupSelectionFrame.onMove(terrainPosition);
                    renderTask.onMove(groupSelectionFrame);
                } else {
                    logger.warning("TerrainMouseHandler.onMouseMove(): groupSelectionFrame != null");
                }
            } else {
                SyncBaseItemSimpleDto syncBaseItem = baseItemUiService.findItemAtPosition(terrainPosition.toXY());
                if (syncBaseItem != null) {
                    cursorService.handleMouseOverBaseItem(syncBaseItem);
                    itemMarkerService.onHover(syncBaseItem);
                    return;
                }
                SyncResourceItemSimpleDto syncResourceItem = resourceUiService.findItemAtPosition(terrainPosition.toXY());
                if (syncResourceItem != null) {
                    cursorService.handleMouseOverResourceItem();
                    itemMarkerService.onHover(syncResourceItem);
                    return;
                }
                SyncBoxItemSimpleDto syncBoxItem = boxUiService.findItemAtPosition(terrainPosition.toXY());
                if (syncBoxItem != null) {
                    cursorService.handleMouseOverBoxItem();
                    itemMarkerService.onHover(syncBoxItem);
                    return;
                }
                cursorService.handleMouseOverTerrain(terrainPosition.toXY());
                itemMarkerService.onHover(null);
            }
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
            if (terrainPosition == null) {
                return;
            }

            System.out.println("terrainPosition: " + terrainPosition);
            if (shiftKey) {
                logger.severe("Terrain Position: " + terrainPosition);
            }
            if (editorMouseListener != null) {
                editorMouseListener.onMouseDown(terrainPosition);
                return;
            }

            if (baseItemPlacerService.isActive()) {
                if (primaryButtonPressed) {
                    baseItemPlacerService.onMouseDownEvent(terrainPosition);
                }
                return;
            }

            if (primaryButtonPressed) {
                groupSelectionFrame = new GroupSelectionFrame(terrainPosition);
                renderTask.startGroupSelection(groupSelectionFrame);
            } else if (secondaryButtonPressed) {
                if (!selectionHandler.hasOwnSelection()) {
                    return;
                }
                SyncBaseItemSimpleDto syncBaseItem = baseItemUiService.findItemAtPosition(terrainPosition.toXY());
                if (syncBaseItem != null) {
                    if (baseItemUiService.isMyOwnProperty(syncBaseItem)) {
                        BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
                        if (syncBaseItem.checkBuildup() && baseItemType.getItemContainerType() != null && selectionHandler.hasOwnSelection()) {
                            Collection<SyncBaseItemSimpleDto> contained = selectionHandler.getOwnSelection().getItems().stream().filter(syncBaseItemSimpleDto -> baseItemType.getItemContainerType().isAbleToContain(syncBaseItemSimpleDto.getItemTypeId())).collect(Collectors.toList());
                            if (!contained.isEmpty()) {
                                audioService.onCommandSent();
                                gameEngineControl.loadContainerCmd(contained, syncBaseItem);
                            }
                        } else if (!syncBaseItem.checkBuildup()) {
                            Collection<SyncBaseItemSimpleDto> builders = selectionHandler.getOwnSelection().getBuilders(syncBaseItem.getItemTypeId());
                            if (!builders.isEmpty()) {
                                audioService.onCommandSent();
                                gameEngineControl.finalizeBuildCmd(builders, syncBaseItem);
                            }
                        }
                    } else {
                        if (baseItemUiService.isMyEnemy(syncBaseItem)) {
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
                // Terrain
                Group selection = selectionHandler.getOwnSelection();
                if (selection == null) {
                    return;
                }

                SyncBaseItemSimpleDto container = findOneAllowedToUnload(selection.getItems(), terrainPosition.toXY());
                if (container != null && cockpitMode.getMode() == CockpitMode.Mode.UNLOAD) {
                    cockpitMode.clear();
                    audioService.onCommandSent();
                    gameEngineControl.unloadContainerCmd(container, terrainPosition.toXY());
                } else {
                    executeMoveCommand(selection, terrainPosition.toXY());
                }
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    private SyncBaseItemSimpleDto findOneAllowedToUnload(Collection<SyncBaseItemSimpleDto> items, DecimalPosition unloadPosition) {
        for (SyncBaseItemSimpleDto item : items) {
            if (item.getContainingItemCount() > 0) {
                BaseItemType baseItemType = itemTypeService.getBaseItemType(item.getItemTypeId());
                ItemContainerType itemContainerType = baseItemType.getItemContainerType();
                if (item.getPosition2d().getDistance(unloadPosition) - baseItemType.getPhysicalAreaConfig().getRadius() <= itemContainerType.getRange()) {
                    if (terrainUiService.isTerrainFreeInDisplay(unloadPosition, item.getMaxContainingRadius(), SyncItemContainer.DEFAULT_UNLOAD_TERRAIN_TYPE)) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    public void onMouseUp(int x, int y, int width, int height, boolean primaryButtonReleased) {
        try {
            Vertex terrainPosition = setupTerrainPosition(x, y, width, height);
            if (terrainPosition == null) {
                return;
            }

            if (editorMouseListener != null) {
                editorMouseListener.onMouseUp();
                return;
            }

            if (primaryButtonReleased) {
                if (groupSelectionFrame != null) {
                    renderTask.stop();
                    groupSelectionFrame.onMove(terrainPosition);
                    if (groupSelectionFrame.getRectangle2D() != null) {
                        selectionHandler.selectRectangle(groupSelectionFrame.getRectangle2D());
                    } else {
                        selectionHandler.selectPosition(groupSelectionFrame.getStart2D());
                    }
                    groupSelectionFrame = null;
                } else {
                    logger.warning("TerrainMouseHandler.onMouseUp(): groupSelectionFrame != null");
                }
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void onMouseWheel(double wheelDeltaY) {
        try {
            // Chrome and Firefox do have different deltas
            double fovYStep;
            if (wheelDeltaY < 0) {
                fovYStep = FOV_Y_STEP;
            } else {
                fovYStep = -FOV_Y_STEP;
            }
            projectionTransformation.setFovYSave(projectionTransformation.getFovY() - fovYStep);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void setEditorMouseListener(EditorMouseListener editorMouseListener) {
        this.editorMouseListener = editorMouseListener;
    }

    private Vertex setupTerrainPosition(int x, int y, int width, int height) {
        DecimalPosition webglClipPosition = new DecimalPosition((double) x / (double) width, 1.0 - (double) y / (double) height);
        webglClipPosition = webglClipPosition.multiply(2.0).sub(1, 1);
        Line3d pickRay = projectionTransformation.createPickRay(webglClipPosition);
        Line3d worldPickRay = camera.toWorld(pickRay);
        return terrainUiService.calculateMousePositionGroundMesh(worldPickRay);
    }

    private void executeMoveCommand(Group selection, DecimalPosition position) {
        Collection<SyncBaseItemSimpleDto> movables = selection.getMovables();
        if (movables.isEmpty()) {
            return;
        }

        audioService.onCommandSent();
        gameEngineControl.moveCmd(movables, position);
    }
}
