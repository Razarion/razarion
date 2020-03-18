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
import com.btxtech.server.persistence.server.ServerChildCrudPersistence;
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
    private ImagePersistence imagePersistence;
    @Inject
    private Instance<ServerChildCrudPersistence<GameUiControlContextEntity, GameUiControlContextEntity, SceneEntity, SceneConfig>> sceneConfigCrudInstance;

    public ServerChildCrudPersistence<GameUiControlContextEntity, GameUiControlContextEntity, SceneEntity, SceneConfig> getSceneConfigCrud(int gameUiControlConfigId) {
        ServerChildCrudPersistence<GameUiControlContextEntity, GameUiControlContextEntity, SceneEntity, SceneConfig> crud = sceneConfigCrudInstance.get();
        crud.setRootProvider(() -> readGameUiControlConfigEntity(gameUiControlConfigId)).setParentProvider(entityManager -> readGameUiControlConfigEntity(gameUiControlConfigId));
        crud.setEntitiesGetter((entityManager) -> readGameUiControlConfigEntity(gameUiControlConfigId).getScenes());
        crud.setEntitiesSetter((entityManager, sceneConfigEntities) -> readGameUiControlConfigEntity(gameUiControlConfigId).setScenes(sceneConfigEntities));
        crud.setEntityIdProvider(SceneEntity::getId).setConfigIdProvider(SceneConfig::getId);
        crud.setConfigGenerator(sceneEntity -> sceneEntity.toSceneConfig(Locale.US));
        crud.setEntityFactory(SceneEntity::new);
        crud.setEntityFiller((sceneEntity, sceneConfig) -> saveScene(sceneEntity, sceneConfig, Locale.US));
        return crud;
    }

    private GameUiControlContextEntity readGameUiControlConfigEntity(int gameUiControlConfigId) {
        return entityManager.find(GameUiControlContextEntity.class, gameUiControlConfigId);
    }

    private void saveScene(SceneEntity sceneEntity, SceneConfig sceneConfig, Locale locale) {
        sceneEntity.fromSceneConfig(itemTypePersistence, sceneConfig, locale);
        sceneEntity.clearBotConfigEntities();
        if (sceneConfig.getBotConfigs() != null) {
            for (BotConfig botConfig : sceneConfig.getBotConfigs()) {
                BotConfigEntity botConfigEntity = new BotConfigEntity();
                botConfigEntity.fromBotConfig(itemTypePersistence, botConfig);
                sceneEntity.addBotConfigEntity(botConfigEntity);
            }
        }
        sceneEntity.clearBotAttackCommandEntities();
        if (sceneConfig.getBotAttackCommandConfigs() != null) {
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
                sceneEntity.addBotAttackCommandEntity(botAttackCommandEntity);
            }
        }
        sceneEntity.clearBotMoveCommandEntities();
        if (sceneConfig.getBotMoveCommandConfigs() != null) {
            for (BotMoveCommandConfig botMoveCommandConfig : sceneConfig.getBotMoveCommandConfigs()) {
                BotMoveCommandEntity botMoveCommandEntity = new BotMoveCommandEntity();
                botMoveCommandEntity.setBotAuxiliaryIdId(botMoveCommandConfig.getBotAuxiliaryId());
                botMoveCommandEntity.setBaseItemType(itemTypePersistence.readBaseItemTypeEntity(botMoveCommandConfig.getBaseItemTypeId()));
                botMoveCommandEntity.setTargetPosition(botMoveCommandConfig.getTargetPosition());
                sceneEntity.addBotMoveCommandEntity(botMoveCommandEntity);
            }
        }
        sceneEntity.clearBotHarvestCommandEntities();
        if (sceneConfig.getBotHarvestCommandConfigs() != null) {
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
                sceneEntity.addBotHarvestCommandEntity(botHarvestCommandEntity);
            }
        }
        sceneEntity.clearBotKillOtherBotCommandEntities();
        if (sceneConfig.getBotKillOtherBotCommandConfigs() != null) {
            for (BotKillOtherBotCommandConfig botKillOtherBotCommandConfig : sceneConfig.getBotKillOtherBotCommandConfigs()) {
                BotKillOtherBotCommandEntity botKillOtherBotCommandEntity = new BotKillOtherBotCommandEntity();
                botKillOtherBotCommandEntity.fromBotKillOtherBotCommandConfig(botKillOtherBotCommandConfig);
                if (botKillOtherBotCommandConfig.getAttackerBaseItemTypeId() != null) {
                    botKillOtherBotCommandEntity.setAttackerBaseItemType(itemTypePersistence.readBaseItemTypeEntity(botKillOtherBotCommandConfig.getAttackerBaseItemTypeId()));
                }
                sceneEntity.addBotKillOtherBotCommandEntity(botKillOtherBotCommandEntity);
            }
        }
        sceneEntity.clearBotKillHumanCommandEntities();
        if (sceneConfig.getBotKillHumanCommandConfigs() != null) {
            for (BotKillHumanCommandConfig botKillHumanCommandConfig : sceneConfig.getBotKillHumanCommandConfigs()) {
                BotKillHumanCommandEntity botKillHumanCommandEntity = new BotKillHumanCommandEntity();
                botKillHumanCommandEntity.fromBotKillHumanCommandConfig(botKillHumanCommandConfig);
                if (botKillHumanCommandConfig.getAttackerBaseItemTypeId() != null) {
                    botKillHumanCommandEntity.setAttackerBaseItemType(itemTypePersistence.readBaseItemTypeEntity(botKillHumanCommandConfig.getAttackerBaseItemTypeId()));
                }
                sceneEntity.addBotKillHumanCommandEntity(botKillHumanCommandEntity);
            }
        }
        sceneEntity.clearBotRemoveOwnItemCommandEntities();
        if (sceneConfig.getBotRemoveOwnItemCommandConfigs() != null) {
            for (BotRemoveOwnItemCommandConfig botRemoveOwnItemCommandConfig : sceneConfig.getBotRemoveOwnItemCommandConfigs()) {
                BotRemoveOwnItemCommandEntity botRemoveOwnItemCommandEntity = new BotRemoveOwnItemCommandEntity();
                botRemoveOwnItemCommandEntity.setBotAuxiliaryIdId(botRemoveOwnItemCommandConfig.getBotAuxiliaryId());
                botRemoveOwnItemCommandEntity.setBaseItemType2Remove(itemTypePersistence.readBaseItemTypeEntity(botRemoveOwnItemCommandConfig.getBaseItemType2RemoveId()));
                sceneEntity.addBotRemoveOwnItemCommandEntity(botRemoveOwnItemCommandEntity);
            }
        }
        sceneEntity.clearKillBotCommandEntities();
        if (sceneConfig.getKillBotCommandConfigs() != null) {
            for (KillBotCommandConfig killBotCommandConfig : sceneConfig.getKillBotCommandConfigs()) {
                BotKillBotCommandEntity botKillBotCommandEntity = new BotKillBotCommandEntity();
                botKillBotCommandEntity.fromKillBotCommandConfig(killBotCommandConfig);
                sceneEntity.addKillBotCommandEntity(botKillBotCommandEntity);
            }
        }
        sceneEntity.clearResourceItemPositionEntities();
        if (sceneConfig.getResourceItemTypePositions() != null) {
            for (ResourceItemPosition resourceItemPosition : sceneConfig.getResourceItemTypePositions()) {
                ResourceItemPositionEntity resourceItemPositionEntity = new ResourceItemPositionEntity();
                resourceItemPositionEntity.setResourceItemType(itemTypePersistence.readResourceItemTypeEntity(resourceItemPosition.getResourceItemTypeId()));
                resourceItemPositionEntity.setPosition(resourceItemPosition.getPosition());
                resourceItemPositionEntity.setRotationZ(resourceItemPosition.getRotationZ());
                sceneEntity.addResourceItemPositionEntity(resourceItemPositionEntity);
            }
        }
        sceneEntity.clearBoxItemPositionEntities();
        if (sceneConfig.getBoxItemPositions() != null) {
            for (BoxItemPosition boxItemPosition : sceneConfig.getBoxItemPositions()) {
                BoxItemPositionEntity resourceItemPositionEntity = new BoxItemPositionEntity();
                resourceItemPositionEntity.setBoxItemType(itemTypePersistence.readBoxItemTypeEntity(boxItemPosition.getBoxItemTypeId()));
                resourceItemPositionEntity.setPosition(boxItemPosition.getPosition());
                resourceItemPositionEntity.setRotationZ(boxItemPosition.getRotationZ());
                sceneEntity.addBoxItemPositionEntity(resourceItemPositionEntity);
            }
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
            gameTipConfigEntity.setScrollMapImage(imagePersistence.getImageLibraryEntity(sceneConfig.getGameTipConfig().getScrollMapImageId()));
            sceneEntity.setGameTipConfigEntity(gameTipConfigEntity);
        }

    }

}
