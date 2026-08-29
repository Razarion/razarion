package com.btxtech.server.service.engine;

import com.btxtech.server.gameengine.ClientSystemConnectionService;
import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.model.engine.LevelEntity;
import com.btxtech.server.service.history.HistoryService;
import com.btxtech.server.service.tracking.MetaConversionService;
import com.btxtech.server.service.tracking.RedditConversionService;
import com.btxtech.server.service.tracking.UserActivityService;
import com.btxtech.server.service.tracking.XConversionService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import jakarta.inject.Provider;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Quests are the only source of xp and only quests up to the player's own level are reachable, so a
 * player who has passed all of them without earning the level is stuck with nothing left to do. That
 * happened in production on 2026-08-14 and is what {@code activateNextPossibleQuest} now catches.
 */
class ServerLevelQuestServiceDeadEndTest {
    private static final String USER_ID = "user-1";

    private final QuestService questService = mock(QuestService.class);
    private final ServerGameEngineService serverGameEngineService = mock(ServerGameEngineService.class);
    private final LevelCrudService levelCrudService = mock(LevelCrudService.class);
    private final UserService userService = mock(UserService.class);
    private final ClientSystemConnectionService clientSystemConnectionService = mock(ClientSystemConnectionService.class);
    private final ServerGameEngineControl serverGameEngineControl = mock(ServerGameEngineControl.class);
    private final ServerUnlockService serverUnlockService = mock(ServerUnlockService.class);
    private final QuestConfigService questConfigService = mock(QuestConfigService.class);
    private final UserActivityService userActivityService = mock(UserActivityService.class);
    private final RedditConversionService redditConversionService = mock(RedditConversionService.class);
    private final XConversionService xConversionService = mock(XConversionService.class);
    private final MetaConversionService metaConversionService = mock(MetaConversionService.class);
    private final HistoryService historyService = mock(HistoryService.class);

    private final ServerLevelQuestService serverLevelQuestService = new ServerLevelQuestService(questService,
            serverGameEngineService,
            levelCrudService,
            userService,
            clientSystemConnectionService,
            providerOf(serverGameEngineControl),
            serverUnlockService,
            questConfigService,
            userActivityService,
            redditConversionService,
            xConversionService,
            metaConversionService,
            historyService);

    @Test
    void playerWithoutQuestsLeftIsLevelledUpAndGetsTheNextLevelsQuest() {
        LevelEntity level4 = level(271, 4, 40);
        LevelEntity level5 = level(273, 5, 40);
        QuestConfig level5Quest = new QuestConfig();
        // Nothing open at level 4 any more, the first ask comes back empty; after the level-up the
        // quests of level 5 are in reach.
        when(userService.getAndSaveNewQuest(USER_ID)).thenReturn(null, level5Quest);
        when(userService.findActiveQuestConfig4CurrentUser(USER_ID)).thenReturn(null);
        when(userService.getUserContextTransactional(USER_ID))
                .thenReturn(new UserContext().userId(USER_ID).levelId(271).xp(20));
        when(levelCrudService.getEntity(271)).thenReturn(level4);
        when(levelCrudService.getNextLevel(level4)).thenReturn(level5);

        serverLevelQuestService.activateNextPossibleQuest(USER_ID);

        verify(userService).persistLevel(USER_ID, level5);
        verify(userService).persistXp(USER_ID, 0);
        verify(clientSystemConnectionService).onQuestActivated(USER_ID, level5Quest);
        // The message the stuck player actually saw: "this is the end, more quests will be added later".
        verify(clientSystemConnectionService, never()).onAllQuestsCompleted(USER_ID);
    }

    @Test
    void aRunningQuestIsNotADeadEnd() {
        // getAndSaveNewQuest answers null while a quest is still active too, and the client asks on
        // every reconnect. Levelling up on that would hand out free levels.
        when(userService.getAndSaveNewQuest(USER_ID)).thenReturn(null);
        when(userService.findActiveQuestConfig4CurrentUser(USER_ID)).thenReturn(new QuestConfig());

        serverLevelQuestService.activateNextPossibleQuest(USER_ID);

        verify(userService, never()).persistLevel(eq(USER_ID), any());
        verify(levelCrudService, never()).getNextLevel(any());
    }

    @Test
    void onTheTopLevelTheEmptyQuestListIsTheEndOfTheContent() {
        LevelEntity topLevel = level(290, 21, 200);
        when(userService.getAndSaveNewQuest(USER_ID)).thenReturn(null);
        when(userService.findActiveQuestConfig4CurrentUser(USER_ID)).thenReturn(null);
        when(userService.getUserContextTransactional(USER_ID))
                .thenReturn(new UserContext().userId(USER_ID).levelId(290).xp(0));
        when(levelCrudService.getEntity(290)).thenReturn(topLevel);
        when(levelCrudService.getNextLevel(topLevel)).thenReturn(null);

        serverLevelQuestService.activateNextPossibleQuest(USER_ID);

        verify(userService, never()).persistLevel(eq(USER_ID), any());
        verify(clientSystemConnectionService).onAllQuestsCompleted(USER_ID);
    }

    private LevelEntity level(int id, int number, int xp2LevelUp) {
        LevelEntity levelEntity = mock(LevelEntity.class);
        when(levelEntity.getId()).thenReturn(id);
        when(levelEntity.getNumber()).thenReturn(number);
        when(levelEntity.getXp2LevelUp()).thenReturn(xp2LevelUp);
        return levelEntity;
    }

    private Provider<ServerGameEngineControl> providerOf(ServerGameEngineControl serverGameEngineControl) {
        return () -> serverGameEngineControl;
    }
}
