package com.btxtech.server.gameengine;

import com.btxtech.server.connection.ClientSystemConnectionService;
import com.btxtech.server.persistence.GameUiContextCrudPersistence;
import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.persistence.history.QuestHistoryEntity;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelCrudPersistence;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.server.ServerGameEngineCrudPersistence;
import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.user.UnregisteredUser;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.SlaveQuestInfo;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.planet.quest.QuestListener;
import com.btxtech.shared.gameengine.planet.quest.QuestService;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 09.08.2017.
 */
@Singleton
public class ServerLevelQuestService implements QuestListener {
    private Logger logger = Logger.getLogger(ServerLevelQuestService.class.getName());
    @Inject
    private Instance<GameUiContextCrudPersistence> gameUiControlConfigPersistence;
    @Inject
    private Instance<HistoryPersistence> historyPersistence;
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
    private Instance<ServerGameEngineControl> serverGameEngineControlInstance;
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
        historyPersistence.get().onLevelUp(userContext.getHumanPlayerId(), newLevel);

        // Temporary: Only save the level if on multiplayer planet. Main reason, tutorial state und units are not saved.
        // This is only called from the client.
        if (gameUiControlConfigPersistence.get().load4Level(newLevelId).getGameEngineMode() == GameEngineMode.SLAVE) {
            boolean activeQuest = questService.hasActiveQuest(userContext.getHumanPlayerId());
            userContext.setLevelId(newLevelId);
            QuestConfig newQuest = null;
            if (userContext.registered()) {
                userService.persistLevel(userContext.getHumanPlayerId().getUserId(), newLevel);
                if (!activeQuest) {
                    newQuest = userService.getAndSaveNewQuest(userContext.getHumanPlayerId().getUserId());
                }
            } else {
                if (!activeQuest) {
                    UnregisteredUser unregisteredUser = sessionService.getSession(sessionId).getUnregisteredUser();
                    newQuest = serverGameEngineCrudPersistence.getQuest4LevelAndCompleted(newLevel, unregisteredUser.getCompletedQuestIds()).toQuestConfig(playerSession.getLocale());
                    unregisteredUser.setActiveQuest(newQuest);
                }
            }
            if (newQuest != null) {
                historyPersistence.get().onQuest(userContext.getHumanPlayerId(), newQuest, QuestHistoryEntity.Type.QUEST_ACTIVATED);
                clientSystemConnectionService.onQuestActivated(userContext.getHumanPlayerId(), newQuest);
                questService.activateCondition(userContext.getHumanPlayerId(), newQuest);
                clientSystemConnectionService.onQuestProgressInfo(userContext.getHumanPlayerId(), questService.getQuestProgressInfo(userContext.getHumanPlayerId()));
            }
        }
    }

    public SlaveQuestInfo getSlaveQuestInfo(Locale locale, HumanPlayerId humanPlayerId) {
        SlaveQuestInfo slaveQuestInfo = new SlaveQuestInfo();
        slaveQuestInfo.setActiveQuest(userService.findActiveQuestConfig4CurrentUser(locale));
        slaveQuestInfo.setQuestProgressInfo(questService.getQuestProgressInfo(humanPlayerId));
        return slaveQuestInfo;
    }

    @Override
    @Transactional
    public void onQuestPassed(HumanPlayerId humanPlayerId, QuestConfig questConfig) {
        clientSystemConnectionService.onQuestPassed(humanPlayerId, questConfig);
        historyPersistence.get().onQuest(humanPlayerId, questConfig, QuestHistoryEntity.Type.QUEST_PASSED);
        boolean registered = humanPlayerId.getUserId() != null;
        UserContext userContext = userService.getUserContext(humanPlayerId);
        // Check for level up
        int newXp = userContext.getXp() + questConfig.getXp();
        LevelEntity currentLevel = levelCrudPersistence.getEntity(userContext.getLevelId());
        if (newXp >= currentLevel.getXp2LevelUp()) {
            LevelEntity newLevel = levelCrudPersistence.getNextLevel(currentLevel);
            if (newLevel != null) {
                userContext.setLevelId(newLevel.getId());
                userContext.setXp(0);
                historyPersistence.get().onLevelUp(humanPlayerId, newLevel);
                List<LevelUnlockConfig> levelUnlockConfigs = serverUnlockService.gatherAvailableUnlocks(userContext, newLevel.getId());
                clientSystemConnectionService.onLevelUp(humanPlayerId, userContext, levelUnlockConfigs);
                serverGameEngineControlInstance.get().onLevelChanged(humanPlayerId, newLevel.getId());
                if (registered) {
                    userService.persistLevel(humanPlayerId.getUserId(), newLevel);
                    userService.persistXp(humanPlayerId.getUserId(), 0);
                }
            } else {
                logger.warning("No next level found for: " + currentLevel);
            }
        } else {
            userContext.setXp(newXp);
            if (registered) {
                userService.persistXp(humanPlayerId.getUserId(), newXp);
            }
            clientSystemConnectionService.onXpChanged(humanPlayerId, newXp);
        }
        // Activate next quest
        QuestConfig newQuest = null;
        if (registered) {
            userService.addCompletedServerQuest(humanPlayerId.getUserId(), questConfig);
            newQuest = userService.getAndSaveNewQuest(humanPlayerId.getUserId());
        } else {
            PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
            if (playerSession != null) {
                UnregisteredUser unregisteredUser = playerSession.getUnregisteredUser();
                unregisteredUser.addCompletedQuestId(questConfig.getId());
                QuestConfigEntity newQuestEntity = serverGameEngineCrudPersistence.getQuest4LevelAndCompleted(levelCrudPersistence.getEntity(playerSession.getUserContext().getLevelId()), unregisteredUser.getCompletedQuestIds());
                if (newQuestEntity != null) {
                    newQuest = newQuestEntity.toQuestConfig(playerSession.getLocale());
                }
                unregisteredUser.setActiveQuest(newQuest);
            }
        }
        if (newQuest != null) {
            historyPersistence.get().onQuest(humanPlayerId, newQuest, QuestHistoryEntity.Type.QUEST_ACTIVATED);
            clientSystemConnectionService.onQuestActivated(humanPlayerId, newQuest);
            questService.activateCondition(humanPlayerId, newQuest);
            clientSystemConnectionService.onQuestProgressInfo(humanPlayerId, questService.getQuestProgressInfo(humanPlayerId));
        }
    }

    public List<QuestConfig> readOpenQuestForDialog(UserContext userContext, Locale locale) {
        return serverGameEngineCrudPersistence.getQuests4Dialog(levelCrudPersistence.getEntity(userContext.getLevelId()), readActiveOrPassedQuestIds(userContext), locale);
    }

    @Transactional
    public List<Integer> readActiveOrPassedQuestIds(UserContext userContext) {
        List<Integer> ignoredQuests = new ArrayList<>();
        if (userContext.registered()) {
            ignoredQuests.addAll(userService.findActivePassedQuestId(userContext.getHumanPlayerId().getUserId()));
        } else {
            UnregisteredUser unregisteredUser = sessionService.findPlayerSession(userContext.getHumanPlayerId()).getUnregisteredUser();
            if (unregisteredUser.getCompletedQuestIds() != null) {
                ignoredQuests.addAll(unregisteredUser.getCompletedQuestIds());
            }
            if (unregisteredUser.getActiveQuest() != null) {
                ignoredQuests.add(unregisteredUser.getActiveQuest().getId());
            }
        }
        return ignoredQuests;
    }

    @Transactional // Needs to be @Transactional if a quest if fulfilled during activation and a new quest is activated
    public void activateQuest(UserContext userContext, int questId, Locale locale) {
        HumanPlayerId humanPlayerId = userContext.getHumanPlayerId();
        boolean registered = humanPlayerId.getUserId() != null;
        if (questService.hasActiveQuest(humanPlayerId)) {
            questService.deactivateActorCondition(humanPlayerId);
            clientSystemConnectionService.onQuestActivated(humanPlayerId, null);
            QuestConfig oldQuest;
            if (registered) {
                oldQuest = userService.getActiveQuest(humanPlayerId.getUserId(), locale);
                userService.clearActiveQuest(humanPlayerId.getUserId());
            } else {
                UnregisteredUser unregisteredUser = sessionService.findPlayerSession(humanPlayerId).getUnregisteredUser();
                oldQuest = unregisteredUser.getActiveQuest();
                unregisteredUser.setActiveQuest(null);
            }
            historyPersistence.get().onQuest(humanPlayerId, oldQuest, QuestHistoryEntity.Type.QUEST_DEACTIVATED);
        }
        QuestConfig newQuest = serverGameEngineCrudPersistence.getAndVerifyQuest(userContext.getLevelId(), questId, locale);
        if (readActiveOrPassedQuestIds(userContext).contains(newQuest.getId())) {
            throw new IllegalArgumentException("Given quest is passed");
        }

        if (registered) {
            userService.setActiveQuest(humanPlayerId.getUserId(), newQuest.getId());
        } else {
            sessionService.findPlayerSession(humanPlayerId).getUnregisteredUser().setActiveQuest(newQuest);
        }
        historyPersistence.get().onQuest(humanPlayerId, newQuest, QuestHistoryEntity.Type.QUEST_ACTIVATED);
        clientSystemConnectionService.onQuestActivated(humanPlayerId, newQuest);
        questService.activateCondition(humanPlayerId, newQuest);
        clientSystemConnectionService.onQuestProgressInfo(humanPlayerId, questService.getQuestProgressInfo(humanPlayerId));
    }
}