package com.btxtech.server.persistence;

import com.btxtech.server.gameengine.ServerLevelQuestService;
import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.persistence.bot.BotConfigEntity;
import com.btxtech.server.persistence.inventory.InventoryPersistence;
import com.btxtech.server.persistence.itemtype.BaseItemTypeCrudPersistence;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.itemtype.ResourceItemTypeCrudPersistence;
import com.btxtech.server.persistence.level.LevelCrudPersistence;
import com.btxtech.server.persistence.level.LevelEntity_;
import com.btxtech.server.persistence.particle.ParticleEmitterSequenceCrudPersistence;
import com.btxtech.server.persistence.particle.ParticleShapeCrudPersistence;
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
import com.btxtech.server.persistence.server.ServerGameEngineCrudPersistence;
import com.btxtech.server.persistence.tracker.TrackerPersistence;
import com.btxtech.shared.datatypes.DbPropertyKey;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.AudioConfig;
import com.btxtech.shared.dto.BotAttackCommandConfig;
import com.btxtech.shared.dto.BotHarvestCommandConfig;
import com.btxtech.shared.dto.BotKillHumanCommandConfig;
import com.btxtech.shared.dto.BotKillOtherBotCommandConfig;
import com.btxtech.shared.dto.BotMoveCommandConfig;
import com.btxtech.shared.dto.BotRemoveOwnItemCommandConfig;
import com.btxtech.shared.dto.BoxItemPosition;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GameTipVisualConfig;
import com.btxtech.shared.dto.GameUiContextConfig;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.InGameQuestVisualConfig;
import com.btxtech.shared.dto.KillBotCommandConfig;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.unityconverter.Converter;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.btxtech.shared.system.alarm.Alarm.Type.NO_GAME_UI_CONTROL_CONFIG_ENTITY_FOR_LEVEL_ID;

/**
 * Created by Beat
 * 03.08.2016.
 */
@Singleton
public class GameUiContextCrudPersistence extends AbstractCrudPersistence<GameUiContextConfig, GameUiContextEntity> {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private Shape3DCrudPersistence shape3DPersistence;
    @Inject
    private ParticleShapeCrudPersistence particleShapeCrudPersistence;
    @Inject
    private ParticleEmitterSequenceCrudPersistence particleEmitterSequenceCrudPersistence;
    @Inject
    private StaticGameConfigPersistence staticGameConfigPersistence;
    @Inject
    private PlanetCrudPersistence planetCrudPersistence;
    @Inject
    private LevelCrudPersistence levelCrudPersistence;
    @Inject
    private ServerGameEngineCrudPersistence serverGameEngineCrudPersistence;
    @Inject
    private BaseItemTypeCrudPersistence baseItemTypeCrudPersistence;
    @Inject
    private ResourceItemTypeCrudPersistence resourceItemTypeCrudPersistence;
    @Inject
    private DbPropertiesService dbPropertiesService;
    @Inject
    private TrackerPersistence trackerPersistence;
    @Inject
    private ServerLevelQuestService serverLevelQuestService;
    @Inject
    private ServerUnlockService serverUnlockService;
    @Inject
    private BotService botService;
    @Inject
    private AlarmService alarmService;
    @Inject
    private ItemTypePersistence itemTypePersistence;
    @Inject
    private InventoryPersistence inventoryPersistence;
    @Inject
    private ImagePersistence imagePersistence;

    public GameUiContextCrudPersistence() {
        super(GameUiContextEntity.class, GameUiContextEntity_.id, GameUiContextEntity_.internalName);
    }

    @Override
    protected GameUiContextConfig toConfig(GameUiContextEntity entity) {
        return entity.toConfig();
    }

    @Override
    protected void fromConfig(GameUiContextConfig config, GameUiContextEntity entity) {
        entity.fromConfig(config,
                levelCrudPersistence.getEntity(config.getMinimalLevelId()),
                planetCrudPersistence.getEntity(config.getPlanetId()));
        createEntityList(config.getScenes(), entity.getScenes());
    }

    private void createEntityList(List<SceneConfig> scenes, List<SceneEntity> entities) {
        List<SceneEntity> originals = new ArrayList<>(entities);
        entities.clear();
        scenes.forEach(sceneConfig -> {
            SceneEntity sceneEntity = getOrCreate(sceneConfig, originals);
            fromConfig(sceneEntity, sceneConfig, Locale.US);
            entities.add(sceneEntity);
        });
    }

    private SceneEntity getOrCreate(SceneConfig sceneConfig, List<SceneEntity> entities) {
        return entities.stream()
                .filter(sceneEntity -> sceneEntity.getId().equals(sceneConfig.getId()))
                .findFirst()
                .orElse(new SceneEntity());
    }

    @Transactional
    public ColdGameUiContext loadCold(GameUiControlInput gameUiControlInput, Locale locale, UserContext userContext) throws ParserConfigurationException, SAXException, IOException {
        ColdGameUiContext coldGameUiContext = new ColdGameUiContext();
        coldGameUiContext.staticGameConfig(staticGameConfigPersistence.loadStaticGameConfig());
        coldGameUiContext.userContext(userContext);
        if (userContext.getLevelId() == null) {
            alarmService.riseAlarm(Alarm.Type.USER_HAS_NO_LEVEL, userContext.getUserId());
            userContext.setLevelId(levelCrudPersistence.getStarterLevelId());
        }
        if (userContext.getLevelId() != null) {
            coldGameUiContext.levelUnlockConfigs(serverUnlockService.gatherAvailableUnlocks(userContext, userContext.getLevelId()));
        }
        coldGameUiContext.shape3Ds(shape3DPersistence.getShape3Ds());
        coldGameUiContext.meshContainers(Converter.readMeshContainers());
        coldGameUiContext.setParticleShapeConfigs(particleShapeCrudPersistence.read());
        coldGameUiContext.setParticleEmitterSequenceConfigs(particleEmitterSequenceCrudPersistence.read());
        coldGameUiContext.audioConfig(setupAudioConfig());
        coldGameUiContext.gameTipVisualConfig(setupGameTipVisualConfig());
        coldGameUiContext.inGameQuestVisualConfig(setupInGameQuestVisualConfig());
        if (gameUiControlInput.checkPlayback()) {
            coldGameUiContext.warmGameUiContext(trackerPersistence.setupWarmGameUiControlConfig(gameUiControlInput));
        } else {
            coldGameUiContext.warmGameUiContext(loadWarm(locale, userContext));
        }
        return coldGameUiContext;
    }

    @Transactional
    public WarmGameUiContext loadWarm(Locale locale, UserContext userContext) {
        if (userContext.getLevelId() == null) {
            return null;
        }
        GameUiContextEntity gameUiContextEntity = load4Level(userContext.getLevelId());
        if (gameUiContextEntity == null) {
            return null;
        }
        WarmGameUiContext warmGameUiContext = gameUiContextEntity.toGameWarmGameUiControlConfig(locale);
        if (warmGameUiContext.getGameEngineMode() == GameEngineMode.SLAVE) {
            warmGameUiContext.setSlavePlanetConfig(serverGameEngineCrudPersistence.readSlavePlanetConfig(userContext.getLevelId()));
            warmGameUiContext.setSlaveQuestInfo(serverLevelQuestService.getSlaveQuestInfo(locale, userContext.getUserId()));
            warmGameUiContext.setBotSceneIndicationInfos(botService.getBotSceneIndicationInfos(userContext.getUserId()));
        }
        return warmGameUiContext;
    }

    public GameUiContextEntity load4Level(int levelId) {
        int levelNumber = levelCrudPersistence.getLevelNumber4Id(levelId);

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameUiContextEntity> query = criteriaBuilder.createQuery(GameUiContextEntity.class);
        Root<GameUiContextEntity> root = query.from(GameUiContextEntity.class);
        query.where(criteriaBuilder.lessThanOrEqualTo(root.join(GameUiContextEntity_.minimalLevel).get(LevelEntity_.number), levelNumber));
        CriteriaQuery<GameUiContextEntity> userSelect = query.select(root);
        query.orderBy(criteriaBuilder.desc(root.join(GameUiContextEntity_.minimalLevel).get(LevelEntity_.number)));
        GameUiContextEntity gameUiContextEntity = entityManager.createQuery(userSelect).setFirstResult(0).setMaxResults(1).getResultList().stream().findFirst().orElse(null);
        if (gameUiContextEntity == null) {
            alarmService.riseAlarm(NO_GAME_UI_CONTROL_CONFIG_ENTITY_FOR_LEVEL_ID, levelId);
        }
        return gameUiContextEntity;
    }

    private void fromConfig(SceneEntity sceneEntity, SceneConfig sceneConfig, Locale locale) {
        sceneEntity.fromSceneConfig(itemTypePersistence, baseItemTypeCrudPersistence, sceneConfig, locale);
        sceneEntity.clearBotConfigEntities();
        if (sceneConfig.getBotConfigs() != null) {
            for (BotConfig botConfig : sceneConfig.getBotConfigs()) {
                BotConfigEntity botConfigEntity = new BotConfigEntity();
                botConfigEntity.fromBotConfig(baseItemTypeCrudPersistence, botConfig);
                sceneEntity.addBotConfigEntity(botConfigEntity);
            }
        }
        sceneEntity.clearBotAttackCommandEntities();
        if (sceneConfig.getBotAttackCommandConfigs() != null) {
            for (BotAttackCommandConfig botAttackCommandConfig : sceneConfig.getBotAttackCommandConfigs()) {
                BotAttackCommandEntity botAttackCommandEntity = new BotAttackCommandEntity();
                botAttackCommandEntity.setBotAuxiliaryIdId(botAttackCommandConfig.getBotAuxiliaryId());
                botAttackCommandEntity.setActorItemType(baseItemTypeCrudPersistence.getEntity(botAttackCommandConfig.getActorItemTypeId()));
                botAttackCommandEntity.setTargetItemType(baseItemTypeCrudPersistence.getEntity(botAttackCommandConfig.getTargetItemTypeId()));
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
                botMoveCommandEntity.setBaseItemType(baseItemTypeCrudPersistence.getEntity(botMoveCommandConfig.getBaseItemTypeId()));
                botMoveCommandEntity.setTargetPosition(botMoveCommandConfig.getTargetPosition());
                sceneEntity.addBotMoveCommandEntity(botMoveCommandEntity);
            }
        }
        sceneEntity.clearBotHarvestCommandEntities();
        if (sceneConfig.getBotHarvestCommandConfigs() != null) {
            for (BotHarvestCommandConfig botHarvestCommandConfig : sceneConfig.getBotHarvestCommandConfigs()) {
                BotHarvestCommandEntity botHarvestCommandEntity = new BotHarvestCommandEntity();
                botHarvestCommandEntity.setBotAuxiliaryIdId(botHarvestCommandConfig.getBotAuxiliaryId());
                botHarvestCommandEntity.setHarvesterItemType(baseItemTypeCrudPersistence.getEntity(botHarvestCommandConfig.getHarvesterItemTypeId()));
                botHarvestCommandEntity.setResourceItemType(resourceItemTypeCrudPersistence.getEntity(botHarvestCommandConfig.getResourceItemTypeId()));
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
                    botKillOtherBotCommandEntity.setAttackerBaseItemType(baseItemTypeCrudPersistence.getEntity(botKillOtherBotCommandConfig.getAttackerBaseItemTypeId()));
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
                    botKillHumanCommandEntity.setAttackerBaseItemType(baseItemTypeCrudPersistence.getEntity(botKillHumanCommandConfig.getAttackerBaseItemTypeId()));
                }
                sceneEntity.addBotKillHumanCommandEntity(botKillHumanCommandEntity);
            }
        }
        sceneEntity.clearBotRemoveOwnItemCommandEntities();
        if (sceneConfig.getBotRemoveOwnItemCommandConfigs() != null) {
            for (BotRemoveOwnItemCommandConfig botRemoveOwnItemCommandConfig : sceneConfig.getBotRemoveOwnItemCommandConfigs()) {
                BotRemoveOwnItemCommandEntity botRemoveOwnItemCommandEntity = new BotRemoveOwnItemCommandEntity();
                botRemoveOwnItemCommandEntity.setBotAuxiliaryIdId(botRemoveOwnItemCommandConfig.getBotAuxiliaryId());
                botRemoveOwnItemCommandEntity.setBaseItemType2Remove(baseItemTypeCrudPersistence.getEntity(botRemoveOwnItemCommandConfig.getBaseItemType2RemoveId()));
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
                resourceItemPositionEntity.setResourceItemType(resourceItemTypeCrudPersistence.getEntity(resourceItemPosition.getResourceItemTypeId()));
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
            gameTipConfigEntity.setActor(baseItemTypeCrudPersistence.getEntity(sceneConfig.getGameTipConfig().getActor()));
            gameTipConfigEntity.setToCreatedItemType(baseItemTypeCrudPersistence.getEntity(sceneConfig.getGameTipConfig().getToCreatedItemTypeId()));
            gameTipConfigEntity.setResourceItemTypeEntity(resourceItemTypeCrudPersistence.getEntity(sceneConfig.getGameTipConfig().getResourceItemTypeId()));
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

    private GameTipVisualConfig setupGameTipVisualConfig() {
        GameTipVisualConfig gameTipVisualConfig = new GameTipVisualConfig();
        gameTipVisualConfig.setCornerMoveDuration(dbPropertiesService.getIntProperty(DbPropertyKey.TIP_CORNER_MOVE_DURATION));
        gameTipVisualConfig.setCornerMoveDistance(dbPropertiesService.getDoubleProperty(DbPropertyKey.TIP_CORNER_MOVE_DISTANCE));
        gameTipVisualConfig.setCornerLength(dbPropertiesService.getDoubleProperty(DbPropertyKey.TIP_CORNER_LENGTH));
        gameTipVisualConfig.setDefaultCommandShape3DId(dbPropertiesService.getShape3DIdProperty(DbPropertyKey.TIP_DEFAULT_COMMAND_SHAPE3D));
        gameTipVisualConfig.setSelectCornerColor(dbPropertiesService.getColorProperty(DbPropertyKey.TIP_SELECT_CORNER_COLOR));
        gameTipVisualConfig.setSelectShape3DId(dbPropertiesService.getShape3DIdProperty(DbPropertyKey.TIP_SELECT_SHAPE3D));
        gameTipVisualConfig.setOutOfViewShape3DId(dbPropertiesService.getShape3DIdProperty(DbPropertyKey.TIP_OUT_OF_VIEW_SHAPE3D));
        gameTipVisualConfig.setAttackCommandCornerColor(dbPropertiesService.getColorProperty(DbPropertyKey.TIP_ATTACK_COMMAND_CORNER_COLOR));
        gameTipVisualConfig.setBaseItemPlacerCornerColor(dbPropertiesService.getColorProperty(DbPropertyKey.TIP_BASE_ITEM_PLACER_CORNER_COLOR));
        gameTipVisualConfig.setBaseItemPlacerShape3DId(dbPropertiesService.getShape3DIdProperty(DbPropertyKey.TIP_BASE_ITEM_PLACER_SHAPE3D));
        gameTipVisualConfig.setGrabCommandCornerColor(dbPropertiesService.getColorProperty(DbPropertyKey.TIP_GRAB_COMMAND_CORNER_COLOR));
        gameTipVisualConfig.setMoveCommandCornerColor(dbPropertiesService.getColorProperty(DbPropertyKey.TIP_MOVE_COMMAND_CORNER_COLOR));
        gameTipVisualConfig.setToBeFinalizedCornerColor(dbPropertiesService.getColorProperty(DbPropertyKey.TIP_TO_BE_FINALIZED_CORNER_COLOR));
        gameTipVisualConfig.setWestLeftMouseGuiImageId(dbPropertiesService.getImageIdProperty(DbPropertyKey.TIP_WEST_LEFT_MOUSE_IMAGE));
        gameTipVisualConfig.setSouthLeftMouseGuiImageId(dbPropertiesService.getImageIdProperty(DbPropertyKey.TIP_SOUTH_LEFT_MOUSE_IMAGE));
        gameTipVisualConfig.setDirectionShape3DId(dbPropertiesService.getShape3DIdProperty(DbPropertyKey.TIP_DIRECTION_SHAPE3D));
        gameTipVisualConfig.setScrollDialogKeyboardImageId(dbPropertiesService.getImageIdProperty(DbPropertyKey.TIP_SCROLL_DIALOG_KEYBOARD_IMAGE));
        return gameTipVisualConfig;
    }

    private InGameQuestVisualConfig setupInGameQuestVisualConfig() {
        InGameQuestVisualConfig inGameQuestVisualConfig = new InGameQuestVisualConfig();
        inGameQuestVisualConfig.setCornerLength(dbPropertiesService.getDoubleProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_CORNER_LENGTH));
        inGameQuestVisualConfig.setOutOfViewShape3DId(dbPropertiesService.getShape3DIdProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_OUT_OF_VIEW_SHAPE3D));
        inGameQuestVisualConfig.setMoveDistance(dbPropertiesService.getDoubleProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_MOVE_DISTANCE));
        inGameQuestVisualConfig.setDuration(dbPropertiesService.getIntProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_MOVE_DURATION));
        inGameQuestVisualConfig.setHarvestColor(dbPropertiesService.getColorProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_CORNER_HARVEST_COLOR));
        inGameQuestVisualConfig.setAttackColor(dbPropertiesService.getColorProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_CORNER_ATTACK_COLOR));
        inGameQuestVisualConfig.setPickColor(dbPropertiesService.getColorProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_CORNER_PICK_COLOR));
        return inGameQuestVisualConfig;
    }

    private AudioConfig setupAudioConfig() {
        AudioConfig audioConfig = new AudioConfig();
        audioConfig.setDialogOpened(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_DIALOG_OPENED));
        audioConfig.setDialogClosed(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_DIALOG_CLOSED));
        audioConfig.setOnQuestActivated(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_QUEST_ACTIVATED));
        audioConfig.setOnQuestPassed(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_QUEST_PASSED));
        audioConfig.setOnLevelUp(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_LEVEL_UP));
        audioConfig.setOnBoxPicked(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_BOX_PICKED));
        audioConfig.setOnSelectionCleared(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_SELECTION_CLEARED));
        audioConfig.setOnOwnMultiSelection(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_SELECTION_OWN_MULTI));
        audioConfig.setOnOwnSingleSelection(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_SELECTION_OWN_SINGLE));
        audioConfig.setOnOtherSelection(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_SELECTION_OTHER));
        audioConfig.setOnCommandSent(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_COMMAND_SENT));
        audioConfig.setOnBaseLost(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_BASE_LOST));
        audioConfig.setTerrainLoopLand(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_TERRAIN_LAND));
        audioConfig.setTerrainLoopWater(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_TERRAIN_WATER));
        return audioConfig;
    }
}
