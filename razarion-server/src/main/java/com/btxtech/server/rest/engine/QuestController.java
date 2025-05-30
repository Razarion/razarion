package com.btxtech.server.rest.engine;

import com.btxtech.server.service.engine.ServerLevelQuestService;
import com.btxtech.server.user.UserService;
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
    private final UserService userService;
    private final ServerLevelQuestService serverLevelQuestService;

    public QuestController(UserService userService, ServerLevelQuestService serverLevelQuestService) {
        this.userService = userService;
        this.serverLevelQuestService = serverLevelQuestService;
    }

    @GetMapping(value = "readMyOpenQuests", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<QuestConfig> readMyOpenQuests() {
        try {

            var userContext = userService.getUserContextFromContext();
            return serverLevelQuestService.readOpenQuestForDialog(userContext);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping(value = "activateQuest/{id}")
    public void activateQuest(@PathVariable("id") int questId) {
        try {
            var userContext = userService.getUserContextFromContext();
            serverLevelQuestService.activateQuest(userContext, questId);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping(value = "activateNextPossibleQuest")
    public void activateNextPossibleQuest() {
        try {
            var userId = userService.getOrCreateUserIdFromContext();
            serverLevelQuestService.activateNextPossibleQuest(userId);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

}
