package com.btxtech.server.gameengine;

import com.btxtech.server.service.engine.EngineEventExecutor;
import com.btxtech.server.service.engine.PlanetBackupService;
import com.btxtech.server.service.engine.PlanetCrudService;
import com.btxtech.server.service.engine.ServerGameEngineService;
import com.btxtech.server.service.engine.ServerInventoryService;
import com.btxtech.server.service.engine.ServerLevelQuestService;
import com.btxtech.server.service.engine.StaticGameConfigService;
import com.btxtech.server.service.history.HistoryService;
import com.btxtech.server.service.tracking.MetaConversionService;
import com.btxtech.server.service.tracking.RedditConversionService;
import com.btxtech.server.service.tracking.UserActivityService;
import com.btxtech.server.service.tracking.XConversionService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BoxService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Quest rewards and level changes come from the EngineEventExecutor, not from the tick. They change the
 * world only at the end of the next tick, on the tick thread: a base's Razarion changed from the side
 * could overwrite, or be overwritten by, what a harvester or a build did in the tick at the same moment.
 */
class ServerGameEngineControlInTickTest {
    private static final String USER_ID = "user-1";
    private final BaseItemService baseItemService = mock(BaseItemService.class);
    private final ServerGameEngineControl control = new ServerGameEngineControl(mock(InitializeService.class),
            mock(PlanetService.class),
            mock(BoxService.class),
            mock(StaticGameConfigService.class),
            mock(GameLogicService.class),
            mock(ClientGameConnectionService.class),
            mock(ClientSystemConnectionService.class),
            mock(ServerGameEngineService.class),
            mock(PlanetCrudService.class),
            mock(QuestService.class),
            mock(UserService.class),
            baseItemService,
            mock(BotService.class),
            mock(ResourceService.class),
            mock(PlanetBackupService.class),
            mock(UserActivityService.class),
            mock(RedditConversionService.class),
            mock(XConversionService.class),
            mock(MetaConversionService.class),
            mock(ServerInventoryService.class),
            mock(HistoryService.class),
            () -> mock(ServerLevelQuestService.class),
            mock(EngineEventExecutor.class));

    @Test
    void aQuestRewardIsPaidInTheTick() {
        PlayerBaseFull playerBase = mock(PlayerBaseFull.class);
        when(baseItemService.getPlayerBaseFull4UserId(USER_ID)).thenReturn(playerBase);

        control.addRazarion(USER_ID, 50);
        control.onLevelChanged(USER_ID, 7);

        verifyNoInteractions(baseItemService, playerBase);

        control.onPostTick(null);

        verify(playerBase).addResource(50);
        verify(baseItemService).updateLevel(USER_ID, 7);
    }
}
