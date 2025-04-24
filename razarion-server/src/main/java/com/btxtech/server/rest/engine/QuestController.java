package com.btxtech.server.rest.engine;

import com.btxtech.server.rest.ui.GameUiContextControllerImpl;
import com.btxtech.server.service.engine.ServerLevelQuestService;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.rest.QuestAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.btxtech.shared.rest.QuestAccess.PATH;

@RestController
@RequestMapping("/rest" + PATH)
public class QuestController implements QuestAccess {
    private final Logger logger = LoggerFactory.getLogger(QuestController.class);
    private final ServerLevelQuestService serverLevelQuestService;
    private final SessionService sessionService;

    public QuestController(ServerLevelQuestService serverLevelQuestService, SessionService sessionService) {
        this.serverLevelQuestService = serverLevelQuestService;
        this.sessionService = sessionService;
    }

    @GetMapping(value = "readMyOpenQuests", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<QuestConfig> readMyOpenQuests() {
        try {

            var playerSession = sessionService.getSession(GameUiContextControllerImpl.getCurrentHttpSessionId());
            return serverLevelQuestService.readOpenQuestForDialog(playerSession.getUserContext());
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping(value = "activateQuest/{id}")
    public void activateQuest(@PathVariable("id") int questId) {
        try {
            var playerSession = sessionService.getSession(GameUiContextControllerImpl.getCurrentHttpSessionId());
            serverLevelQuestService.activateQuest(playerSession.getUserContext(), questId);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping(value = "activateNextPossibleQuest")
    public void activateNextPossibleQuest() {
        try {
            var playerSession = sessionService.getSession(GameUiContextControllerImpl.getCurrentHttpSessionId());
            serverLevelQuestService.activateNextPossibleQuest(playerSession.getUserContext().getUserId());
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

}
