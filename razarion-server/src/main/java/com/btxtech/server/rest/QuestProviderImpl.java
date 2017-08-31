package com.btxtech.server.rest;

import com.btxtech.server.persistence.server.ServerLevelQuestService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.rest.QuestProvider;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.Path;
import java.util.List;

/**
 * Created by Beat
 * on 30.08.2017.
 */
public class QuestProviderImpl implements QuestProvider {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ServerLevelQuestService serverLevelQuestService;
    @Inject
    private SessionHolder sessionHolder;

    @Override
    public List<QuestConfig> readMyOpenQuests() {
        try {
            return serverLevelQuestService.readOpenQuestForDialog(sessionHolder.getPlayerSession().getUserContext(), sessionHolder.getPlayerSession().getLocale());
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void activateQuest(int questId) {
        try {
            serverLevelQuestService.activateQuest(sessionHolder.getPlayerSession().getUserContext(), questId, sessionHolder.getPlayerSession().getLocale());
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
