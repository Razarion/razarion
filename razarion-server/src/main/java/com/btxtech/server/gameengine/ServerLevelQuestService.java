package com.btxtech.server.gameengine;

import com.btxtech.server.connection.ClientSystemConnectionService;
import com.btxtech.server.persistence.GameUiContextCrudPersistence;
import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.persistence.history.QuestHistoryEntity;
import com.btxtech.server.persistence.level.LevelCrudPersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.server.ServerGameEngineCrudPersistence;
import com.btxtech.server.rest.dto.QuestBackendInfo;
import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.SlaveQuestInfo;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.planet.quest.QuestListener;
import com.btxtech.shared.gameengine.planet.quest.QuestService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 09.08.2017.
 */
@Singleton
public class ServerLevelQuestService implements QuestListener {
    private final Logger logger = Logger.getLogger(ServerLevelQuestService.class.getName());
    @Inject
    private Provider<GameUiContextCrudPersistence> gameUiControlConfigPersistence;
    @Inject
    private Provider<HistoryPersistence> historyPersistence;
    @Inject
    private SessionService sessionService;
    @Inject
    private QuestService questService;
    @Inject
    private ServerGameEngineCrudPersistence serverGameEngineCrudPersistence;
    @Inject
    private LevelCrudPersistence levelCrudPersistence;
    @Inject
    private UserService userService;
    @Inject
    private ClientSystemConnectionService clientSystemConnectionService;
    @Inject
    private Provider<ServerGameEngineControl> serverGameEngineControlInstance;
    @Inject
    private ServerUnlockService serverUnlockService;

    @PostConstruct
    public void init() {
        questService.addQuestListener(this);
    }

    @Transactional
    public void onClientLevelUpdate(String sessionId, int newLevelId) {
        LevelEntity newLevel = levelCrudPersistence.getEntity(newLevelId);
        PlayerSession playerSession = sessionService.getSession(sessionId);
        UserContext userContext = playerSession.getUserContext();
        historyPersistence.get().onLevelUp(userContext.getUserId(), newLevel);

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
                historyPersistence.get().onQuest(userContext.getUserId(), newQuest, QuestHistoryEntity.Type.QUEST_ACTIVATED);
                clientSystemConnectionService.onQuestActivated(userContext.getUserId(), newQuest);
                questService.activateCondition(userContext.getUserId(), newQuest);
                clientSystemConnectionService.onQuestProgressInfo(userContext.getUserId(), questService.getQuestProgressInfo(userContext.getUserId()));
            }
        }
    }

    public SlaveQuestInfo getSlaveQuestInfo(int userId) {
        SlaveQuestInfo slaveQuestInfo = new SlaveQuestInfo();
        slaveQuestInfo.setActiveQuest(userService.findActiveQuestConfig4CurrentUser());
        slaveQuestInfo.setQuestProgressInfo(questService.getQuestProgressInfo(userId));
        return slaveQuestInfo;
    }

    @Override
    @Transactional
    public void onQuestPassed(int userId, QuestConfig questConfig) {
        clientSystemConnectionService.onQuestPassed(userId, questConfig);
        historyPersistence.get().onQuest(userId, questConfig, QuestHistoryEntity.Type.QUEST_PASSED);
        UserContext userContext = userService.getUserContext(userId);
        // Check for level up
        int newXp = userContext.getXp() + questConfig.getXp();
        LevelEntity currentLevel = levelCrudPersistence.getEntity(userContext.getLevelId());
        if (newXp >= currentLevel.getXp2LevelUp()) {
            LevelEntity newLevel = levelCrudPersistence.getNextLevel(currentLevel);
            if (newLevel != null) {
                userContext.levelId(newLevel.getId());
                userContext.xp(0);
                historyPersistence.get().onLevelUp(userId, newLevel);
                clientSystemConnectionService.onLevelUp(userId,
                        userContext,
                        serverUnlockService.hasAvailableUnlocks(userContext));
                serverGameEngineControlInstance.get().onLevelChanged(userId, newLevel.getId());
                userService.persistLevel(userId, newLevel);
                userService.persistXp(userId, 0);
            } else {
                logger.warning("No next level found for: " + currentLevel);
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
    public void setUserLevel(int userId, int levelId) {
        UserContext userContext = userService.getUserContext(userId);
        LevelEntity currentLevel = levelCrudPersistence.getEntity(userContext.getLevelId());
        LevelEntity newLevel = levelCrudPersistence.getEntity(levelId);
        if (newLevel != null) {
            userContext.levelId(newLevel.getId());
            userContext.xp(0);
            historyPersistence.get().onLevelUp(userId, newLevel);
            clientSystemConnectionService.onLevelUp(userId,
                    userContext,
                    serverUnlockService.hasAvailableUnlocks(userContext));
            serverGameEngineControlInstance.get().onLevelChanged(userId, newLevel.getId());
            userService.persistLevel(userId, newLevel);
            userService.persistXp(userId, 0);
        } else {
            logger.warning("No next level found for: " + currentLevel);
        }
    }

    public void activateNextPossibleQuest(int userId) {
        QuestConfig newQuest = userService.getAndSaveNewQuest(userId);
        if (newQuest != null) {
            historyPersistence.get().onQuest(userId, newQuest, QuestHistoryEntity.Type.QUEST_ACTIVATED);
            clientSystemConnectionService.onQuestActivated(userId, newQuest);
            questService.activateCondition(userId, newQuest);
            clientSystemConnectionService.onQuestProgressInfo(userId, questService.getQuestProgressInfo(userId));
        }
    }

    public List<QuestConfig> readOpenQuestForDialog(UserContext userContext) {
        return serverGameEngineCrudPersistence.getQuests4Dialog(levelCrudPersistence.getEntity(userContext.getLevelId()), readActiveOrPassedQuestIds(userContext));
    }

    @Transactional
    public List<Integer> readActiveOrPassedQuestIds(UserContext userContext) {
        return userService.findActivePassedQuestId(userContext.getUserId());
    }


    @Transactional
    public void activateQuestBackend(int userId, int questId) {
        activateQuest(userService.getUserContext(userId), questId);
    }

    @Transactional
    public void deactivateQuestBackend(int userId) {
        deactivateQuest(userId);
    }

    @Transactional // Needs to be @Transactional if a quest if fulfilled during activation and a new quest is activated
    public void activateQuest(UserContext userContext, int questId) {
        int userId = userContext.getUserId();
        deactivateQuest(userId);
        QuestConfig newQuest = serverGameEngineCrudPersistence.getAndVerifyQuest(userContext.getLevelId(), questId);
        if (readActiveOrPassedQuestIds(userContext).contains(newQuest.getId())) {
            throw new IllegalArgumentException("Given quest is passed");
        }

        userService.setActiveQuest(userId, newQuest.getId());
        historyPersistence.get().onQuest(userId, newQuest, QuestHistoryEntity.Type.QUEST_ACTIVATED);
        clientSystemConnectionService.onQuestActivated(userId, newQuest);
        questService.activateCondition(userId, newQuest);
        clientSystemConnectionService.onQuestProgressInfo(userId, questService.getQuestProgressInfo(userId));
    }

    private void deactivateQuest(int userId) {
        if (questService.hasActiveQuest(userId)) {
            questService.deactivateActorCondition(userId);
            clientSystemConnectionService.onQuestActivated(userId, null);
            QuestConfig oldQuest = userService.getActiveQuest(userId);
            userService.clearActiveQuest(userId);
            historyPersistence.get().onQuest(userId, oldQuest, QuestHistoryEntity.Type.QUEST_DEACTIVATED);
        }
    }

    public List<QuestBackendInfo> getQuestBackendInfos() {
        return gameUiControlConfigPersistence.get().readQuestBackendInfos();
    }
}