package com.btxtech.server.service.engine;

import com.btxtech.server.gameengine.ClientSystemConnectionService;
import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.model.engine.LevelEntity;
import com.btxtech.server.model.engine.quest.QuestBackendInfo;
import com.btxtech.server.service.ui.GameUiContextService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.SlaveQuestInfo;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.planet.quest.QuestListener;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import jakarta.inject.Provider;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class ServerLevelQuestService implements QuestListener {
    private final Logger logger = Logger.getLogger(ServerLevelQuestService.class.getName());
    private final Provider<GameUiContextService> gameUiControlConfigPersistence;
    private final QuestService questService;
    private final ServerGameEngineCrudPersistence serverGameEngineCrudPersistence;
    private final LevelCrudPersistence levelCrudPersistence;
    private final UserService userService;
    private final ClientSystemConnectionService clientSystemConnectionService;
    private final Provider<ServerGameEngineControl> serverGameEngineControlInstance;
    private final ServerUnlockService serverUnlockService;

    public ServerLevelQuestService(Provider<GameUiContextService> gameUiControlConfigPersistence,
                                   QuestService questService,
                                   ServerGameEngineCrudPersistence serverGameEngineCrudPersistence,
                                   LevelCrudPersistence levelCrudPersistence,
                                   UserService userService,
                                   ClientSystemConnectionService clientSystemConnectionService,
                                   Provider<ServerGameEngineControl> serverGameEngineControlInstance,
                                   ServerUnlockService serverUnlockService) {
        this.gameUiControlConfigPersistence = gameUiControlConfigPersistence;
        this.questService = questService;
        this.serverGameEngineCrudPersistence = serverGameEngineCrudPersistence;
        this.levelCrudPersistence = levelCrudPersistence;
        this.userService = userService;
        this.clientSystemConnectionService = clientSystemConnectionService;
        this.serverGameEngineControlInstance = serverGameEngineControlInstance;
        this.serverUnlockService = serverUnlockService;
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
                clientSystemConnectionService.onQuestActivated(userContext.getUserId(), newQuest);
                questService.activateCondition(userContext.getUserId(), newQuest);
                clientSystemConnectionService.onQuestProgressInfo(userContext.getUserId(), questService.getQuestProgressInfo(userContext.getUserId()));
            }
        }
    }

    public SlaveQuestInfo getSlaveQuestInfo(String userId) {
        SlaveQuestInfo slaveQuestInfo = new SlaveQuestInfo();
        slaveQuestInfo.setActiveQuest(userService.findActiveQuestConfig4CurrentUser(userId));
        slaveQuestInfo.setQuestProgressInfo(questService.getQuestProgressInfo(userId));
        return slaveQuestInfo;
    }

    @Override
    @Transactional
    public void onQuestPassed(String userId, QuestConfig questConfig) {
        clientSystemConnectionService.onQuestPassed(userId, questConfig);
        // TODO historyPersistence.get().onQuest(userId, questConfig, QuestHistoryEntity.Type.QUEST_PASSED);
        UserContext userContext = userService.getUserContextTransactional(userId);
        // Check for level up
        int newXp = userContext.getXp() + questConfig.getXp();
        LevelEntity currentLevel = levelCrudPersistence.getEntity(userContext.getLevelId());
        if (newXp >= currentLevel.getXp2LevelUp()) {
            LevelEntity newLevel = levelCrudPersistence.getNextLevel(currentLevel);
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
            logger.warning("No next level found for: " + currentLevel);
        }
    }

    public void activateNextPossibleQuest(String userId) {
        QuestConfig newQuest = userService.getAndSaveNewQuest(userId);
        if (newQuest != null) {
            // TODO historyPersistence.get().onQuest(userId, newQuest, QuestHistoryEntity.Type.QUEST_ACTIVATED);
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
        clientSystemConnectionService.onQuestActivated(userId, newQuest);
        questService.activateCondition(userId, newQuest);
        clientSystemConnectionService.onQuestProgressInfo(userId, questService.getQuestProgressInfo(userId));
    }

    public List<QuestBackendInfo> getQuestBackendInfos() {
        throw new UnsupportedOperationException("... TODO ...");
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
}