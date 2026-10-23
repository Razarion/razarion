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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * QuestService reports a passed quest from inside the planet tick. Nothing of what follows - the user
 * read and written some ten times in MariaDB, user_activity saved to Mongo Atlas in another region -
 * may happen there: it is handed to the EngineEventExecutor and done after the tick.
 */
class ServerLevelQuestServiceOffTickTest {
    private static final String USER_ID = "user-1";

    @Test
    void aPassedQuestTouchesNoDatabaseInTheTick() {
        UserService userService = mock(UserService.class);
        LevelCrudService levelCrudService = mock(LevelCrudService.class);
        UserActivityService userActivityService = mock(UserActivityService.class);
        QueuedEngineEventExecutor executor = new QueuedEngineEventExecutor(false);
        ServerLevelQuestService serverLevelQuestService = new ServerLevelQuestService(mock(QuestService.class),
                mock(ServerGameEngineService.class),
                levelCrudService,
                userService,
                mock(ClientSystemConnectionService.class),
                () -> mock(ServerGameEngineControl.class),
                mock(ServerUnlockService.class),
                mock(QuestConfigService.class),
                userActivityService,
                mock(RedditConversionService.class),
                mock(XConversionService.class),
                mock(MetaConversionService.class),
                mock(HistoryService.class),
                executor);
        LevelEntity level2 = mock(LevelEntity.class);
        when(level2.getXp2LevelUp()).thenReturn(30);
        when(levelCrudService.getEntity(2)).thenReturn(level2);
        when(userService.getUserContextTransactional(USER_ID)).thenReturn(new UserContext().userId(USER_ID).levelId(2).xp(0));
        when(userService.findActiveQuestConfig4CurrentUser(USER_ID)).thenReturn(new QuestConfig());

        serverLevelQuestService.onQuestPassed(USER_ID, new QuestConfig().id(363).xp(10));

        verifyNoInteractions(userService, levelCrudService, userActivityService);
        assertEquals(1, executor.queuedCount());

        executor.runQueued();

        verify(userService).persistXp(USER_ID, 10);
        verify(userActivityService).onQuestPassed(anyString(), anyInt(), anyInt());
    }
}
