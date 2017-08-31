package com.btxtech.server.persistence.server;

import com.btxtech.server.connection.ClientSystemConnectionService;
import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.persistence.GameUiControlConfigPersistence;
import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.persistence.history.QuestHistoryEntity;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelPersistence;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.user.UnregisteredUser;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.SlaveQuestInfo;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
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
    private Instance<GameUiControlConfigPersistence> gameUiControlConfigPersistence;
    @Inject
    private Instance<HistoryPersistence> historyPersistence;
    @Inject
    private SessionService sessionService;
    @Inject
    private QuestService questService;
    @Inject
    private ServerGameEnginePersistence serverGameEnginePersistence;
    @Inject
    private LevelPersistence levelPersistence;
    @Inject
    private UserService userService;
    @Inject
    private ClientSystemConnectionService clientSystemConnectionService;
    @Inject
    private Instance<ServerGameEngineControl> serverGameEngineControlInstance;

    @PostConstruct
    public void init() {
        questService.addQuestListener(this);
    }

    @Transactional
    public void onClientLevelUpdate(String sessionId, int newLevelId) {
        LevelEntity newLevel = levelPersistence.getLevel4Id(newLevelId);
        PlayerSession playerSession = sessionService.getSession(sessionId);
        UserContext userContext = playerSession.getUserContext();
        historyPersistence.get().onLevelUp(userContext.getHumanPlayerId(), newLevel);

        // Temporary: Only save the level if on multiplayer planet. Main reason, tutorial state und units are not saved.
        // This is only called from the client.
        if (gameUiControlConfigPersistence.get().load4Level(newLevelId).getGameEngineMode() == GameEngineMode.SLAVE) {
            boolean activeQuest = questService.hasActiveQuest(userContext.getHumanPlayerId());
            userContext.setLevelId(newLevelId);
            QuestConfig newQuest = null;
            if (userContext.getHumanPlayerId().getUserId() != null) {
                userService.persistLevel(userContext.getHumanPlayerId().getUserId(), newLevel);
                if (!activeQuest) {
                    newQuest = userService.getAndSaveNewQuest(userContext.getHumanPlayerId().getUserId());
                }
            } else {
                if (!activeQuest) {
                    UnregisteredUser unregisteredUser = sessionService.getSession(sessionId).getUnregisteredUser();
                    newQuest = serverGameEnginePersistence.getQuest4LevelAndCompleted(newLevel, unregisteredUser.getCompletedQuestIds()).toQuestConfig(playerSession.getLocale());
                    unregisteredUser.setActiveQuest(newQuest);
                }
            }
            if (newQuest != null) {
                historyPersistence.get().onQuest(userContext.getHumanPlayerId(), newQuest, QuestHistoryEntity.Type.QUEST_ACTIVATED);
                questService.activateCondition(userContext.getHumanPlayerId(), newQuest);
                clientSystemConnectionService.onQuestActivated(userContext.getHumanPlayerId(), newQuest);
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
        UserContext userContext;
        if (registered) {
            PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
            if (playerSession != null) {
                userContext = playerSession.getUserContext();
            } else {
                userContext = userService.getUserEntity(humanPlayerId.getUserId()).toUserContext();
            }
        } else {
            PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
            if (playerSession == null) {
                // Unregistered user is no longer online
                return;
            }
            userContext = playerSession.getUserContext();
        }
        // Check for level up
        int newXp = userContext.getXp() + questConfig.getXp();
        LevelEntity currentLevel = levelPersistence.getLevel4Id(userContext.getLevelId());
        if (newXp >= currentLevel.getXp2LevelUp()) {
            LevelEntity newLevel = levelPersistence.getNextLevel(currentLevel);
            if (newLevel != null) {
                userContext.setLevelId(newLevel.getId());
                userContext.setXp(0);
                historyPersistence.get().onLevelUp(humanPlayerId, newLevel);
                clientSystemConnectionService.onLevelUp(humanPlayerId, userContext);
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
                QuestConfigEntity newQuestEntity = serverGameEnginePersistence.getQuest4LevelAndCompleted(levelPersistence.getLevel4Id(playerSession.getUserContext().getLevelId()), unregisteredUser.getCompletedQuestIds());
                if (newQuestEntity != null) {
                    newQuest = newQuestEntity.toQuestConfig(playerSession.getLocale());
                }
                unregisteredUser.setActiveQuest(newQuest);
            }
        }
        if (newQuest != null) {
            historyPersistence.get().onQuest(humanPlayerId, newQuest, QuestHistoryEntity.Type.QUEST_ACTIVATED);
            questService.activateCondition(humanPlayerId, newQuest);
            clientSystemConnectionService.onQuestActivated(humanPlayerId, newQuest);
        }
    }

    public List<QuestConfig> readOpenQuestForDialog(UserContext userContext, Locale locale) {
        return serverGameEnginePersistence.getQuests4Dialog(levelPersistence.getLevel4Id(userContext.getLevelId()), readActiveOrPassedQuestIds(userContext), locale);
    }

    @Transactional
    public List<Integer> readActiveOrPassedQuestIds(UserContext userContext) {
        List<Integer> ignoredQuests = new ArrayList<>();
        if (userContext.getHumanPlayerId().getUserId() != null) {
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
        QuestConfig newQuest = serverGameEnginePersistence.getAndVerifyQuest(userContext.getLevelId(), questId, locale);
        if (readActiveOrPassedQuestIds(userContext).contains(newQuest.getId())) {
            throw new IllegalArgumentException("Given quest is passed");
        }

        if (registered) {
            userService.setActiveQuest(humanPlayerId.getUserId(), newQuest.getId());
        } else {
            sessionService.findPlayerSession(humanPlayerId).getUnregisteredUser().setActiveQuest(newQuest);
        }
        historyPersistence.get().onQuest(humanPlayerId, newQuest, QuestHistoryEntity.Type.QUEST_ACTIVATED);
        questService.activateCondition(humanPlayerId, newQuest);
        clientSystemConnectionService.onQuestActivated(humanPlayerId, newQuest);
    }
}