package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.GameTipConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.uiservice.storyboard.StoryboardService;
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
    private StoryboardService storyboardService;

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
            default:
                throw new IllegalArgumentException("TipTaskFactory: unknown tip: " + gameTipConfig.getTip());
        }
        return tipTaskContainer;
    }

    private void createBuiltFactory(TipTaskContainer tipTaskContainer, GameTipConfig gameTipConfig) {
        tipTaskContainer.add(createSelectTipTask(gameTipConfig.getActor()));
        tipTaskContainer.add(createToBeBuildPlacerTipTask(gameTipConfig.getToBeCreatedId()));
        tipTaskContainer.add(createSendBuildCommandTipTask(gameTipConfig.getToBeCreatedId(), gameTipConfig.getTerrainPositionHint()));
        tipTaskContainer.addFallback(createIdleItemTipTask(gameTipConfig.getActor()));
        tipTaskContainer.addFallback(createSelectTipTask(gameTipConfig.getActor()));
        tipTaskContainer.addFallback(createToBeBuildPlacerTipTask(gameTipConfig.getToBeCreatedId()));
        tipTaskContainer.addFallback(createSendBuildCommandTipTask(gameTipConfig.getToBeCreatedId(), gameTipConfig.getTerrainPositionHint()));
    }

    private void createFactorizeUnit(TipTaskContainer tipTaskContainer, GameTipConfig gameTipConfig) {
        tipTaskContainer.add(createSelectTipTask(gameTipConfig.getActor()));
        tipTaskContainer.add(createSendFactorizeCommandTipTask(gameTipConfig.getToBeCreatedId()));
        tipTaskContainer.addFallback(createIdleItemTipTask(gameTipConfig.getActor()));
        tipTaskContainer.addFallback(createSelectTipTask(gameTipConfig.getActor()));
        tipTaskContainer.addFallback(createSendFactorizeCommandTipTask(gameTipConfig.getToBeCreatedId()));
    }

    private void createHarvest(TipTaskContainer tipTaskContainer, GameTipConfig gameTipConfig) {
        tipTaskContainer.add(createSelectTipTask(gameTipConfig.getActor()));
        tipTaskContainer.add(createSendHarvestCommandTipTask(gameTipConfig.getResourceId(), gameTipConfig.getPlaceConfig()));
        tipTaskContainer.addFallback(createIdleItemTipTask(gameTipConfig.getActor()));
        tipTaskContainer.addFallback(createSelectTipTask(gameTipConfig.getActor()));
        tipTaskContainer.addFallback(createSendHarvestCommandTipTask(gameTipConfig.getResourceId(), gameTipConfig.getPlaceConfig()));
    }

    private void createMove(TipTaskContainer tipTaskContainer, GameTipConfig gameTipConfig) {
        tipTaskContainer.add(createSelectTipTask(gameTipConfig.getActor()));
        tipTaskContainer.add(createSendMoveCommandTipTask(gameTipConfig.getTerrainPositionHint()));
        tipTaskContainer.addFallback(createIdleItemTipTask(gameTipConfig.getActor()));
        tipTaskContainer.addFallback(createSelectTipTask(gameTipConfig.getActor()));
        tipTaskContainer.addFallback(createSendMoveCommandTipTask(gameTipConfig.getTerrainPositionHint()));
    }

    private void createAttack(TipTaskContainer tipTaskContainer, GameTipConfig gameTipConfig) {
        tipTaskContainer.add(createSelectTipTask(gameTipConfig.getActor()));
        tipTaskContainer.add(createSendAttackCommandTipTask(null, gameTipConfig.getPlaceConfig()));
        tipTaskContainer.addFallback(createIdleItemTipTask(gameTipConfig.getActor()));
        tipTaskContainer.addFallback(createSelectTipTask(gameTipConfig.getActor()));
        tipTaskContainer.addFallback(createSendAttackCommandTipTask(null, gameTipConfig.getPlaceConfig()));
    }

    private void createStartPlacer(TipTaskContainer tipTaskContainer, GameTipConfig gameTipConfig) {
        tipTaskContainer.add(createSpawnPlacerTipTask(gameTipConfig.getToBeCreatedId(), gameTipConfig.getTerrainPositionHint()));
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


}