package com.btxtech.uiservice.mouse;

import com.btxtech.shared.datatypes.DecimalPosition;
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
import com.btxtech.uiservice.renderer.task.selection.SelectionFrameRenderTaskRunner;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 12.07.2015.
 */
@ApplicationScoped
public class TerrainMouseHandler {
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private CursorService cursorService;
    @Inject
    private BaseItemPlacerService baseItemPlacerService;
    @Inject
    private SelectionHandler selectionHandler;
    @Inject
    private GameEngineControl gameEngineControl;
    @Inject
    private CockpitMode cockpitMode;
    @Inject
    private SelectionFrameRenderTaskRunner selectionFrameRenderTaskRunner;
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
    private GroupSelectionFrame groupSelectionFrame;
    // DOTO private EditorMouseListener editorMouseListener;

    public void clear() {
        groupSelectionFrame = null;
    }

    public void onMouseMove(DecimalPosition terrainPosition, boolean primaryButtonDown) {
        try {
            if (baseItemPlacerService.isActive()) {
                baseItemPlacerService.onMouseMoveEvent(terrainPosition);
                return;
            }

            if (primaryButtonDown) {
                if (groupSelectionFrame != null) {
                    groupSelectionFrame.onMove(terrainPosition);
                    selectionFrameRenderTaskRunner.onMove(groupSelectionFrame);
                }
            } else {
                SyncBaseItemSimpleDto syncBaseItem = baseItemUiService.findItemAtPosition(terrainPosition);
                if (syncBaseItem != null) {
                    cursorService.handleMouseOverBaseItem(syncBaseItem);
                    baseItemUiService.onHover(syncBaseItem);
                    return;
                }
                SyncResourceItemSimpleDto syncResourceItem = resourceUiService.findItemAtPosition(terrainPosition);
                if (syncResourceItem != null) {
                    cursorService.handleMouseOverResourceItem();
                    resourceUiService.onHover(syncResourceItem);
                    return;
                }
                SyncBoxItemSimpleDto syncBoxItem = boxUiService.findItemAtPosition(terrainPosition);
                if (syncBoxItem != null) {
                    cursorService.handleMouseOverBoxItem();
                    baseItemUiService.onHover(syncBoxItem);
                    boxUiService.onHover(syncBoxItem);
                    return;
                }
                cursorService.handleMouseOverTerrain(terrainPosition);
                baseItemUiService.onHover(null);
                resourceUiService.onHover(null);
                boxUiService.onHover(null);
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void onMouseDown(DecimalPosition terrainPosition) {
        try {
// TODO           if (editorMouseListener != null) {
//                editorMouseListener.onMouseDown(terrainPosition);
//                return;
//            }

            if (baseItemPlacerService.isActive()) {
                return;
            }

            groupSelectionFrame = new GroupSelectionFrame(terrainPosition);
            selectionFrameRenderTaskRunner.startGroupSelection(groupSelectionFrame);
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

    public void onMouseUp(DecimalPosition terrainPosition) {
        try {
// TODO           if (editorMouseListener != null) {
//                editorMouseListener.onMouseUp();
//                return;
//            }

            if (baseItemPlacerService.isActive()) {
                baseItemPlacerService.onMouseUpEvent(terrainPosition);
                return;
            }

            boolean onlySelectionFrame = false;
            if (groupSelectionFrame != null) {
                selectionFrameRenderTaskRunner.stop();
                groupSelectionFrame.onMove(terrainPosition);
                if (groupSelectionFrame.getRectangle2D() != null) {
                    onlySelectionFrame = true;
                    selectionHandler.selectRectangle(groupSelectionFrame.getRectangle2D());
                } else {
                    if (isSelectionChangeNeeded(terrainPosition)) {
                        selectionHandler.selectPosition(groupSelectionFrame.getStart2D());
                    }
                }
                groupSelectionFrame = null;
            }

            if (!onlySelectionFrame && selectionHandler.hasOwnSelection()) {
                mouseUpWithOwnSelection(terrainPosition);
            }

        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void setEditorMouseListener(EditorMouseListener editorMouseListener) {
        // TODO this.editorMouseListener = editorMouseListener;
    }

    private Vertex setupTerrainPosition(int x, int y) {
        return new Vertex(x, y, 0);
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
            // logger.severe("(REMOVE THIS COMMENT) MOVE: terrainPosition: " + terrainPosition);
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
