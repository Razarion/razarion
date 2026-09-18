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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The Razarion reward of a quest was configurable in the editor and never paid - only the xp was.
 * The beginner quests 363 and 366 now carry one, because the tipped chain of levels 1-7 costs more
 * Razarion than the start gives.
 */
class ServerLevelQuestServiceRewardTest {
    private static final String USER_ID = "user-1";

    private final UserService userService = mock(UserService.class);
    private final LevelCrudService levelCrudService = mock(LevelCrudService.class);
    private final ServerGameEngineControl serverGameEngineControl = mock(ServerGameEngineControl.class);

    private final ServerLevelQuestService serverLevelQuestService = new ServerLevelQuestService(mock(QuestService.class),
            mock(ServerGameEngineService.class),
            levelCrudService,
            userService,
            mock(ClientSystemConnectionService.class),
            () -> serverGameEngineControl,
            mock(ServerUnlockService.class),
            mock(QuestConfigService.class),
            mock(UserActivityService.class),
            mock(RedditConversionService.class),
            mock(XConversionService.class),
            mock(MetaConversionService.class),
            mock(HistoryService.class));

    @BeforeEach
    void setUp() {
        LevelEntity level2 = mock(LevelEntity.class);
        when(level2.getNumber()).thenReturn(2);
        when(level2.getXp2LevelUp()).thenReturn(30);
        when(levelCrudService.getEntity(2)).thenReturn(level2);
        when(userService.getUserContextTransactional(USER_ID)).thenReturn(new UserContext().userId(USER_ID).levelId(2).xp(0));
        // A quest still running, so passing one does not walk into the dead-end handling.
        when(userService.findActiveQuestConfig4CurrentUser(USER_ID)).thenReturn(new QuestConfig());
    }

    @Test
    void paysTheRazarionReward() {
        serverLevelQuestService.onQuestPassed(USER_ID, new QuestConfig().id(363).xp(5).razarion(50));

        verify(serverGameEngineControl).addRazarion(USER_ID, 50);
    }

    @Test
    void aQuestWithoutRewardDoesNotTouchTheBase() {
        serverLevelQuestService.onQuestPassed(USER_ID, new QuestConfig().id(364).xp(10));

        verify(serverGameEngineControl, never()).addRazarion(anyString(), anyInt());
    }
}
