package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.GameTipConfig;
import com.btxtech.shared.gameengine.InventoryService;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.uiservice.tip.GameTipService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * User: beat
 * Date: 22.08.12
 * Time: 13:27
 */
@ApplicationScoped
public class TipTaskFactory {
    @Inject
    private Instance<AbstractTipTask> tipTaskInstance;
    @Inject
    private InventoryService inventoryService;

    public TipTaskContainer create(GameTipService gameTipService, GameTipConfig gameTipConfig) {
        TipTaskContainer tipTaskContainer = new TipTaskContainer(gameTipService);
        switch (gameTipConfig.getTip()) {
            case BUILD: {
                createBuiltFactory(tipTaskContainer, gameTipConfig);
                break;
            }
            case FABRICATE: {
                createFactorizeUnit(tipTaskContainer, gameTipConfig);
                break;
            }
            case HARVEST: {
                createHarvest(tipTaskContainer, gameTipConfig);
                break;
            }
            case MOVE: {
                createMove(tipTaskContainer, gameTipConfig);
                break;
            }
            case ATTACK: {
                createAttack(tipTaskContainer, gameTipConfig);
                break;
            }
            case START_PLACER: {
                createStartPlacer(tipTaskContainer, gameTipConfig);
                break;
            }
            case PICK_BOX: {
                createPickBox(tipTaskContainer, gameTipConfig);
                break;
            }
            case SPAN_INVENTORY_ITEM: {
                createSpawnInventoryItem(tipTaskContainer, gameTipConfig);
                break;
            }
            case SCROLL: {
                createScrollTipTask(tipTaskContainer, gameTipConfig);
                break;
            }
            default:
                throw new IllegalArgumentException("TipTaskFactory: unknown tip: " + gameTipConfig.getTip());
        }
        return tipTaskContainer;
    }

    private void createBuiltFactory(TipTaskContainer tipTaskContainer, GameTipConfig gameTipConfig) {
        tipTaskContainer.add(createSelectTipTask(gameTipConfig.getActor()));
        tipTaskContainer.add(createToBeBuildPlacerTipTask(gameTipConfig.getToCreatedItemTypeId()));
        tipTaskContainer.add(createSendBuildCommandTipTask(gameTipConfig.getToCreatedItemTypeId(), gameTipConfig.getTerrainPositionHint()));
        tipTaskContainer.add(createIdleItemTipTask(gameTipConfig.getActor()));
    }

    private void createFactorizeUnit(TipTaskContainer tipTaskContainer, GameTipConfig gameTipConfig) {
        tipTaskContainer.add(createSelectTipTask(gameTipConfig.getActor()));
        tipTaskContainer.add(createSendFactorizeCommandTipTask(gameTipConfig.getToCreatedItemTypeId()));
        tipTaskContainer.add(createIdleItemTipTask(gameTipConfig.getActor()));
    }

    private void createHarvest(TipTaskContainer tipTaskContainer, GameTipConfig gameTipConfig) {
        tipTaskContainer.add(createSelectTipTask(gameTipConfig.getActor()));
        tipTaskContainer.add(createSendHarvestCommandTipTask(gameTipConfig.getResourceItemTypeId(), gameTipConfig.getPlaceConfig()));
        tipTaskContainer.add(createIdleItemTipTask(gameTipConfig.getActor()));
    }

    private void createMove(TipTaskContainer tipTaskContainer, GameTipConfig gameTipConfig) {
        tipTaskContainer.add(createSelectTipTask(gameTipConfig.getActor()));
        tipTaskContainer.add(createSendMoveCommandTipTask(gameTipConfig.getTerrainPositionHint()));
        tipTaskContainer.add(createIdleItemTipTask(gameTipConfig.getActor()));
    }

    private void createAttack(TipTaskContainer tipTaskContainer, GameTipConfig gameTipConfig) {
        tipTaskContainer.add(createSelectTipTask(gameTipConfig.getActor()));
        tipTaskContainer.add(createSendAttackCommandTipTask(null, gameTipConfig.getPlaceConfig()));
        tipTaskContainer.add(createIdleItemTipTask(gameTipConfig.getActor()));
    }

    private void createStartPlacer(TipTaskContainer tipTaskContainer, GameTipConfig gameTipConfig) {
        tipTaskContainer.add(createSpawnPlacerTipTask(gameTipConfig.getToCreatedItemTypeId(), gameTipConfig.getTerrainPositionHint()));
    }

    private void createPickBox(TipTaskContainer tipTaskContainer, GameTipConfig gameTipConfig) {
        tipTaskContainer.add(createSelectTipTask(gameTipConfig.getActor()));
        tipTaskContainer.add(createSendPickupBoxCommandTipTask(gameTipConfig.getBoxItemTypeId()));
        tipTaskContainer.add(createIdleItemTipTask(gameTipConfig.getActor()));
    }

    private void createSpawnInventoryItem(TipTaskContainer tipTaskContainer, GameTipConfig gameTipConfig) {
        tipTaskContainer.add(tipTaskInstance.select(OpenInventoryTipTask.class).get());
        tipTaskContainer.add(createUseInventoryItemTipTask(gameTipConfig.getInventoryItemId()));
        tipTaskContainer.add(createSpawnPlacerTipTask(inventoryService.getInventoryItem(gameTipConfig.getInventoryItemId()).getBaseItemTypeId(), gameTipConfig.getTerrainPositionHint()));
    }

    private void createScrollTipTask(TipTaskContainer tipTaskContainer, GameTipConfig gameTipConfig) {
        ScrollTipTask scrollTipTask = tipTaskInstance.select(ScrollTipTask.class).get();
        scrollTipTask.init(gameTipConfig.getTerrainPositionHint());
        tipTaskContainer.add(scrollTipTask);
    }

    private SelectTipTask createSelectTipTask(int itemTypeId) {
        SelectTipTask selectTipTask = tipTaskInstance.select(SelectTipTask.class).get();
        selectTipTask.init(itemTypeId);
        return selectTipTask;
    }

    private SendMoveCommandTipTask createSendMoveCommandTipTask(DecimalPosition position) {
        SendMoveCommandTipTask sendMoveCommandTipTask = tipTaskInstance.select(SendMoveCommandTipTask.class).get();
        sendMoveCommandTipTask.init(position);
        return sendMoveCommandTipTask;
    }

    private IdleItemTipTask createIdleItemTipTask(int actorItemTypeId) {
        IdleItemTipTask idleItemTipTask = tipTaskInstance.select(IdleItemTipTask.class).get();
        idleItemTipTask.init(actorItemTypeId);
        return idleItemTipTask;
    }

    private ToBeBuildPlacerTipTask createToBeBuildPlacerTipTask(int itemTypeToBePlaced) {
        ToBeBuildPlacerTipTask toBeBuildPlacerTipTask = tipTaskInstance.select(ToBeBuildPlacerTipTask.class).get();
        toBeBuildPlacerTipTask.init(itemTypeToBePlaced);
        return toBeBuildPlacerTipTask;
    }

    private SendBuildCommandTipTask createSendBuildCommandTipTask(int toBeBuildId, DecimalPosition positionHint) {
        SendBuildCommandTipTask sendBuildCommandTipTask = tipTaskInstance.select(SendBuildCommandTipTask.class).get();
        sendBuildCommandTipTask.init(toBeBuildId, positionHint);
        return sendBuildCommandTipTask;
    }

    private SendFactorizeCommandTipTask createSendFactorizeCommandTipTask(int itemTypeToFactorized) {
        SendFactorizeCommandTipTask sendFactorizeCommandTipTask = tipTaskInstance.select(SendFactorizeCommandTipTask.class).get();
        sendFactorizeCommandTipTask.init(itemTypeToFactorized);
        return sendFactorizeCommandTipTask;
    }

    private SendHarvestCommandTipTask createSendHarvestCommandTipTask(int toCollectFormId, PlaceConfig resourceSelection) {
        SendHarvestCommandTipTask sendHarvestCommandTipTask = tipTaskInstance.select(SendHarvestCommandTipTask.class).get();
        sendHarvestCommandTipTask.init(toCollectFormId, resourceSelection);
        return sendHarvestCommandTipTask;
    }

    private SendAttackCommandTipTask createSendAttackCommandTipTask(Integer targetItemTypeId, PlaceConfig placeConfig) {
        SendAttackCommandTipTask sendAttackCommandTipTask = tipTaskInstance.select(SendAttackCommandTipTask.class).get();
        sendAttackCommandTipTask.init(targetItemTypeId, placeConfig);
        return sendAttackCommandTipTask;
    }

    private SpawnPlacerTipTask createSpawnPlacerTipTask(int spawnItemTypeId, DecimalPosition positionHint) {
        SpawnPlacerTipTask spawnPlacerTipTask = tipTaskInstance.select(SpawnPlacerTipTask.class).get();
        spawnPlacerTipTask.init(spawnItemTypeId, positionHint);
        return spawnPlacerTipTask;
    }

    private AbstractTipTask createSendPickupBoxCommandTipTask(int boxItemTypeId) {
        SendPickupBoxCommandTipTask sendPickupBoxCommandTipTask = tipTaskInstance.select(SendPickupBoxCommandTipTask.class).get();
        sendPickupBoxCommandTipTask.init(boxItemTypeId);
        return sendPickupBoxCommandTipTask;
    }

    private UseInventoryItemTipTask createUseInventoryItemTipTask(int inventoryItemId) {
        UseInventoryItemTipTask useInventoryItemTipTask = tipTaskInstance.select(UseInventoryItemTipTask.class).get();
        useInventoryItemTipTask.init(inventoryItemId);
        return useInventoryItemTipTask;
    }
}