package com.btxtech.server.persistence;

import com.btxtech.server.persistence.bot.BotConfigEntity;
import com.btxtech.server.persistence.inventory.InventoryPersistence;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.scene.BotAttackCommandEntity;
import com.btxtech.server.persistence.scene.BotHarvestCommandEntity;
import com.btxtech.server.persistence.scene.BotKillBotCommandEntity;
import com.btxtech.server.persistence.scene.BotKillHumanCommandEntity;
import com.btxtech.server.persistence.scene.BotKillOtherBotCommandEntity;
import com.btxtech.server.persistence.scene.BotMoveCommandEntity;
import com.btxtech.server.persistence.scene.BotRemoveOwnItemCommandEntity;
import com.btxtech.server.persistence.scene.BoxItemPositionEntity;
import com.btxtech.server.persistence.scene.GameTipConfigEntity;
import com.btxtech.server.persistence.scene.ResourceItemPositionEntity;
import com.btxtech.server.persistence.scene.SceneEntity;
import com.btxtech.server.persistence.server.ServerChildListCrudePersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.dto.BotAttackCommandConfig;
import com.btxtech.shared.dto.BotHarvestCommandConfig;
import com.btxtech.shared.dto.BotKillHumanCommandConfig;
import com.btxtech.shared.dto.BotKillOtherBotCommandConfig;
import com.btxtech.shared.dto.BotMoveCommandConfig;
import com.btxtech.shared.dto.BotRemoveOwnItemCommandConfig;
import com.btxtech.shared.dto.BoxItemPosition;
import com.btxtech.shared.dto.KillBotCommandConfig;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Beat
 * 16.05.2017.
 */
@Singleton
public class SceneEditorPersistence {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private ItemTypePersistence itemTypePersistence;
    @Inject
    private InventoryPersistence inventoryPersistence;
    @Inject
    private Instance<ServerChildListCrudePersistence<GameUiControlConfigEntity, GameUiControlConfigEntity, SceneEntity, SceneConfig>> sceneConfigCrudInstance;

    @Transactional
    @SecurityCheck
    @Deprecated
    public void saveAllScenes(int gameUiControlConfigId, List<SceneConfig> sceneConfigs, Locale locale) {
        GameUiControlConfigEntity gameUiControlConfigEntity = entityManager.find(GameUiControlConfigEntity.class, gameUiControlConfigId);
        if (gameUiControlConfigEntity == null) {
            throw new IllegalArgumentException("No GameUiControlConfigEntity for gameUiControlConfigId: " + gameUiControlConfigId);
        }
        List<SceneEntity> sceneEntities = gameUiControlConfigEntity.getScenes();
        if (sceneEntities == null) {
            sceneEntities = new ArrayList<>();
        }
        sceneEntities.clear();
        for (SceneConfig sceneConfig : sceneConfigs) {
            SceneEntity sceneEntity = new SceneEntity();
            sceneEntity.fromSceneConfig(itemTypePersistence, sceneConfig, locale);
            if (sceneConfig.getBotConfigs() != null) {
                List<BotConfigEntity> botConfigEntities = new ArrayList<>();
                for (BotConfig botConfig : sceneConfig.getBotConfigs()) {
                    BotConfigEntity botConfigEntity = new BotConfigEntity();
                    botConfigEntity.fromBotConfig(itemTypePersistence, botConfig);
                    botConfigEntities.add(botConfigEntity);
                }
                sceneEntity.setBotConfigEntities(botConfigEntities);
            }
            if (sceneConfig.getBotAttackCommandConfigs() != null) {
                List<BotAttackCommandEntity> botAttackCommandEntities = new ArrayList<>();
                for (BotAttackCommandConfig botAttackCommandConfig : sceneConfig.getBotAttackCommandConfigs()) {
                    BotAttackCommandEntity botAttackCommandEntity = new BotAttackCommandEntity();
                    botAttackCommandEntity.setBotAuxiliaryIdId(botAttackCommandConfig.getBotAuxiliaryId());
                    botAttackCommandEntity.setActorItemType(itemTypePersistence.readBaseItemTypeEntity(botAttackCommandConfig.getActorItemTypeId()));
                    botAttackCommandEntity.setTargetItemType(itemTypePersistence.readBaseItemTypeEntity(botAttackCommandConfig.getTargetItemTypeId()));
                    if (botAttackCommandConfig.getTargetSelection() != null) {
                        PlaceConfigEntity BotConfigEntity = new PlaceConfigEntity();
                        BotConfigEntity.fromPlaceConfig(botAttackCommandConfig.getTargetSelection());
                        botAttackCommandEntity.setTargetSelection(BotConfigEntity);
                    }
                    botAttackCommandEntities.add(botAttackCommandEntity);
                }
                sceneEntity.setBotAttackCommandEntities(botAttackCommandEntities);
            }
            if (sceneConfig.getBotMoveCommandConfigs() != null) {
                List<BotMoveCommandEntity> botMoveCommandEntities = new ArrayList<>();
                for (BotMoveCommandConfig botMoveCommandConfig : sceneConfig.getBotMoveCommandConfigs()) {
                    BotMoveCommandEntity botMoveCommandEntity = new BotMoveCommandEntity();
                    botMoveCommandEntity.setBotAuxiliaryIdId(botMoveCommandConfig.getBotAuxiliaryId());
                    botMoveCommandEntity.setBaseItemType(itemTypePersistence.readBaseItemTypeEntity(botMoveCommandConfig.getBaseItemTypeId()));
                    botMoveCommandEntity.setTargetPosition(botMoveCommandConfig.getTargetPosition());
                    botMoveCommandEntities.add(botMoveCommandEntity);
                }
                sceneEntity.setBotMoveCommandEntities(botMoveCommandEntities);
            }
            if (sceneConfig.getBotHarvestCommandConfigs() != null) {
                List<BotHarvestCommandEntity> botHarvestCommandEntities = new ArrayList<>();
                for (BotHarvestCommandConfig botHarvestCommandConfig : sceneConfig.getBotHarvestCommandConfigs()) {
                    BotHarvestCommandEntity botHarvestCommandEntity = new BotHarvestCommandEntity();
                    botHarvestCommandEntity.setBotAuxiliaryIdId(botHarvestCommandConfig.getBotAuxiliaryId());
                    botHarvestCommandEntity.setHarvesterItemType(itemTypePersistence.readBaseItemTypeEntity(botHarvestCommandConfig.getHarvesterItemTypeId()));
                    botHarvestCommandEntity.setResourceItemType(itemTypePersistence.readResourceItemTypeEntity(botHarvestCommandConfig.getResourceItemTypeId()));
                    if (botHarvestCommandConfig.getResourceSelection() != null) {
                        PlaceConfigEntity placeConfigEntity = new PlaceConfigEntity();
                        placeConfigEntity.fromPlaceConfig(botHarvestCommandConfig.getResourceSelection());
                        botHarvestCommandEntity.setResourceSelection(placeConfigEntity);
                    }
                    botHarvestCommandEntities.add(botHarvestCommandEntity);
                }
                sceneEntity.setBotHarvestCommandEntities(botHarvestCommandEntities);
            }
            if (sceneConfig.getBotKillOtherBotCommandConfigs() != null) {
                List<BotKillOtherBotCommandEntity> botKillOtherBotCommandEntities = new ArrayList<>();
                for (BotKillOtherBotCommandConfig botKillOtherBotCommandConfig : sceneConfig.getBotKillOtherBotCommandConfigs()) {
                    BotKillOtherBotCommandEntity botKillOtherBotCommandEntity = new BotKillOtherBotCommandEntity();
                    botKillOtherBotCommandEntity.fromBotKillOtherBotCommandConfig(botKillOtherBotCommandConfig);
                    if (botKillOtherBotCommandConfig.getAttackerBaseItemTypeId() != null) {
                        botKillOtherBotCommandEntity.setAttackerBaseItemType(itemTypePersistence.readBaseItemTypeEntity(botKillOtherBotCommandConfig.getAttackerBaseItemTypeId()));
                    }
                    botKillOtherBotCommandEntities.add(botKillOtherBotCommandEntity);
                }
                sceneEntity.setBotKillOtherBotCommandEntities(botKillOtherBotCommandEntities);
            }
            if (sceneConfig.getBotKillHumanCommandConfigs() != null) {
                List<BotKillHumanCommandEntity> botKillHumanCommandEntities = new ArrayList<>();
                for (BotKillHumanCommandConfig botKillHumanCommandConfig : sceneConfig.getBotKillHumanCommandConfigs()) {
                    BotKillHumanCommandEntity botKillHumanCommandEntity = new BotKillHumanCommandEntity();
                    botKillHumanCommandEntity.fromBotKillHumanCommandConfig(botKillHumanCommandConfig);
                    if (botKillHumanCommandConfig.getAttackerBaseItemTypeId() != null) {
                        botKillHumanCommandEntity.setAttackerBaseItemType(itemTypePersistence.readBaseItemTypeEntity(botKillHumanCommandConfig.getAttackerBaseItemTypeId()));
                    }
                    botKillHumanCommandEntities.add(botKillHumanCommandEntity);
                }
                sceneEntity.setBotKillHumanCommandEntities(botKillHumanCommandEntities);
            }
            if (sceneConfig.getBotRemoveOwnItemCommandConfigs() != null) {
                List<BotRemoveOwnItemCommandEntity> botRemoveOwnItemCommandEntities = new ArrayList<>();
                for (BotRemoveOwnItemCommandConfig botRemoveOwnItemCommandConfig : sceneConfig.getBotRemoveOwnItemCommandConfigs()) {
                    BotRemoveOwnItemCommandEntity botRemoveOwnItemCommandEntity = new BotRemoveOwnItemCommandEntity();
                    botRemoveOwnItemCommandEntity.setBotAuxiliaryIdId(botRemoveOwnItemCommandConfig.getBotAuxiliaryId());
                    botRemoveOwnItemCommandEntity.setBaseItemType2Remove(itemTypePersistence.readBaseItemTypeEntity(botRemoveOwnItemCommandConfig.getBaseItemType2RemoveId()));
                    botRemoveOwnItemCommandEntities.add(botRemoveOwnItemCommandEntity);
                }
                sceneEntity.setBotRemoveOwnItemCommandEntities(botRemoveOwnItemCommandEntities);
            }
            if (sceneConfig.getKillBotCommandConfigs() != null) {
                List<BotKillBotCommandEntity> killBotCommandEntities = new ArrayList<>();
                for (KillBotCommandConfig killBotCommandConfig : sceneConfig.getKillBotCommandConfigs()) {
                    BotKillBotCommandEntity botKillBotCommandEntity = new BotKillBotCommandEntity();
                    botKillBotCommandEntity.fromKillBotCommandConfig(killBotCommandConfig);
                    killBotCommandEntities.add(botKillBotCommandEntity);
                }
                sceneEntity.setKillBotCommandEntities(killBotCommandEntities);
            }
            if (sceneConfig.getResourceItemTypePositions() != null) {
                List<ResourceItemPositionEntity> resourceItemPositionEntities = new ArrayList<>();
                for (ResourceItemPosition resourceItemPosition : sceneConfig.getResourceItemTypePositions()) {
                    ResourceItemPositionEntity resourceItemPositionEntity = new ResourceItemPositionEntity();
                    resourceItemPositionEntity.setResourceItemType(itemTypePersistence.readResourceItemTypeEntity(resourceItemPosition.getResourceItemTypeId()));
                    resourceItemPositionEntity.setPosition(resourceItemPosition.getPosition());
                    resourceItemPositionEntity.setRotationZ(resourceItemPosition.getRotationZ());
                    resourceItemPositionEntities.add(resourceItemPositionEntity);
                }
                sceneEntity.setResourceItemPositionEntities(resourceItemPositionEntities);
            }
            if (sceneConfig.getBoxItemPositions() != null) {
                List<BoxItemPositionEntity> boxItemPositionEntities = new ArrayList<>();
                for (BoxItemPosition boxItemPosition : sceneConfig.getBoxItemPositions()) {
                    BoxItemPositionEntity resourceItemPositionEntity = new BoxItemPositionEntity();
                    resourceItemPositionEntity.setBoxItemType(itemTypePersistence.readBoxItemTypeEntity(boxItemPosition.getBoxItemTypeId()));
                    resourceItemPositionEntity.setPosition(boxItemPosition.getPosition());
                    resourceItemPositionEntity.setRotationZ(boxItemPosition.getRotationZ());
                    boxItemPositionEntities.add(resourceItemPositionEntity);
                }
                sceneEntity.setBoxItemPositionEntities(boxItemPositionEntities);
            }
            if (sceneConfig.getGameTipConfig() != null) {
                GameTipConfigEntity gameTipConfigEntity = new GameTipConfigEntity();
                gameTipConfigEntity.setTip(sceneConfig.getGameTipConfig().getTip());
                gameTipConfigEntity.setActor(itemTypePersistence.readBaseItemTypeEntity(sceneConfig.getGameTipConfig().getActor()));
                gameTipConfigEntity.setToCreatedItemType(itemTypePersistence.readBaseItemTypeEntity(sceneConfig.getGameTipConfig().getToCreatedItemTypeId()));
                gameTipConfigEntity.setResourceItemTypeEntity(itemTypePersistence.readResourceItemTypeEntity(sceneConfig.getGameTipConfig().getResourceItemTypeId()));
                gameTipConfigEntity.setBoxItemTypeEntity(itemTypePersistence.readBoxItemTypeEntity(sceneConfig.getGameTipConfig().getBoxItemTypeId()));
                gameTipConfigEntity.setInventoryItemEntity(inventoryPersistence.readInventoryItemEntity(sceneConfig.getGameTipConfig().getInventoryItemId()));
                gameTipConfigEntity.setTerrainPositionHint(sceneConfig.getGameTipConfig().getTerrainPositionHint());
                if (sceneConfig.getGameTipConfig().getPlaceConfig() != null) {
                    PlaceConfigEntity placeConfigEntity = new PlaceConfigEntity();
                    placeConfigEntity.fromPlaceConfig(sceneConfig.getGameTipConfig().getPlaceConfig());
                    gameTipConfigEntity.setPlaceConfig(placeConfigEntity);
                }
                sceneEntity.setGameTipConfigEntity(gameTipConfigEntity);
            }
            sceneEntities.add(sceneEntity);
        }
    }

    public ServerChildListCrudePersistence<GameUiControlConfigEntity, GameUiControlConfigEntity, SceneEntity, SceneConfig> getSceneConfigCrud(int gameUiControlConfigId) {
        ServerChildListCrudePersistence<GameUiControlConfigEntity, GameUiControlConfigEntity, SceneEntity, SceneConfig> crud = sceneConfigCrudInstance.get();
        crud.setRootProvider(() -> readGameUiControlConfigEntity(gameUiControlConfigId)).setParentProvider(entityManager -> readGameUiControlConfigEntity(gameUiControlConfigId));
        crud.setEntitiesGetter((entityManager) -> readGameUiControlConfigEntity(gameUiControlConfigId).getScenes());
        crud.setEntitiesSetter((entityManager, sceneConfigEntities) -> readGameUiControlConfigEntity(gameUiControlConfigId).setScenes(sceneConfigEntities));
        crud.setEntityIdProvider(SceneEntity::getId).setConfigIdProvider(SceneConfig::getId);
        crud.setConfigGenerator(sceneEntity -> sceneEntity.toSceneConfig(Locale.US));
        crud.setEntityFactory(SceneEntity::new);
        crud.setEntityFiller((sceneEntity, sceneConfig) -> {
            sceneEntity.fromSceneConfig(itemTypePersistence, sceneConfig, Locale.US);
        });
        return crud;
    }

    private GameUiControlConfigEntity readGameUiControlConfigEntity(int gameUiControlConfigId) {
        return entityManager.find(GameUiControlConfigEntity.class, gameUiControlConfigId);
    }

}
