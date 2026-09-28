package com.btxtech.server.service.ui;

import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.model.ui.GameUiContextEntity;
import com.btxtech.server.repository.ui.GameUiContextRepository;
import com.btxtech.server.service.engine.AbstractConfigCrudService;
import com.btxtech.server.service.engine.DbPropertiesService;
import com.btxtech.server.service.engine.LevelCrudService;
import com.btxtech.server.service.engine.ServerGameEngineService;
import com.btxtech.server.service.engine.ServerLevelQuestService;
import com.btxtech.server.service.engine.StartPositionFinderService;
import com.btxtech.server.service.engine.StaticGameConfigService;
import com.btxtech.shared.datatypes.DbPropertyKey;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.AudioConfig;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GameUiContextConfig;
import com.btxtech.shared.dto.InGameQuestVisualConfig;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GameUiContextService extends AbstractConfigCrudService<GameUiContextConfig, GameUiContextEntity> {
    private final Logger logger = LoggerFactory.getLogger(GameUiContextService.class);
    private final StaticGameConfigService staticGameConfigService;
    private final LevelCrudService levelCrudPersistence;
    private final ServerGameEngineService serverGameEngineCrudPersistence;
    private final ServerLevelQuestService serverLevelQuestService;
    private final ServerUnlockService serverUnlockService;
    private final AlarmService alarmService;
    private final DbPropertiesService dbPropertiesService;
    private final BaseItemService baseItemService;
    private final StartPositionFinderService startPositionFinderService;
    /**
     * The static config the game engine runs with, handed over by {@link InitializeService} at start,
     * on reloadStatic and on a terrain rebuild. Null until the first one, then loadCold reads the database.
     */
    private volatile StaticGameConfig engineStaticGameConfig;
    /** Audio and quest-visualisation ids from the db properties, see {@link #cachedProperties()}. */
    private volatile CachedProperties cachedProperties;
    private static final long PROPERTIES_TTL_MILLIS = 60_000;

    public GameUiContextService(GameUiContextRepository gameUiContextRepository,
                                StaticGameConfigService staticGameConfigService,
                                LevelCrudService levelCrudPersistence,
                                ServerGameEngineService serverGameEngineCrudPersistence, ServerLevelQuestService serverLevelQuestService, ServerUnlockService serverUnlockService,
                                AlarmService alarmService, DbPropertiesService dbPropertiesService,
                                BaseItemService baseItemService,
                                StartPositionFinderService startPositionFinderService,
                                InitializeService initializeService) {
        super(GameUiContextEntity.class, gameUiContextRepository);
        this.staticGameConfigService = staticGameConfigService;
        this.levelCrudPersistence = levelCrudPersistence;
        this.serverGameEngineCrudPersistence = serverGameEngineCrudPersistence;
        this.serverLevelQuestService = serverLevelQuestService;
        this.serverUnlockService = serverUnlockService;
        this.alarmService = alarmService;
        this.dbPropertiesService = dbPropertiesService;
        this.baseItemService = baseItemService;
        this.startPositionFinderService = startPositionFinderService;
        initializeService.receiveStaticGameConfig(staticGameConfig -> engineStaticGameConfig = staticGameConfig);
    }

    /**
     * Every player's start waits for this, and it was rebuilt from the database each time: the whole
     * static config (~100 ms locally), sixteen single property reads (~45 ms) and the warm part - on
     * PROD 270 to 560 ms of server time for an answer that is the same for everybody but the warm part,
     * 2026-09-27. The static config now comes from the game engine, which holds it anyway and is what
     * the client has to agree with; the properties are kept for a minute.
     */
    public ColdGameUiContext loadCold(UserContext userContext) {
        ColdGameUiContext coldGameUiContext = new ColdGameUiContext();
        StaticGameConfig staticGameConfig = engineStaticGameConfig;
        coldGameUiContext.staticGameConfig(staticGameConfig != null ? staticGameConfig : staticGameConfigService.loadStaticGameConfig());
        coldGameUiContext.userContext(userContext);
        if (userContext.getLevelId() == null) {
            alarmService.riseAlarm(Alarm.Type.USER_HAS_NO_LEVEL, userContext.getUserId());
            userContext.levelId(levelCrudPersistence.getStarterLevelId());
        }
        CachedProperties properties = cachedProperties();
        coldGameUiContext.audioConfig(properties.audioConfig());
        coldGameUiContext.inGameQuestVisualConfig(properties.inGameQuestVisualConfig());
        coldGameUiContext.warmGameUiContext(loadWarm(userContext));
        return coldGameUiContext;
    }

    /**
     * A minute old at most: the ids change only when somebody edits the db properties, and a player
     * who starts within that minute gets the previous sound - there is no reload hook for them.
     */
    private CachedProperties cachedProperties() {
        CachedProperties properties = cachedProperties;
        long now = System.currentTimeMillis();
        if (properties == null || now - properties.loadedAt() > PROPERTIES_TTL_MILLIS) {
            properties = new CachedProperties(setupAudioConfig(), setupInGameQuestVisualConfig(), now);
            cachedProperties = properties;
        }
        return properties;
    }

    private record CachedProperties(AudioConfig audioConfig, InGameQuestVisualConfig inGameQuestVisualConfig, long loadedAt) {
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
            var slavePlanetConfig = serverGameEngineCrudPersistence.readSlavePlanetConfig(userContext.getLevelId());
            if (slavePlanetConfig.isFindFreePosition() && baseItemService.getPlayerBase4UserId(userContext.getUserId()) == null) {
                try {
                    slavePlanetConfig.setNoBaseViewPosition(startPositionFinderService.findFreePosition(slavePlanetConfig));
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
            }
            warmGameUiContext.setSlavePlanetConfig(slavePlanetConfig);
            warmGameUiContext.setSlaveQuestInfo(serverLevelQuestService.getSlaveQuestInfo(userContext.getUserId()));
            warmGameUiContext.setAvailableUnlocks(serverUnlockService.hasAvailableUnlocks(userContext));
        }
        return warmGameUiContext;
    }

    private AudioConfig setupAudioConfig() {
        AudioConfig audioConfig = new AudioConfig();
        audioConfig.setDialogOpened(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_DIALOG_OPENED));
        audioConfig.setDialogClosed(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_DIALOG_CLOSED));
        audioConfig.setOnQuestActivated(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_QUEST_ACTIVATED));
        audioConfig.setOnQuestPassed(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_QUEST_PASSED));
        audioConfig.setOnLevelUp(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_LEVEL_UP));
        audioConfig.setOnBoxPicked(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_BOX_PICKED));
        audioConfig.setOnBaseLost(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_BASE_LOST));
        return audioConfig;
    }

    private InGameQuestVisualConfig setupInGameQuestVisualConfig() {
        return new InGameQuestVisualConfig()
                .nodesMaterialId(dbPropertiesService.getBabylonMaterialProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_NODES_MATERIAL))
                .placeNodesMaterialId(dbPropertiesService.getBabylonMaterialProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_PLACE_NODES_MATERIAL))
                .radius(dbPropertiesService.getDoubleProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_RADIUS))
                .outOfViewNodesMaterialId(dbPropertiesService.getBabylonMaterialProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_OUT_OF_VIEW_NODES_MATERIAL))
                .outOfViewSize(dbPropertiesService.getDoubleProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_OUT_OF_VIEW_SIZE))
                .outOfViewDistanceFromCamera(dbPropertiesService.getDoubleProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_OUT_DISTANCE_FROM_CAMERA))
                .harvestColor(dbPropertiesService.getColorProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_CORNER_HARVEST_COLOR))
                .attackColor(dbPropertiesService.getColorProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_CORNER_ATTACK_COLOR))
                .pickColor(dbPropertiesService.getColorProperty(DbPropertyKey.QUEST_IN_GAME_VISUALIZATION_CORNER_PICK_COLOR));
    }

    /**
     * The planet a new player starts on - the one loadWarm hands a player of the starter level. The
     * game page asks for its terrain before it knows anything else about the player, see
     * TerrainShapeControllerImpl#prefetchUrls.
     */
    @Transactional
    public PlanetConfig starterPlanetConfig() {
        return load4Level(levelCrudPersistence.getStarterLevelId()).toGameWarmGameUiControlConfig().getPlanetConfig();
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
