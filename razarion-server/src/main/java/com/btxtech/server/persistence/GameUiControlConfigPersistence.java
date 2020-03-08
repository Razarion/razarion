package com.btxtech.server.persistence;

import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.gameengine.ServerLevelQuestService;
import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.persistence.level.LevelEntity_;
import com.btxtech.server.persistence.level.LevelPersistence;
import com.btxtech.server.persistence.server.ServerGameEngineCrudPersistence;
import com.btxtech.server.persistence.tracker.TrackerPersistence;
import com.btxtech.shared.datatypes.DbPropertyKey;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.AudioConfig;
import com.btxtech.shared.dto.ColdGameUiControlConfig;
import com.btxtech.shared.dto.GameTipVisualConfig;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.InGameQuestVisualConfig;
import com.btxtech.shared.dto.WarmGameUiControlConfig;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.planet.bot.BotService;
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
import java.util.Locale;

/**
 * Created by Beat
 * 03.08.2016.
 */
@Singleton
public class GameUiControlConfigPersistence {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private Shape3DPersistence shape3DPersistence;
    @Inject
    private StaticGameConfigPersistence staticGameConfigPersistence;
    @Inject
    private ServerGameEngineControl gameEngineService;
    @Inject
    private LevelPersistence levelPersistence;
    @Inject
    private ServerGameEngineCrudPersistence serverGameEngineCrudPersistence;
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

    @Transactional
    public ColdGameUiControlConfig load(GameUiControlInput gameUiControlInput, Locale locale, UserContext userContext) throws ParserConfigurationException, SAXException, IOException {
        ColdGameUiControlConfig coldGameUiControlConfig = new ColdGameUiControlConfig();
        coldGameUiControlConfig.setStaticGameConfig(staticGameConfigPersistence.loadStaticGameConfig());
        coldGameUiControlConfig.setUserContext(userContext);
        coldGameUiControlConfig.setLevelUnlockConfigs(serverUnlockService.gatherAvailableUnlocks(userContext.getHumanPlayerId(), userContext.getLevelId()));
        coldGameUiControlConfig.setShape3Ds(shape3DPersistence.getShape3Ds());
        coldGameUiControlConfig.setAudioConfig(setupAudioConfig());
        coldGameUiControlConfig.setGameTipVisualConfig(setupGameTipVisualConfig());
        coldGameUiControlConfig.setInGameQuestVisualConfig(setupInGameQuestVisualConfig());
        if (gameUiControlInput.checkPlayback()) {
            coldGameUiControlConfig.setWarmGameUiControlConfig(trackerPersistence.setupWarmGameUiControlConfig(gameUiControlInput));
        } else {
            coldGameUiControlConfig.setWarmGameUiControlConfig(loadWarm(locale, userContext));
        }
        return coldGameUiControlConfig;
    }

    @Transactional
    public WarmGameUiControlConfig loadWarm(Locale locale, UserContext userContext) {
        WarmGameUiControlConfig warmGameUiControlConfig = load4Level(userContext.getLevelId()).toGameWarmGameUiControlConfig(locale);
        if (warmGameUiControlConfig.getGameEngineMode() == GameEngineMode.SLAVE) {
            warmGameUiControlConfig.setSlavePlanetConfig(serverGameEngineCrudPersistence.readSlavePlanetConfig(userContext.getLevelId()));
            warmGameUiControlConfig.setSlaveQuestInfo(serverLevelQuestService.getSlaveQuestInfo(locale, userContext.getHumanPlayerId()));
            warmGameUiControlConfig.setBotSceneIndicationInfos(botService.getBotSceneIndicationInfos(userContext.getHumanPlayerId()));
        }
        return warmGameUiControlConfig;
    }

    public GameUiControlConfigEntity load4Level(int levelId) {
        int levelNumber = levelPersistence.getLevelNumber4Id(levelId);

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameUiControlConfigEntity> query = criteriaBuilder.createQuery(GameUiControlConfigEntity.class);
        Root<GameUiControlConfigEntity> root = query.from(GameUiControlConfigEntity.class);
        query.where(criteriaBuilder.lessThanOrEqualTo(root.join(GameUiControlConfigEntity_.minimalLevel).get(LevelEntity_.number), levelNumber));
        CriteriaQuery<GameUiControlConfigEntity> userSelect = query.select(root);
        query.orderBy(criteriaBuilder.desc(root.join(GameUiControlConfigEntity_.minimalLevel).get(LevelEntity_.number)));
        return entityManager.createQuery(userSelect).setFirstResult(0).setMaxResults(1).getSingleResult();
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
