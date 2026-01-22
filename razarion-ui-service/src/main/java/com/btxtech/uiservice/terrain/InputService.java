package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.uiservice.SelectionService;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
import com.btxtech.uiservice.renderer.ViewField;
import elemental2.promise.Promise;
import jsinterop.annotations.JsType;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@JsType
@Singleton
public class InputService {
    private final Logger logger = Logger.getLogger(InputService.class.getName());
    private final MapCollection<Index, Consumer<TerrainType>> terrainTypeOnTerrainConsumers = new MapCollection<>();
    private final TerrainUiService terrainUiService;
    private final BaseItemUiService baseItemUiService;
    private final ResourceUiService resourceUiService;
    private final BoxUiService boxUiService;
    private final InGameQuestVisualizationService inGameQuestVisualizationService;
    private final SelectionService selectionService;
    private final AudioService audioService;
    private final GameEngineControl gameEngineControl;
    private final ItemTypeService itemTypeService;
    private boolean hasPendingMoveCommand;
    private MoveCommandEntry queuedMoveCommandEntry;

    @Inject
    public InputService(ItemTypeService itemTypeService,
                        GameEngineControl gameEngineControl,
                        AudioService audioService,
                        SelectionService selectionService,
                        InGameQuestVisualizationService inGameQuestVisualizationService,
                        BoxUiService boxUiService,
                        ResourceUiService resourceUiService,
                        BaseItemUiService baseItemUiService,
                        TerrainUiService terrainUiService) {
        this.itemTypeService = itemTypeService;
        this.gameEngineControl = gameEngineControl;
        this.audioService = audioService;
        this.selectionService = selectionService;
        this.inGameQuestVisualizationService = inGameQuestVisualizationService;
        this.boxUiService = boxUiService;
        this.resourceUiService = resourceUiService;
        this.baseItemUiService = baseItemUiService;
        this.terrainUiService = terrainUiService;
    }

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
            if (selectionService.hasOwnSelection()) {
                if (syncBaseItem.checkBuildup() && baseItemType.getItemContainerType() != null) {
                    Collection<SyncBaseItemSimpleDto> contained = selectionService.getOwnSelection().getSyncBaseItemsMonitors().stream().filter(monitor -> baseItemType.getItemContainerType().isAbleToContain(monitor.getSyncBaseItemState().getSyncBaseItem().getItemTypeId())).map(monitor -> monitor.getSyncBaseItemState().getSyncBaseItem()).collect(Collectors.toList());
                    if (!contained.isEmpty()) {
                        audioService.onCommandSent();
                        gameEngineControl.loadContainerCmd(contained, syncBaseItem);
                        return;
                    }
                } else if (!syncBaseItem.checkBuildup()) {
                    Collection<SyncBaseItemSimpleDto> builders = selectionService.getOwnSelection().getBuilders(syncBaseItem.getItemTypeId());
                    if (!builders.isEmpty()) {
                        audioService.onCommandSent();
                        gameEngineControl.finalizeBuildCmd(builders, syncBaseItem);
                        return;
                    }
                }
            }
            selectionService.onBaseItemsSelected(Collections.singletonList(syncBaseItem));
        } catch (Throwable t) {
            logger.log(Level.WARNING, t.getMessage(), t);
        }
    }

    @SuppressWarnings("unused") // Called by Babylonjs
    public void friendItemClicked(int syncItemId) {
        try {
            SyncBaseItemSimpleDto syncBaseItem = baseItemUiService.getItem4Id(syncItemId);
            selectionService.onBaseItemsSelected(Collections.singletonList(syncBaseItem));
        } catch (Throwable t) {
            logger.log(Level.WARNING, t.getMessage(), t);
        }
    }

    @SuppressWarnings("unused") // Called by Babylonjs
    public void enemyItemClicked(int syncItemId) {
        try {
            SyncBaseItemSimpleDto syncBaseItem = baseItemUiService.getItem4Id(syncItemId);
            if (selectionService.hasOwnSelection()) {
                Collection<SyncBaseItemSimpleDto> attackers = selectionService.getOwnSelection().getAttackers(syncBaseItem.getItemTypeId());
                if (!attackers.isEmpty()) {
                    audioService.onCommandSent();
                    gameEngineControl.attackCmd(attackers, syncBaseItem);
                } else {
                    selectionService.onBaseItemsSelected(Collections.singletonList(syncBaseItem));
                }
            } else {
                selectionService.onBaseItemsSelected(Collections.singletonList(syncBaseItem));
            }
        } catch (Throwable t) {
            logger.log(Level.WARNING, t.getMessage(), t);
        }
    }

    @SuppressWarnings("unused") // Called by Babylonjs
    public void resourceItemClicked(int syncItemId) {
        try {
            SyncResourceItemSimpleDto syncResourceItem = resourceUiService.getItem4Id(syncItemId);
            if (selectionService.hasOwnSelection()) {
                Collection<SyncBaseItemSimpleDto> harvesters = selectionService.getOwnSelection().getHarvesters();
                if (!harvesters.isEmpty()) {
                    audioService.onCommandSent();
                    gameEngineControl.harvestCmd(harvesters, syncResourceItem);
                } else {
                    selectionService.setOtherItemSelected(syncResourceItem);
                }
            } else {
                selectionService.setOtherItemSelected(syncResourceItem);
            }
        } catch (Throwable t) {
            logger.log(Level.WARNING, t.getMessage(), t);
        }
    }

    @SuppressWarnings("unused") // Called by Babylonjs
    public void boxItemClicked(int syncItemId) {
        try {
            SyncBoxItemSimpleDto syncBoxItem = boxUiService.getItem4Id(syncItemId);
            if (selectionService.hasOwnSelection()) {
                Collection<SyncBaseItemSimpleDto> pickers = selectionService.getOwnSelection().getMovables();
                if (!pickers.isEmpty()) {
                    audioService.onCommandSent();
                    gameEngineControl.pickBoxCmd(pickers, syncBoxItem);
                } else {
                    selectionService.setOtherItemSelected(syncBoxItem);
                }
            } else {
                selectionService.setOtherItemSelected(syncBoxItem);
            }
        } catch (Throwable t) {
            logger.log(Level.WARNING, t.getMessage(), t);
        }
    }

    @SuppressWarnings("unused") // Called by Babylonjs
    public void terrainClicked(DecimalPosition terrainPosition) {
        try {
            if (!selectionService.hasOwnSelection()) {
                return;
            }
            Collection<SyncBaseItemSimpleDto> movables = selectionService.getOwnSelection().getMovables();
            movables = movables.stream().filter(syncBaseItemSimpleDto -> {
                TerrainType terrainType = itemTypeService.getBaseItemType(syncBaseItemSimpleDto.getItemTypeId()).getPhysicalAreaConfig().getTerrainType();
                return terrainUiService.isTerrainFree(terrainPosition, terrainType);
            }).collect(Collectors.toList());
            if (movables.isEmpty()) {
                return;
            }
            audioService.onCommandSent();

            if (hasPendingMoveCommand) {
                queuedMoveCommandEntry = new MoveCommandEntry(movables, terrainPosition);
            } else {
                gameEngineControl.moveCmd(movables, terrainPosition);
            }
            hasPendingMoveCommand = true;
        } catch (Throwable t) {
            logger.log(Level.WARNING, t.getMessage(), t);
        }
    }

    @SuppressWarnings("unused") // Called by Babylonjs
    public Promise<TerrainType> getTerrainTypeOnTerrain(Index nodeIndex) {
        return new Promise<>((resolve, reject) -> {
            boolean contains = terrainTypeOnTerrainConsumers.containsKey(nodeIndex);
            terrainTypeOnTerrainConsumers.put(nodeIndex, resolve::onInvoke);
            if (!contains) {
                gameEngineControl.getTerrainType(nodeIndex);
            }
        });
    }

    public void onGetTerrainTypeAnswer(Index nodeIndex, TerrainType terrainType) {
        Collection<Consumer<TerrainType>> consumers = terrainTypeOnTerrainConsumers.remove(nodeIndex);
        for (Consumer<TerrainType> consumer : consumers) {
            consumer.accept(terrainType);
        }
    }

    public void onMoveCommandAck() {
        if (!hasPendingMoveCommand) {
            return;
        }
        if (queuedMoveCommandEntry != null) {
            gameEngineControl.moveCmd(queuedMoveCommandEntry.getMovables(), queuedMoveCommandEntry.getTerrainPosition());
        } else {
            hasPendingMoveCommand = false;
        }
        queuedMoveCommandEntry = null;
    }
}
