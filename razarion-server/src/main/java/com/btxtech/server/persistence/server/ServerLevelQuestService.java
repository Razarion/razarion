package com.btxtech.server.persistence.server;

import com.btxtech.server.connection.ClientSystemConnectionService;
import com.btxtech.server.persistence.GameUiControlConfigEntity;
import com.btxtech.server.persistence.GameUiControlConfigPersistence;
import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.persistence.history.QuestHistoryEntity;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelPersistence;
import com.btxtech.server.persistence.scene.SceneEntity;
import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.user.UnregisteredUser;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.SceneConfig;
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
import java.util.Locale;

/**
 * Created by Beat
 * on 09.08.2017.
 */
@Singleton
public class ServerLevelQuestService implements QuestListener {
    // private Logger logger = Logger.getLogger(ServerLevelQuestService.class.getName());
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

    @PostConstruct
    public void init() {
        questService.addQuestListener(this);
    }

    @Transactional
    public void onLevelUpdate(String sessionId, int newLevelId) {
        LevelEntity newLevel = levelPersistence.getLevel4Id(newLevelId);
        PlayerSession playerSession = sessionService.getSession(sessionId);
        UserContext userContext = playerSession.getUserContext();
        historyPersistence.get().onLevelUp(userContext.getHumanPlayerId(), newLevel);

        // Temporary: Only save the level if on multiplayer planet. Main reason, tutorial state und units are not saved.
        if (gameUiControlConfigPersistence.get().load4Level(newLevelId).getGameEngineMode() == GameEngineMode.SLAVE) {
            userContext.setLevelId(newLevelId);
            QuestConfig newQuest;
            if (userContext.getHumanPlayerId().getUserId() != null) {
                userService.persistLevel(userContext.getHumanPlayerId().getUserId(), newLevel);
                newQuest = userService.getAndSaveNewQuest(userContext.getHumanPlayerId().getUserId());
            } else {
                UnregisteredUser unregisteredUser = sessionService.getSession(sessionId).getUnregisteredUser();
                newQuest = serverGameEnginePersistence.getQuest4LevelAndCompleted(newLevel, unregisteredUser.getCompletedQuestIds()).toQuestConfig(playerSession.getLocale());
                unregisteredUser.setActiveQuest(newQuest);
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
        QuestConfig newQuest = null;
        if (humanPlayerId.getUserId() != null) {
            userService.addCompletedServerQuest(humanPlayerId.getUserId(), questConfig);
            newQuest = userService.getAndSaveNewQuest(humanPlayerId.getUserId());
        } else {
            PlayerSession playerSession = sessionService.findPlayerSession(humanPlayerId);
            if (playerSession != null) {
                UnregisteredUser unregisteredUser = playerSession.getUnregisteredUser();
                unregisteredUser.addCompletedQuestId(questConfig.getId());
                newQuest = serverGameEnginePersistence.getQuest4LevelAndCompleted(levelPersistence.getLevel4Id(playerSession.getUserContext().getLevelId()), unregisteredUser.getCompletedQuestIds()).toQuestConfig(playerSession.getLocale());
                unregisteredUser.setActiveQuest(newQuest);
            }
        }
        if (newQuest != null) {
            historyPersistence.get().onQuest(humanPlayerId, newQuest, QuestHistoryEntity.Type.QUEST_ACTIVATED);
            questService.activateCondition(humanPlayerId, newQuest);
            clientSystemConnectionService.onQuestActivated(humanPlayerId, newQuest);
        }
    }
}