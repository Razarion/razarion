package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.GameUiControlConfigPersistence;
import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelPersistence;
import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.user.UnregisteredUser;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.SlaveQuestInfo;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.planet.quest.QuestService;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 09.08.2017.
 */
@Singleton
public class ServerLevelQuestService {
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

    @Transactional
    public void onLevelUpdate(String sessionId, int newLevelId) {
        LevelEntity newLevel = levelPersistence.getLevel4Id(newLevelId);
        PlayerSession playerSession = sessionService.getSession(sessionId);
        UserContext userContext = playerSession.getUserContext();
        historyPersistence.get().onLevelUp(userContext.getHumanPlayerId(), newLevel);

        // Temporary: Only save the level if on multiplayer planet. Main reason, tutorial state und units are not saved.
        if (gameUiControlConfigPersistence.get().load4Level(newLevelId).getGameEngineMode() == GameEngineMode.SLAVE) {
            userContext.setLevelId(newLevelId);
            QuestConfig newQuest = null;
            if (userContext.getHumanPlayerId().getUserId() != null) {
                userService.persistLevel(userContext.getHumanPlayerId().getUserId(), newLevel);
                newQuest = userService.getAndSaveNewQuest(userContext.getHumanPlayerId().getUserId(), playerSession.getLocale());
            } else {
                UnregisteredUser unregisteredUser = sessionService.getSession(sessionId).getUnregisteredUser();
                newQuest = serverGameEnginePersistence.getQuestConfigEntityUnregisteredUser(newLevel, unregisteredUser.getCompletedQuestIds()).toQuestConfig(playerSession.getLocale());
                unregisteredUser.setActiveQuest(newQuest);
            }
            if (newQuest != null) {
                questService.activateCondition(userContext.getHumanPlayerId(), newQuest);
            }
        }
    }

    public SlaveQuestInfo getSlaveQuestInfo(Locale locale, HumanPlayerId humanPlayerId) {
        SlaveQuestInfo slaveQuestInfo = new SlaveQuestInfo();
        slaveQuestInfo.setActiveQuest(userService.findActiveQuestConfig4CurrentUser(locale));
        slaveQuestInfo.setQuestProgressInfo(questService.getQuestProgressInfo(humanPlayerId));
        return slaveQuestInfo;
    }

}
