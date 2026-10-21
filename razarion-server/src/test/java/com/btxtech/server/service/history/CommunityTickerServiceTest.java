package com.btxtech.server.service.history;

import com.btxtech.server.model.history.GameHistory;
import com.btxtech.server.model.history.GameHistorySource;
import com.btxtech.server.model.history.GameHistoryType;
import com.btxtech.server.rest.CallSign;
import com.btxtech.shared.dto.CommunityTickerKind;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Which history rows make a ticker line. No Mongo: the warm-up fails quietly without one, which is
 * what it must do on a start without a database anyway.
 */
class CommunityTickerServiceTest {

    private static GameHistory human(GameHistoryType type, String userId) {
        return new GameHistory().type(type).source(GameHistorySource.HUMAN).userId(userId).serverTime(new Date());
    }

    private static List<CommunityTickerKind> kinds(CommunityTickerService service) {
        return service.recent(0, 100).stream().map(CommunityTickerService.Event::kind).toList();
    }

    @Test
    void takesWhatOthersShouldSee() {
        CommunityTickerService service = new CommunityTickerService(null);
        service.onHistory(human(GameHistoryType.BASE_CREATED, "a"));
        service.onHistory(human(GameHistoryType.ANONYMOUS_USER_RETURNED, "a"));
        service.onHistory(human(GameHistoryType.LEVEL_UP, "a").levelNumber(5));
        service.onHistory(human(GameHistoryType.QUEST_PASSED, "a").questId(CommunityTickerService.LEFT_NOOB_ISLAND_QUEST_ID));
        service.onHistory(human(GameHistoryType.ITEM_DESTROYED, "a").itemTypeName("(Bot2) Spire").targetBaseName("RazCore Spire"));
        service.onHistory(human(GameHistoryType.BASE_DEFEATED, "a").targetBaseName("RazCore Extractor"));
        service.onHistory(human(GameHistoryType.ITEM_CREATED, "a").itemTypeName("Hydra"));

        assertEquals(List.of(CommunityTickerKind.LANDED, CommunityTickerKind.RETURNED, CommunityTickerKind.LEVEL_UP,
                CommunityTickerKind.LEFT_NOOB_ISLAND, CommunityTickerKind.DESTROYED, CommunityTickerKind.DEFEATED_BOT_BASE,
                CommunityTickerKind.FIRST_UNIT), kinds(service));
    }

    @Test
    void leavesOutNoiseAndBots() {
        CommunityTickerService service = new CommunityTickerService(null);
        service.onHistory(human(GameHistoryType.QUEST_PASSED, "a").questId(358));
        // A player's own factory sold: HUMAN too, but the player is the victim
        service.onHistory(human(GameHistoryType.ITEM_DESTROYED, "a").itemTypeName("Factory").targetUserId("a"));
        service.onHistory(new GameHistory().type(GameHistoryType.BASE_CREATED).source(GameHistorySource.BOT).serverTime(new Date()));
        // A player's base removed without a fight: no actor
        service.onHistory(new GameHistory().type(GameHistoryType.BASE_DEFEATED).source(GameHistorySource.HUMAN).targetUserId("a").serverTime(new Date()));

        assertTrue(kinds(service).isEmpty(), kinds(service).toString());
    }

    @Test
    void everyLevelEveryBuildAndEveryKillButNotAFlood() {
        CommunityTickerService service = new CommunityTickerService(null);
        service.onHistory(human(GameHistoryType.LEVEL_UP, "a").levelNumber(2));
        service.onHistory(human(GameHistoryType.ITEM_CREATED, "a").itemTypeName("Viper"));
        service.onHistory(human(GameHistoryType.ITEM_CREATED, "a").itemTypeName("Viper"));
        service.onHistory(human(GameHistoryType.ITEM_CREATED, "a").itemTypeName("Factory"));
        service.onHistory(human(GameHistoryType.ITEM_CREATED, "b").itemTypeName("Viper"));
        service.onHistory(human(GameHistoryType.ITEM_DESTROYED, "a").itemTypeName("(Bot1) Refinery"));
        service.onHistory(human(GameHistoryType.ITEM_DESTROYED, "a").itemTypeName("(Bot1) Tower"));
        // Another player's item counts; a Spire always
        service.onHistory(human(GameHistoryType.ITEM_DESTROYED, "b").itemTypeName("Viper").targetUserId("a"));
        service.onHistory(human(GameHistoryType.ITEM_DESTROYED, "a").itemTypeName("(Bot2) Spire"));

        assertEquals(List.of(CommunityTickerKind.LEVEL_UP, CommunityTickerKind.BUILT, CommunityTickerKind.BUILT,
                CommunityTickerKind.BUILT, CommunityTickerKind.DESTROYED, CommunityTickerKind.DESTROYED,
                CommunityTickerKind.DESTROYED), kinds(service));
    }

    @Test
    void oneBotBaseAndOneFirstUnitPerPlayer() {
        CommunityTickerService service = new CommunityTickerService(null);
        service.onHistory(human(GameHistoryType.BASE_DEFEATED, "a").targetBaseName("RazCore Extractor"));
        service.onHistory(human(GameHistoryType.BASE_DEFEATED, "a").targetBaseName("RazCore Garrison"));
        service.onHistory(human(GameHistoryType.BASE_DEFEATED, "b").targetBaseName("RazCore Extractor"));
        service.onHistory(human(GameHistoryType.ITEM_CREATED, "a").itemTypeName("Hydra"));
        service.onHistory(human(GameHistoryType.ITEM_CREATED, "a").itemTypeName("Hydra"));

        assertEquals(List.of(CommunityTickerKind.DEFEATED_BOT_BASE, CommunityTickerKind.DEFEATED_BOT_BASE,
                CommunityTickerKind.FIRST_UNIT), kinds(service));
    }

    @Test
    void asksAfterTheLastOneItHas() {
        CommunityTickerService service = new CommunityTickerService(null);
        service.onHistory(human(GameHistoryType.BASE_CREATED, "a"));
        long first = service.recent(0, 100).get(0).seq();
        service.onHistory(human(GameHistoryType.BASE_CREATED, "b"));

        assertEquals(1, service.recent(first, 100).size());
        // A number from before a restart: the latest again, not nothing forever
        assertEquals(2, service.recent(first + 1000, 100).size());
    }

    @Test
    void callSignIsFixedPerPlayer() {
        assertEquals(CallSign.of("e8b69365-ac63-4e74-a270-ae1d893eb86f"), CallSign.of("e8b69365-ac63-4e74-a270-ae1d893eb86f"));
        assertNotEquals(CallSign.of("e8b69365-ac63-4e74-a270-ae1d893eb86f"), CallSign.of("c02bae5b-8ab0-4a34-ac0c-a49ae3ddce21"));
        assertTrue(CallSign.of("x").matches("[A-Z][a-z]+-\\d{2}"), CallSign.of("x"));
    }
}
