package com.btxtech.server.gameengine;

import com.btxtech.server.ClientSystemConnectionServiceTestHelper;
import com.btxtech.server.IgnoreOldArquillianTest;
import com.btxtech.server.SimpleTestEnvironment;
import com.btxtech.server.TestClientSystemConnection;
import com.btxtech.server.TestHelper;
import com.btxtech.server.persistence.GameUiContextCrudPersistence;
import com.btxtech.server.persistence.history.LevelUnlockHistoryEntry;
import com.btxtech.server.persistence.level.LevelUnlockEntity;
import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.user.UnregisteredUser;
import com.btxtech.server.user.UserEntity;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 21.09.2017.
 */
@Ignore
public class RestServerUnlockServiceTestBase extends IgnoreOldArquillianTest {

    private UserService userService;

    private SessionHolder sessionHolder;

    private ServerLevelQuestService serverLevelQuestService;

    private ServerUnlockService serverUnlockService;

    private ClientSystemConnectionServiceTestHelper systemConnectionService;

    private SessionService sessionService;

    private GameUiContextCrudPersistence gameUiContextCrudPersistence;

    @Inject
    public RestServerUnlockServiceTestBase(GameUiContextCrudPersistence gameUiContextCrudPersistence, SessionService sessionService, ClientSystemConnectionServiceTestHelper systemConnectionService, ServerUnlockService serverUnlockService, ServerLevelQuestService serverLevelQuestService, SessionHolder sessionHolder, UserService userService) {
        this.gameUiContextCrudPersistence = gameUiContextCrudPersistence;
        this.sessionService = sessionService;
        this.systemConnectionService = systemConnectionService;
        this.serverUnlockService = serverUnlockService;
        this.serverLevelQuestService = serverLevelQuestService;
        this.sessionHolder = sessionHolder;
        this.userService = userService;
    }

    @Before
    public void before() {
        setupPlanetDb();
    }

    @After
    public void after() {
        cleanTableNative("USER_UNLOCKED");
        cleanTable(UserEntity.class);
        cleanPlanets();
        cleanTable(LevelUnlockHistoryEntry.class);
    }

    @Test
    public void newRegisteredUser() throws Exception {
        int userId = handleFacebookUserLogin("0000001").getUserId();
        runTest(userId, crystals -> userService.persistAddCrystals(userId, crystals), this::assertRegisteredState);
    }

    @Test
    public void newUnregisteredUser() throws Exception {
        int userId = userService.getUserContextFromSession().getUserId();
        runTest(userId, crystals -> sessionService.findPlayerSession(userId).getUnregisteredUser().addCrystals(crystals), this::assertUnregisteredState);
    }

    private void runTest(int userId, Consumer<Integer> crystalSetter, AssertHelper assertHelper) throws Exception {
        Map<Integer, Integer> expectedItemLimit1 = new HashMap<>();
        expectedItemLimit1.put(BASE_ITEM_TYPE_BULLDOZER_ID, 1);
        HashMap<Integer, Integer> expectedItemLimit2 = new HashMap<>();
        expectedItemLimit2.put(BASE_ITEM_TYPE_BULLDOZER_ID, 1);
        expectedItemLimit2.put(BASE_ITEM_TYPE_ATTACKER_ID, 2);
        HashMap<Integer, Integer> expectedItemLimit3 = new HashMap<>();
        expectedItemLimit3.put(BASE_ITEM_TYPE_BULLDOZER_ID, 1);
        expectedItemLimit3.put(BASE_ITEM_TYPE_ATTACKER_ID, 2);
        expectedItemLimit3.put(BASE_ITEM_TYPE_HARVESTER_ID, 1);

        BaseItemService baseItemServiceMock = EasyMock.createStrictMock(BaseItemService.class);
        baseItemServiceMock.updateUnlockedItemLimit(userId, expectedItemLimit1);
        baseItemServiceMock.updateUnlockedItemLimit(userId, expectedItemLimit2);
        baseItemServiceMock.updateUnlockedItemLimit(userId, expectedItemLimit3);
        EasyMock.replay(baseItemServiceMock);

        SimpleTestEnvironment.injectService("baseItemService", serverUnlockService, baseItemServiceMock);
        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_5_ID);
        assertAvailableUnlocks(LEVEL_UNLOCK_ID_L4_1, LEVEL_UNLOCK_ID_L5_1, LEVEL_UNLOCK_ID_L5_2);
        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_4_ID);
        assertHelper.assertState(userId, 0);
        assertAvailableUnlocks(LEVEL_UNLOCK_ID_L4_1);
        TestClientSystemConnection systemConnection = systemConnectionService.connectClient(sessionHolder.getPlayerSession());
        // Unlock
        serverUnlockService.unlockViaCrystals(userId, LEVEL_UNLOCK_ID_L4_1);
        // Verify
        UserContext userContext = userService.getUserContextFromSession();
        Assert.assertEquals(expectedItemLimit1, userContext.getUnlockedItemLimit());
        assertHelper.assertState(userId, 0, LEVEL_UNLOCK_ID_L4_1);
        assertAvailableUnlocks();
        // Try unlock same again
        try {
            serverUnlockService.unlockViaCrystals(userId, LEVEL_UNLOCK_ID_L4_1);
            Assert.fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        // Try unlock wrong level again
        try {
            serverUnlockService.unlockViaCrystals(userId, LEVEL_UNLOCK_ID_L5_1);
            Assert.fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        // Verify
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(expectedItemLimit1, userContext.getUnlockedItemLimit());
        assertHelper.assertState(userId, 0, LEVEL_UNLOCK_ID_L4_1);
        // Level up and unlock lvl 5
        crystalSetter.accept(15);
        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_5_ID);
        assertAvailableUnlocks(LEVEL_UNLOCK_ID_L5_1, LEVEL_UNLOCK_ID_L5_2);
        serverUnlockService.unlockViaCrystals(userId, LEVEL_UNLOCK_ID_L5_1);
        // Verify
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(expectedItemLimit2, userContext.getUnlockedItemLimit());
        assertHelper.assertState(userId, 5, LEVEL_UNLOCK_ID_L4_1, LEVEL_UNLOCK_ID_L5_1);
        assertAvailableUnlocks(LEVEL_UNLOCK_ID_L5_2);
        // Unlock but not enough crystals
        try {
            serverUnlockService.unlockViaCrystals(userId, LEVEL_UNLOCK_ID_L5_2);
            Assert.fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        // Verify
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(expectedItemLimit2, userContext.getUnlockedItemLimit());
        assertHelper.assertState(userId, 5, LEVEL_UNLOCK_ID_L4_1, LEVEL_UNLOCK_ID_L5_1);
        assertAvailableUnlocks(LEVEL_UNLOCK_ID_L5_2);
        // Unlock
        crystalSetter.accept(17);
        serverUnlockService.unlockViaCrystals(userId, LEVEL_UNLOCK_ID_L5_2);
        // Verify
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(expectedItemLimit3, userContext.getUnlockedItemLimit());
        assertHelper.assertState(userId, 2, LEVEL_UNLOCK_ID_L4_1, LEVEL_UNLOCK_ID_L5_1, LEVEL_UNLOCK_ID_L5_2);
        assertAvailableUnlocks();

        systemConnection.getWebsocketMessageHelper().assertMessageSentCount(3);
        systemConnection.getWebsocketMessageHelper().assertMessageSent(0, "UNLOCKED_ITEM_LIMIT", expectedItemLimit1);
        systemConnection.getWebsocketMessageHelper().assertMessageSent(1, "UNLOCKED_ITEM_LIMIT", expectedItemLimit2);
        systemConnection.getWebsocketMessageHelper().assertMessageSent(2, "UNLOCKED_ITEM_LIMIT", expectedItemLimit3);

        EasyMock.verify(baseItemServiceMock);
        assertCount(3, LevelUnlockHistoryEntry.class);
    }

    private void assertAvailableUnlocks(Integer... expectedLevelUnlockIds) throws Exception {
        Assert.fail("...TODO...");

//        List<LevelUnlockConfig> actualLevelUnlockConfigs = gameUiContextCrudPersistence.loadCold(new GameUiControlInput(), Locale.ENGLISH, userService.getUserContextFromSession()).getLevelUnlockConfigs();
//        Assert.assertEquals(expectedLevelUnlockIds.length, actualLevelUnlockConfigs.size());
//        Collection<Integer> expectedCollection = new ArrayList<>(Arrays.asList(expectedLevelUnlockIds));
//        expectedCollection.removeAll(actualLevelUnlockConfigs.stream().map(LevelUnlockConfig::getId).collect(Collectors.toList()));
//        Assert.assertTrue(expectedCollection.isEmpty());
    }

    private void assertRegisteredState(int userId, int expectedCrystals, Integer... expectedLevelUnlockEntityIds) {
        runInTransaction(entityManager -> {
            UserEntity userEntity = entityManager.find(UserEntity.class, userId);
            Assert.assertEquals("Crystals", expectedCrystals, userEntity.getCrystals());
            Assert.assertEquals("LevelUnlockEntities size", expectedLevelUnlockEntityIds.length, userEntity.getLevelUnlockEntities().size());
            List<Integer> expectedLevelUnlockEntityIdList = new ArrayList<>(Arrays.asList(expectedLevelUnlockEntityIds));
            for (LevelUnlockEntity levelUnlockEntity : userEntity.getLevelUnlockEntities()) {
                Assert.assertTrue("Unexpected LevelUnlockEntity id: " + levelUnlockEntity.getId(), expectedLevelUnlockEntityIdList.remove(levelUnlockEntity.getId()));
            }
            Assert.assertTrue("Unused LevelUnlockEntity", expectedLevelUnlockEntityIdList.isEmpty());
        });
    }

    private void assertUnregisteredState(int userId, int expectedCrystals, Integer... expectedLevelUnlockEntityIds) {
        PlayerSession playerSession = sessionService.findPlayerSession(userId);
        Assert.assertNotNull(playerSession);
        UnregisteredUser unregisteredUser = playerSession.getUnregisteredUser();
        Assert.assertNotNull(unregisteredUser);
        Assert.assertEquals("Crystals", expectedCrystals, unregisteredUser.getCrystals());
        Collection<Integer> actualLevelUnlockEntityIds = unregisteredUser.getLevelUnlockEntityIds();
        if (actualLevelUnlockEntityIds == null) {
            actualLevelUnlockEntityIds = Collections.emptyList();
        }
        TestHelper.assertIds(actualLevelUnlockEntityIds, expectedLevelUnlockEntityIds);
    }

    private interface AssertHelper {
        void assertState(int userId, int expectedCrystals, Integer... expectedLevelUnlockEntityIds);
    }
}