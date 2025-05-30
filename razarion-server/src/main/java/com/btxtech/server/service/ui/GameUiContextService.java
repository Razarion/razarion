package com.btxtech.server.service.ui;

import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.model.ui.GameUiContextEntity;
import com.btxtech.server.repository.ui.GameUiContextRepository;
import com.btxtech.server.service.engine.AbstractConfigCrudPersistence;
import com.btxtech.server.service.engine.LevelCrudPersistence;
import com.btxtech.server.service.engine.ServerGameEngineCrudPersistence;
import com.btxtech.server.service.engine.ServerLevelQuestService;
import com.btxtech.server.service.engine.StaticGameConfigService;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.AudioConfig;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GameUiContextConfig;
import com.btxtech.shared.dto.InGameQuestVisualConfig;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmService;
import org.springframework.stereotype.Service;

@Service
public class GameUiContextService extends AbstractConfigCrudPersistence<GameUiContextConfig, GameUiContextEntity> {
    private final StaticGameConfigService staticGameConfigService;
    private final LevelCrudPersistence levelCrudPersistence;
    private final ServerGameEngineCrudPersistence serverGameEngineCrudPersistence;
    private final ServerLevelQuestService serverLevelQuestService;
    private final ServerUnlockService serverUnlockService;
    private final AlarmService alarmService;

    public GameUiContextService(GameUiContextRepository gameUiContextRepository,
                                StaticGameConfigService staticGameConfigService,
                                LevelCrudPersistence levelCrudPersistence,
                                ServerGameEngineCrudPersistence serverGameEngineCrudPersistence, ServerLevelQuestService serverLevelQuestService, ServerUnlockService serverUnlockService,
                                AlarmService alarmService) {
        super(GameUiContextEntity.class, gameUiContextRepository);
        this.staticGameConfigService = staticGameConfigService;
        this.levelCrudPersistence = levelCrudPersistence;
        this.serverGameEngineCrudPersistence = serverGameEngineCrudPersistence;
        this.serverLevelQuestService = serverLevelQuestService;
        this.serverUnlockService = serverUnlockService;
        this.alarmService = alarmService;
    }

    public ColdGameUiContext loadCold(UserContext userContext) {
        ColdGameUiContext coldGameUiContext = new ColdGameUiContext();
        coldGameUiContext.staticGameConfig(staticGameConfigService.loadStaticGameConfig());
        coldGameUiContext.userContext(userContext);
        if (userContext.getLevelId() == null) {
            alarmService.riseAlarm(Alarm.Type.USER_HAS_NO_LEVEL, userContext.getUserId());
            userContext.levelId(levelCrudPersistence.getStarterLevelId());
        }
        coldGameUiContext.audioConfig(setupAudioConfig());
        // TODO coldGameUiContext.gameTipVisualConfig(setupGameTipVisualConfig());
        coldGameUiContext.inGameQuestVisualConfig(setupInGameQuestVisualConfig());
        coldGameUiContext.warmGameUiContext(loadWarm(userContext));
        return coldGameUiContext;
    }

    public WarmGameUiContext loadWarm(UserContext userContext) {
        if (userContext.getLevelId() == null) {
            return null;
        }
        GameUiContextEntity gameUiContextEntity = load4Level(userContext.getLevelId());
        if (gameUiContextEntity == null) {
            return null;
        }
        WarmGameUiContext warmGameUiContext = gameUiContextEntity.toGameWarmGameUiControlConfig();
        if (warmGameUiContext.getGameEngineMode() == GameEngineMode.SLAVE) {
            warmGameUiContext.setSlavePlanetConfig(serverGameEngineCrudPersistence.readSlavePlanetConfig(userContext.getLevelId()));
            warmGameUiContext.setSlaveQuestInfo(serverLevelQuestService.getSlaveQuestInfo(userContext.getUserId()));
            warmGameUiContext.setAvailableUnlocks(serverUnlockService.hasAvailableUnlocks(userContext));
        }
        return warmGameUiContext;
    }

    private AudioConfig setupAudioConfig() {
        AudioConfig audioConfig = new AudioConfig();
//      TODO  audioConfig.setDialogOpened(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_DIALOG_OPENED));
//        audioConfig.setDialogClosed(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_DIALOG_CLOSED));
//        audioConfig.setOnQuestActivated(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_QUEST_ACTIVATED));
//        audioConfig.setOnQuestPassed(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_QUEST_PASSED));
//        audioConfig.setOnLevelUp(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_LEVEL_UP));
//        audioConfig.setOnBoxPicked(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_BOX_PICKED));
//        audioConfig.setOnSelectionCleared(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_SELECTION_CLEARED));
//        audioConfig.setOnOwnMultiSelection(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_SELECTION_OWN_MULTI));
//        audioConfig.setOnOwnSingleSelection(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_SELECTION_OWN_SINGLE));
//        audioConfig.setOnOtherSelection(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_SELECTION_OTHER));
//        audioConfig.setOnCommandSent(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_COMMAND_SENT));
//        audioConfig.setOnBaseLost(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_BASE_LOST));
//        audioConfig.setTerrainLoopLand(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_TERRAIN_LAND));
//        audioConfig.setTerrainLoopWater(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_TERRAIN_WATER));
        return audioConfig;
    }

    private InGameQuestVisualConfig setupInGameQuestVisualConfig() {
        return new InGameQuestVisualConfig();
//     TODO           .nodesMaterialId(dbPropertiesService.getBabylonMaterialProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_NODES_MATERIAL))
//                .placeNodesMaterialId(dbPropertiesService.getBabylonMaterialProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_PLACE_NODES_MATERIAL))
//                .radius(dbPropertiesService.getDoubleProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_RADIUS))
//                .outOfViewNodesMaterialId(dbPropertiesService.getBabylonMaterialProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_OUT_OF_VIEW_NODES_MATERIAL))
//                .outOfViewSize(dbPropertiesService.getDoubleProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_OUT_OF_VIEW_SIZE))
//                .outOfViewDistanceFromCamera(dbPropertiesService.getDoubleProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_OUT_DISTANCE_FROM_CAMERA))
//                .harvestColor(dbPropertiesService.getColorProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_CORNER_HARVEST_COLOR))
//                .attackColor(dbPropertiesService.getColorProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_CORNER_ATTACK_COLOR))
//                .pickColor(dbPropertiesService.getColorProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_CORNER_PICK_COLOR));
    }

    public GameUiContextEntity load4Level(int levelId) {
        return ((GameUiContextRepository) getJpaRepository())
                .findTopByMinimalLevelNumber(levelId)
                .orElseThrow();
    }

    @Override
    protected GameUiContextConfig toConfig(GameUiContextEntity entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void fromConfig(GameUiContextConfig config, GameUiContextEntity entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
