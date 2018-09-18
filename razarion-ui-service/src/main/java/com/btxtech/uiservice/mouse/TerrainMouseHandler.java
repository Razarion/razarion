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
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.EditorMouseListener;
import com.btxtech.uiservice.Group;
import com.btxtech.uiservice.GroupSelectionFrame;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.cockpit.CockpitMode;
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

    public void onMouseDown(int x, int y, int width, int height, boolean shiftKey) {
        try {
            Vertex terrainPosition = setupTerrainPosition(x, y, width, height);
            if (terrainPosition == null) {
                return;
            }

            if (shiftKey) {
                logger.severe("Terrain Position: " + terrainPosition);
            }
            if (editorMouseListener != null) {
                editorMouseListener.onMouseDown(terrainPosition);
                return;
            }

            if (baseItemPlacerService.isActive()) {
                return;
            }

            groupSelectionFrame = new GroupSelectionFrame(terrainPosition);
            renderTask.startGroupSelection(groupSelectionFrame);
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

    public void onMouseUp(int x, int y, int width, int height) {
        try {
            Vertex terrainPosition = setupTerrainPosition(x, y, width, height);
            if (terrainPosition == null) {
                return;
            }

            if (editorMouseListener != null) {
                editorMouseListener.onMouseUp();
                return;
            }

            if (baseItemPlacerService.isActive()) {
                baseItemPlacerService.onMouseUpEvent(terrainPosition);
                return;
            }

            boolean onlySelectionFrame = false;
            if (groupSelectionFrame != null) {
                renderTask.stop();
                groupSelectionFrame.onMove(terrainPosition);
                if (groupSelectionFrame.getRectangle2D() != null) {
                    onlySelectionFrame = true;
                    selectionHandler.selectRectangle(groupSelectionFrame.getRectangle2D());
                } else {
                    if (isSelectionChangeNeeded(terrainPosition.toXY())) {
                        selectionHandler.selectPosition(groupSelectionFrame.getStart2D());
                    }
                }
                groupSelectionFrame = null;
            }

            if (!onlySelectionFrame && selectionHandler.hasOwnSelection()) {
                mouseUpWithOwnSelection(terrainPosition.toXY());
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
        movables = movables.stream().filter(syncBaseItemSimpleDto -> {
            TerrainType terrainType = itemTypeService.getBaseItemType(syncBaseItemSimpleDto.getItemTypeId()).getPhysicalAreaConfig().getTerrainType();
            return terrainUiService.isTerrainFreeInDisplay(position, terrainType);
        }).collect(Collectors.toList());
        if (movables.isEmpty()) {
            return;
        }
        audioService.onCommandSent();
        gameEngineControl.moveCmd(movables, position);
    }

    private void mouseUpWithOwnSelection(DecimalPosition terrainPosition) {
        SyncBaseItemSimpleDto syncBaseItem = baseItemUiService.findItemAtPosition(terrainPosition);
        if (syncBaseItem != null) {
            if (baseItemUiService.isMyOwnProperty(syncBaseItem)) {
                BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
                if (syncBaseItem.checkBuildup() && baseItemType.getItemContainerType() != null) {
                    Collection<SyncBaseItemSimpleDto> contained = selectionHandler.getOwnSelection().getSyncBaseItemsMonitors().stream().filter(monitor -> baseItemType.getItemContainerType().isAbleToContain(monitor.getSyncBaseItemState().getSyncBaseItem().getItemTypeId())).map(monitor -> monitor.getSyncBaseItemState().getSyncBaseItem()).collect(Collectors.toList());
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
        SyncResourceItemSimpleDto syncResourceItem = resourceUiService.findItemAtPosition(terrainPosition);
        if (syncResourceItem != null) {
            Collection<SyncBaseItemSimpleDto> harvesters = selectionHandler.getOwnSelection().getHarvesters();
            if (!harvesters.isEmpty()) {
                audioService.onCommandSent();
                gameEngineControl.harvestCmd(harvesters, syncResourceItem);
            }
            return;
        }
        SyncBoxItemSimpleDto syncBoxItem = boxUiService.findItemAtPosition(terrainPosition);
        if (syncBoxItem != null) {
            Collection<SyncBaseItemSimpleDto> pickers = selectionHandler.getOwnSelection().getMovables();
            if (!pickers.isEmpty()) {
                audioService.onCommandSent();
                gameEngineControl.pickBoxCmd(pickers, syncBoxItem);
            }
            return;
        }
        // Terrain
        if (cockpitMode.getMode() == CockpitMode.Mode.UNLOAD) {
            SyncBaseItemSimpleDto container = findOneAllowedToUnload(selectionHandler.getOwnSelection().getItems(), terrainPosition);
            if (container != null) {
                cockpitMode.clear();
                audioService.onCommandSent();
                gameEngineControl.unloadContainerCmd(container, terrainPosition);
            }
        } else {
            logger.severe("(REMOVE THIS COMMENT) MOVE: terrainPosition: " + terrainPosition);
            executeMoveCommand(selectionHandler.getOwnSelection(), terrainPosition);
        }
    }

    private boolean isSelectionChangeNeeded(DecimalPosition terrainPosition) {
        if (!selectionHandler.hasOwnSelection()) {
            return true;
        }
        SyncBaseItemSimpleDto syncBaseItem = baseItemUiService.findItemAtPosition(terrainPosition);
        if (syncBaseItem != null) {
            if (baseItemUiService.isMyOwnProperty(syncBaseItem)) {
                BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
                if (syncBaseItem.checkBuildup() && baseItemType.getItemContainerType() != null) {
                    return false;
                } else if (!syncBaseItem.checkBuildup()) {
                    Collection<SyncBaseItemSimpleDto> builders = selectionHandler.getOwnSelection().getBuilders(syncBaseItem.getItemTypeId());
                    return builders.isEmpty();
                }
            } else {
                if (baseItemUiService.isMyEnemy(syncBaseItem)) {
                    Collection<SyncBaseItemSimpleDto> attackers = selectionHandler.getOwnSelection().getAttackers(syncBaseItem);
                    return attackers.isEmpty();
                }
            }
            return true;
        }
        SyncResourceItemSimpleDto syncResourceItem = resourceUiService.findItemAtPosition(terrainPosition);
        if (syncResourceItem != null) {
            Collection<SyncBaseItemSimpleDto> harvesters = selectionHandler.getOwnSelection().getHarvesters();
            return harvesters.isEmpty();
        }
        SyncBoxItemSimpleDto syncBoxItem = boxUiService.findItemAtPosition(terrainPosition);
        if (syncBoxItem != null) {
            Collection<SyncBaseItemSimpleDto> pickers = selectionHandler.getOwnSelection().getMovables();
            return pickers.isEmpty();
        }
        return true;
    }
}
