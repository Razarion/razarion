package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.mouse.TerrainMouseHandler;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
import com.btxtech.uiservice.renderer.ViewField;
import jsinterop.annotations.JsType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@JsType
@ApplicationScoped
public class InputService {
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private ResourceUiService resourceUiService;
    @Inject
    private BoxUiService boxUiService;
    @Inject
    private TerrainMouseHandler terrainMouseHandler;
    @Inject
    private InGameQuestVisualizationService inGameQuestVisualizationService;
    @Inject
    private SelectionHandler selectionHandler;
    @Inject
    private AudioService audioService;
    @Inject
    private GameEngineControl gameEngineControl;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private ExceptionHandler exceptionHandler;

    @SuppressWarnings("unused") // Called by Angular
    public void onViewFieldChanged(double bottomLeftX, double bottomLeftY, double bottomRightX, double bottomRightY, double topRightX, double topRightY, double topLeftX, double topLeftY) {
        ViewField viewField = new ViewField(0)
                .bottomLeft(new DecimalPosition(bottomLeftX, bottomLeftY))
                .bottomRight(new DecimalPosition(bottomRightX, bottomRightY))
                .topRight(new DecimalPosition(topRightX, topRightY))
                .topLeft(new DecimalPosition(topLeftX, topLeftY));
        Rectangle2D viewFieldAabb = viewField.calculateAabbRectangle();
        terrainUiService.onViewChanged(viewField, viewFieldAabb);
        baseItemUiService.onViewChanged(viewField, viewFieldAabb);
        resourceUiService.onViewChanged(viewField, viewFieldAabb);
        boxUiService.onViewChanged(viewField, viewFieldAabb);
        inGameQuestVisualizationService.onViewChanged(viewField, viewFieldAabb);
    }

    @SuppressWarnings("unused") // Called by Babylonjs
    public void ownItemClicked(int syncItemId, BaseItemType baseItemType) {
        try {
            SyncBaseItemSimpleDto syncBaseItem = baseItemUiService.getItem4Id(syncItemId);
            if (selectionHandler.hasOwnSelection()) {
                if (syncBaseItem.checkBuildup() && baseItemType.getItemContainerType() != null) {
                    Collection<SyncBaseItemSimpleDto> contained = selectionHandler.getOwnSelection().getSyncBaseItemsMonitors().stream().filter(monitor -> baseItemType.getItemContainerType().isAbleToContain(monitor.getSyncBaseItemState().getSyncBaseItem().getItemTypeId())).map(monitor -> monitor.getSyncBaseItemState().getSyncBaseItem()).collect(Collectors.toList());
                    if (!contained.isEmpty()) {
                        audioService.onCommandSent();
                        gameEngineControl.loadContainerCmd(contained, syncBaseItem);
                        return;
                    }
                } else if (!syncBaseItem.checkBuildup()) {
                    Collection<SyncBaseItemSimpleDto> builders = selectionHandler.getOwnSelection().getBuilders(syncBaseItem.getItemTypeId());
                    if (!builders.isEmpty()) {
                        audioService.onCommandSent();
                        gameEngineControl.finalizeBuildCmd(builders, syncBaseItem);
                        return;
                    }
                }
            }
            selectionHandler.onBaseItemsSelected(Collections.singletonList(syncBaseItem));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @SuppressWarnings("unused") // Called by Babylonjs
    public void friendItemClicked(int syncItemId) {
        try {
            SyncBaseItemSimpleDto syncBaseItem = baseItemUiService.getItem4Id(syncItemId);
            selectionHandler.onBaseItemsSelected(Collections.singletonList(syncBaseItem));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @SuppressWarnings("unused") // Called by Babylonjs
    public void enemyItemClicked(int syncItemId) {
        try {
            SyncBaseItemSimpleDto syncBaseItem = baseItemUiService.getItem4Id(syncItemId);
            if (selectionHandler.hasOwnSelection()) {
                Collection<SyncBaseItemSimpleDto> attackers = selectionHandler.getOwnSelection().getAttackers(syncBaseItem.getItemTypeId());
                if (!attackers.isEmpty()) {
                    audioService.onCommandSent();
                    gameEngineControl.attackCmd(attackers, syncBaseItem);
                } else {
                    selectionHandler.onBaseItemsSelected(Collections.singletonList(syncBaseItem));
                }
            } else {
                selectionHandler.onBaseItemsSelected(Collections.singletonList(syncBaseItem));
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @SuppressWarnings("unused") // Called by Babylonjs
    public void resourceItemClicked(int syncItemId) {
        try {
            SyncResourceItemSimpleDto syncResourceItem = resourceUiService.getItem4Id(syncItemId);
            if (selectionHandler.hasOwnSelection()) {
                Collection<SyncBaseItemSimpleDto> harvesters = selectionHandler.getOwnSelection().getHarvesters();
                if (!harvesters.isEmpty()) {
                    audioService.onCommandSent();
                    gameEngineControl.harvestCmd(harvesters, syncResourceItem);
                } else {
                    selectionHandler.setOtherItemSelected(syncResourceItem);
                }
            } else {
                selectionHandler.setOtherItemSelected(syncResourceItem);
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @SuppressWarnings("unused") // Called by Babylonjs
    public void boxItemClicked(int syncItemId) {
        try {
            SyncBoxItemSimpleDto syncBoxItem = boxUiService.getItem4Id(syncItemId);
            if (selectionHandler.hasOwnSelection()) {
                Collection<SyncBaseItemSimpleDto> pickers = selectionHandler.getOwnSelection().getMovables();
                if (!pickers.isEmpty()) {
                    audioService.onCommandSent();
                    gameEngineControl.pickBoxCmd(pickers, syncBoxItem);
                } else {
                    selectionHandler.setOtherItemSelected(syncBoxItem);
                }
            } else {
                selectionHandler.setOtherItemSelected(syncBoxItem);
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @SuppressWarnings("unused") // Called by Babylonjs
    public void terrainClicked(DecimalPosition terrainPosition) {
        try {
            Collection<SyncBaseItemSimpleDto> movables = selectionHandler.getOwnSelection().getMovables();
            movables = movables.stream().filter(syncBaseItemSimpleDto -> {
                TerrainType terrainType = itemTypeService.getBaseItemType(syncBaseItemSimpleDto.getItemTypeId()).getPhysicalAreaConfig().getTerrainType();
                return terrainUiService.isTerrainFreeInDisplay(terrainPosition, terrainType);
            }).collect(Collectors.toList());
            if (movables.isEmpty()) {
                return;
            }
            audioService.onCommandSent();
            gameEngineControl.moveCmd(movables, terrainPosition);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @SuppressWarnings("unused") // Called by Babylonjs
    @Deprecated
    public void onMouseMove(int x, int y, int z, boolean primaryButtonDown) {
        terrainMouseHandler.onMouseMove(new Vertex(x, y, z), primaryButtonDown);
    }

    @SuppressWarnings("unused") // Called by Babylonjs
    @Deprecated
    public void onMouseDown(int x, int y, int z) {
        terrainMouseHandler.onMouseDown(new DecimalPosition(x, y));
    }

    @SuppressWarnings("unused") // Called by Babylonjs
    @Deprecated
    public void onMouseUp(int x, int y, int z) {
        terrainMouseHandler.onMouseUp(new Vertex(x, y, z));
    }


}
