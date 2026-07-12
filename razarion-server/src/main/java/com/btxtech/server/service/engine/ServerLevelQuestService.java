package com.btxtech.server.service.engine;

import com.btxtech.server.gameengine.ClientSystemConnectionService;
import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.model.engine.LevelEntity;
import com.btxtech.server.model.engine.quest.QuestBackendInfo;
import com.btxtech.server.service.tracking.RedditConversionService;
import com.btxtech.server.service.tracking.XConversionService;
import com.btxtech.server.service.tracking.UserActivityService;
import com.btxtech.server.service.ui.GameUiContextService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.SlaveQuestInfo;
import com.btxtech.shared.gameengine.datatypes.BackupPlanetInfo;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.planet.quest.QuestListener;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import jakarta.inject.Provider;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ServerLevelQuestService implements QuestListener {
    private final Logger logger = LoggerFactory.getLogger(ServerLevelQuestService.class);
    private final Provider<GameUiContextService> gameUiControlConfigPersistence;
    private final QuestService questService;
    private final ServerGameEngineService serverGameEngineCrudPersistence;
    private final LevelCrudService levelCrudPersistence;
    private final UserService userService;
    private final ClientSystemConnectionService clientSystemConnectionService;
    private final Provider<ServerGameEngineControl> serverGameEngineControlInstance;
    private final ServerUnlockService serverUnlockService;
    private final QuestConfigService questConfigService;
    private final UserActivityService userActivityService;
    private final RedditConversionService redditConversionService;
    private final XConversionService xConversionService;

    public ServerLevelQuestService(Provider<GameUiContextService> gameUiControlConfigPersistence,
                                   QuestService questService,
                                   ServerGameEngineService serverGameEngineCrudPersistence,
                                   LevelCrudService levelCrudPersistence,
                                   UserService userService,
                                   ClientSystemConnectionService clientSystemConnectionService,
                                   Provider<ServerGameEngineControl> serverGameEngineControlInstance,
                                   ServerUnlockService serverUnlockService,
                                   QuestConfigService questConfigService,
                                   UserActivityService userActivityService,
                                   RedditConversionService redditConversionService,
                                   XConversionService xConversionService) {
        this.gameUiControlConfigPersistence = gameUiControlConfigPersistence;
        this.questService = questService;
        this.serverGameEngineCrudPersistence = serverGameEngineCrudPersistence;
        this.levelCrudPersistence = levelCrudPersistence;
        this.userService = userService;
        this.clientSystemConnectionService = clientSystemConnectionService;
        this.serverGameEngineControlInstance = serverGameEngineControlInstance;
        this.serverUnlockService = serverUnlockService;
        this.questConfigService = questConfigService;
        this.userActivityService = userActivityService;
        this.redditConversionService = redditConversionService;
        this.xConversionService = xConversionService;
        questService.addQuestListener(this);
    }

    @Transactional
    public void onClientLevelUpdate(String userId, int newLevelId) {
        LevelEntity newLevel = levelCrudPersistence.getEntity(newLevelId);
        UserContext userContext = userService.getUserContext(userId);
        // TODO historyPersistence.get().onLevelUp(userContext.getUserId(), newLevel);

        // Temporary: Only save the level if on multiplayer planet. Main reason, tutorial state und units are not saved.
        // This is only called from the client.
        if (gameUiControlConfigPersistence.get().load4Level(newLevelId).getGameEngineMode() == GameEngineMode.SLAVE) {
            boolean activeQuest = questService.hasActiveQuest(userContext.getUserId());
            userContext.levelId(newLevelId);
            QuestConfig newQuest = null;
            userService.persistLevel(userContext.getUserId(), newLevel);
            if (!activeQuest) {
                newQuest = userService.getAndSaveNewQuest(userContext.getUserId());
            }
            if (newQuest != null) {
                // TODO historyPersistence.get().onQuest(userContext.getUserId(), newQuest, QuestHistoryEntity.Type.QUEST_ACTIVATED);
                resolveStartRegionIfNeeded(newQuest);
                clientSystemConnectionService.onQuestActivated(userContext.getUserId(), newQuest);
                questService.activateCondition(userContext.getUserId(), newQuest);
                clientSystemConnectionService.onQuestProgressInfo(userContext.getUserId(), questService.getQuestProgressInfo(userContext.getUserId()));
            }
        }
    }

    public SlaveQuestInfo getSlaveQuestInfo(String userId) {
        SlaveQuestInfo slaveQuestInfo = new SlaveQuestInfo();
        QuestConfig activeQuest = userService.findActiveQuestConfig4CurrentUser(userId);
        if (activeQuest != null) {
            resolveStartRegionIfNeeded(activeQuest);
        }
        slaveQuestInfo.setActiveQuest(activeQuest);
        slaveQuestInfo.setQuestProgressInfo(questService.getQuestProgressInfo(userId));
        return slaveQuestInfo;
    }

    @Override
    @Transactional
    public void onQuestPassed(String userId, QuestConfig questConfig) {
        clientSystemConnectionService.onQuestPassed(userId, questConfig);
        UserContext userContext = userService.getUserContextTransactional(userId);
        // Check for level up
        int newXp = userContext.getXp() + questConfig.getXp();
        LevelEntity currentLevel = levelCrudPersistence.getEntity(userContext.getLevelId());
        userActivityService.onQuestPassed(userId, questConfig.getId(), currentLevel.getNumber());
        redditConversionService.sendQuestPassedEvent(userId, questConfig.getId(), currentLevel.getNumber());
        xConversionService.sendQuestPassedEvent(userId, questConfig.getId(), currentLevel.getNumber());
        if (newXp >= currentLevel.getXp2LevelUp()) {
            LevelEntity newLevel = levelCrudPersistence.getNextLevel(currentLevel);
            if (newLevel != null) {
                userContext.levelId(newLevel.getId());
                userContext.xp(0);
                userActivityService.onLevelUp(userId, newLevel.getNumber());
                redditConversionService.sendLevelUpEvent(userId, newLevel.getNumber());
                xConversionService.sendLevelUpEvent(userId, newLevel.getNumber());
                clientSystemConnectionService.onLevelUp(userId,
                        userContext,
                        serverUnlockService.hasAvailableUnlocks(userContext));
                serverGameEngineControlInstance.get().onLevelChanged(userId, newLevel.getId());
                userService.persistLevel(userId, newLevel);
                userService.persistXp(userId, 0);
            } else {
                logger.warn("No next level found for: {}", currentLevel);
            }
        } else {
            userContext.xp(newXp);
            userService.persistXp(userId, newXp);
            clientSystemConnectionService.onXpChanged(userId, newXp);
        }
        userService.addCompletedServerQuest(userId, questConfig);
        // Activate next quest
        activateNextPossibleQuest(userId);
    }

    @Transactional
    public void setUserLevel(String userId, int levelId) {
        UserContext userContext = userService.getUserContext(userId);
        LevelEntity currentLevel = levelCrudPersistence.getEntity(userContext.getLevelId());
        LevelEntity newLevel = levelCrudPersistence.getEntity(levelId);
        if (newLevel != null) {
            userContext.levelId(newLevel.getId());
            userContext.xp(0);
            // TODO historyPersistence.get().onLevelUp(userId, newLevel);
            clientSystemConnectionService.onLevelUp(userId,
                    userContext,
                    serverUnlockService.hasAvailableUnlocks(userContext));
            serverGameEngineControlInstance.get().onLevelChanged(userId, newLevel.getId());
            userService.persistLevel(userId, newLevel);
            userService.persistXp(userId, 0);
        } else {
            logger.warn("No next level found for: {}", currentLevel);
        }
    }

    /**
     * Puts a user at the given level for testing: every quest in the levels
     * <em>before</em> the target level is marked as passed, every quest from the
     * target level onward is left open, and the first open quest of the target
     * level is activated. Mirrors {@code complete-quests-to-level.ps1} but as a
     * single atomic backend operation that also activates the next quest.
     */
    @Transactional
    public void prepareUserAtLevel(String userId, int levelId) {
        LevelEntity targetLevel = levelCrudPersistence.getEntity(levelId);
        if (targetLevel == null) {
            throw new IllegalArgumentException("No level for id: " + levelId);
        }
        int targetLevelNumber = targetLevel.getNumber();
        // Completed = all quests of the levels strictly before the target level.
        List<Integer> completedQuestIds = questConfigService.readQuestBackendInfos().stream()
                .filter(info -> info.getLevelNumber() >= 0 && info.getLevelNumber() < targetLevelNumber)
                .map(QuestBackendInfo::getId)
                .toList();
        userService.setCompletedQuest(userId, completedQuestIds);
        // Move to the target level (persists level, resets xp, notifies client + engine).
        setUserLevel(userId, levelId);
        // Drop any active quest, then activate the first open quest of the target level.
        deactivateQuest(userId);
        activateNextPossibleQuest(userId);
    }

    public void activateNextPossibleQuest(String userId) {
        QuestConfig newQuest = userService.getAndSaveNewQuest(userId);
        if (newQuest != null) {
            // An UNLOCKED quest is event based: it only completes when an unlock actually
            // happens after activation (ServerUnlockService.unlockViaCrystals -> onUnlock).
            // If the player has already unlocked everything reachable at the current level
            // there is no unlock left to trigger it, so the quest would soft-lock the player
            // forever. Auto-complete it in that case instead of activating it.
            if (isUnfulfillableUnlockQuest(userId, newQuest)) {
                logger.info("Auto-completing unlock quest {} for user {}: no available unlocks left", newQuest.getId(), userId);
                onQuestPassed(userId, newQuest);
                return;
            }
            // TODO historyPersistence.get().onQuest(userId, newQuest, QuestHistoryEntity.Type.QUEST_ACTIVATED);
            resolveStartRegionIfNeeded(newQuest);
            clientSystemConnectionService.onQuestActivated(userId, newQuest);
            questService.activateCondition(userId, newQuest);
            clientSystemConnectionService.onQuestProgressInfo(userId, questService.getQuestProgressInfo(userId));
        } else {
            clientSystemConnectionService.onAllQuestsCompleted(userId);
        }
    }

    private boolean isUnfulfillableUnlockQuest(String userId, QuestConfig questConfig) {
        if (questConfig.getConditionConfig() == null
                || questConfig.getConditionConfig().getConditionTrigger() != ConditionTrigger.UNLOCKED) {
            return false;
        }
        // getUserContextTransactional (not getUserContext): toUserContext() reads LAZY relations
        // (levelUnlockEntities, level). This runs from the quest-listener / planet-tick path where
        // no transaction is open (onQuestPassed is @Transactional but registered as the raw bean),
        // so the non-transactional variant would throw LazyInitializationException.
        return !serverUnlockService.hasAvailableUnlocks(userService.getUserContextTransactional(userId));
    }

    public List<QuestConfig> readOpenQuestForDialog(UserContext userContext) {
        return serverGameEngineCrudPersistence.getQuests4Dialog(levelCrudPersistence.getEntity(userContext.getLevelId()), readActiveOrPassedQuestIds(userContext));
    }

    @Transactional
    public List<Integer> readActiveOrPassedQuestIds(UserContext userContext) {
        return userService.findActivePassedQuestId(userContext.getUserId());
    }


    @Transactional
    public void activateQuestBackend(String userId, int questId) {
        activateQuest(userService.getUserContext(userId), questId);
    }

    @Transactional
    public void deactivateQuestBackend(String userId) {
        deactivateQuest(userId);
    }

    @Transactional // Needs to be @Transactional if a quest if fulfilled during activation and a new quest is activated
    public void activateQuest(UserContext userContext, int questId) {
        String userId = userContext.getUserId();
        deactivateQuest(userId);
        QuestConfig newQuest = serverGameEngineCrudPersistence.getAndVerifyQuest(userContext.getLevelId(), questId);
        if (readActiveOrPassedQuestIds(userContext).contains(newQuest.getId())) {
            throw new IllegalArgumentException("Given quest is passed");
        }

        userService.setActiveQuest(userId, newQuest.getId());
        // TODO historyPersistence.get().onQuest(userId, newQuest, QuestHistoryEntity.Type.QUEST_ACTIVATED);
        resolveStartRegionIfNeeded(newQuest);
        clientSystemConnectionService.onQuestActivated(userId, newQuest);
        questService.activateCondition(userId, newQuest);
        clientSystemConnectionService.onQuestProgressInfo(userId, questService.getQuestProgressInfo(userId));
    }

    public List<QuestBackendInfo> getQuestBackendInfos() {
        return questConfigService.readQuestBackendInfos();
    }

    /**
     * Re-registers the live quest-progress condition for every user with a persisted active quest and
     * then restores the backed-up progress counters. Must run on planet (re)start/restore: the active
     * quest identity survives in the DB and the progress counter in the planet backup, but
     * {@link QuestService}'s progressMap is in-memory only and is empty after a fresh start. Without
     * this, every progress trigger silently no-ops and the quest can never be fulfilled.
     * The activateCondition-then-restore order is required: {@link QuestService#restore} looks up the
     * already re-registered progress and skips it otherwise.
     */
    public void reactivatePersistedQuests(BackupPlanetInfo backupPlanetInfo) {
        Map<String, QuestConfig> activeQuests = userService.findActiveQuests4Users();
        activeQuests.forEach((userId, questConfig) -> {
            try {
                resolveStartRegionIfNeeded(questConfig);
                questService.activateCondition(userId, questConfig);
            } catch (Throwable t) {
                logger.error("Could not reactivate persisted quest {} for user {}", questConfig.getId(), userId, t);
            }
        });
        questService.restore(backupPlanetInfo);
    }

    private void deactivateQuest(String userId) {
        if (questService.hasActiveQuest(userId)) {
            questService.deactivateActorCondition(userId);
            clientSystemConnectionService.onQuestActivated(userId, null);
            QuestConfig oldQuest = userService.getActiveQuest(userId);
            userService.clearActiveQuest(userId);
            // TODO historyPersistence.get().onQuest(userId, oldQuest, QuestHistoryEntity.Type.QUEST_DEACTIVATED);
        }
    }

    private void resolveStartRegionIfNeeded(QuestConfig questConfig) {
        if (questConfig.getConditionConfig() != null
                && questConfig.getConditionConfig().getComparisonConfig() != null
                && questConfig.getConditionConfig().getComparisonConfig().getStartRegionId() != null) {
            int startRegionId = questConfig.getConditionConfig().getComparisonConfig().getStartRegionId();
            PlaceConfig startRegion = serverGameEngineCrudPersistence.getStartRegionById(startRegionId);
            if (startRegion != null) {
                questConfig.getConditionConfig().getComparisonConfig().setPlaceConfig(startRegion);
            } else {
                logger.warn("startRegionId is set but no start region found for id: {}", startRegionId);
            }
        }
    }
}