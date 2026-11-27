package com.btxtech.server.service;

import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.service.history.CommunityTickerService;
import com.btxtech.server.service.tracking.UserActivityService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.dto.CommunityTickerKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheerServiceTest {
    private CommunityTickerService ticker;
    private UserService userService;
    private ServerGameEngineControl engine;
    private CheerService cheerService;
    private final Map<String, Integer> levels = new HashMap<>();
    private String asking;
    /** The pairs CheerBonusStore would have in Mongo. */
    private final Set<String> paidPairs = new HashSet<>();

    @BeforeEach
    void setUp() {
        ticker = mock(CommunityTickerService.class);
        userService = mock(UserService.class);
        engine = mock(ServerGameEngineControl.class);
        UserActivityService activity = mock(UserActivityService.class);
        when(userService.getUserIdFromContextOrNull()).thenAnswer(i -> asking);
        when(userService.hasOpenSystemConnection(anyString())).thenReturn(true);
        when(userService.getUserNames(any())).thenReturn(Map.of("helper", "Helper", "newbie", "Newbie"));
        when(userService.getUserLevelNumbers(any())).thenAnswer(i -> levels);
        CheerBonusStore store = mock(CheerBonusStore.class);
        when(store.claim(anyString(), anyString(), anyInt())).thenAnswer(i -> paidPairs.add(i.getArgument(0) + ">" + i.getArgument(1)));
        cheerService = new CheerService(ticker, userService, activity, engine, store);
        when(ticker.find(1)).thenReturn(new CommunityTickerService.Event(1, 0, "newbie", CommunityTickerKind.LANDED, null, null));
    }

    @Test
    void aGreetingFromTheTickerReachesThePlayerOnceAndCanBeAnswered() {
        asking = "helper";
        assertEquals(CheerService.Result.SENT, cheerService.greet(1));

        List<CheerService.Delivered> inbox = cheerService.take("newbie");
        assertEquals(1, inbox.size());
        assertEquals("Helper", inbox.get(0).from());
        assertEquals("WAVE", inbox.get(0).emote());
        assertTrue(cheerService.take("newbie").isEmpty(), "handed out once");

        asking = "newbie";
        assertEquals(CheerService.Result.SENT, cheerService.answer(inbox.get(0).id()));
        List<CheerService.Delivered> back = cheerService.take("helper");
        assertEquals(1, back.size());
        assertTrue(back.get(0).reply());
        assertEquals(CheerService.Result.UNKNOWN, cheerService.answer(back.get(0).id()), "a reply is not answered again");
    }

    @Test
    void theHelperOffTheIslandEarnsTheBonusOnceWhenTheNewcomerAnswers() {
        levels.put("helper", 12);
        levels.put("newbie", 3);
        asking = "helper";
        cheerService.greet(1);
        long id = cheerService.take("newbie").get(0).id();
        asking = "newbie";
        cheerService.answer(id);

        assertEquals(CheerService.HELPER_BONUS, cheerService.take("helper").get(0).bonus());
        verify(engine).addRazarion("helper", CheerService.HELPER_BONUS);
        assertEquals(0, cheerService.helperBonus("helper", "newbie"), "once per newcomer");
    }

    @Test
    void aPairPaidBeforeARestartIsNotPaidAgain() {
        paidPairs.add("helper>newbie");
        levels.put("helper", 12);
        levels.put("newbie", 3);
        assertEquals(0, cheerService.helperBonus("helper", "newbie"));
        verify(engine, never()).addRazarion(anyString(), anyInt());
    }

    @Test
    void noBonusBetweenTwoNewcomers() {
        levels.put("helper", 4);
        levels.put("newbie", 3);
        asking = "helper";
        cheerService.greet(1);
        long id = cheerService.take("newbie").get(0).id();
        asking = "newbie";
        cheerService.answer(id);

        assertEquals(0, cheerService.take("helper").get(0).bonus());
        verify(engine, never()).addRazarion(anyString(), anyInt());
    }

    @Test
    void notToSomebodyOfflineNorTwiceInARowNorToOneself() {
        asking = "helper";
        when(userService.hasOpenSystemConnection("newbie")).thenReturn(false);
        assertEquals(CheerService.Result.NOT_ONLINE, cheerService.greet(1));
        when(userService.hasOpenSystemConnection("newbie")).thenReturn(true);
        assertEquals(CheerService.Result.SENT, cheerService.greet(1));
        assertEquals(CheerService.Result.ALREADY, cheerService.greet(1));
        asking = "newbie";
        assertEquals(CheerService.Result.YOURSELF, cheerService.greet(1));
    }

    @Test
    void onlyLinesAboutArrivingOrGettingOnOfferAGreeting() {
        assertEquals("WAVE", CheerService.cheerFor(CommunityTickerKind.LANDED));
        assertEquals("GG", CheerService.cheerFor(CommunityTickerKind.LEFT_NOOB_ISLAND));
        assertNull(CheerService.cheerFor(CommunityTickerKind.BUILT));
        assertNull(CheerService.cheerFor(CommunityTickerKind.DESTROYED));
    }
}
