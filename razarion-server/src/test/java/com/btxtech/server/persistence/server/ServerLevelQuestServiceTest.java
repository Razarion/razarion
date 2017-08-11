package com.btxtech.server.persistence.server;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.persistence.GameUiControlConfigPersistence;
import com.btxtech.server.persistence.history.LevelHistoryEntity;
import com.btxtech.server.persistence.history.QuestHistoryEntity;
import com.btxtech.server.user.UserEntity;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.utils.CollectionUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Beat
 * on 10.08.2017.
 */
public class ServerLevelQuestServiceTest extends ArquillianBaseTest {
    @Inject
    private UserService userService;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private ServerLevelQuestService serverLevelQuestService;
    @Inject
    private GameUiControlConfigPersistence gameUiControlConfigPersistence;
    @Inject
    private ServerGameEnginePersistence serverGameEnginePersistence;

    @Before
    public void before() throws Exception {
        setupPlanets();
    }

    @After
    public void after() throws Exception {
        cleanTableNative("USER_COMPLETED_QUEST");
        cleanTable(UserEntity.class);
        cleanPlanets();
        cleanTable(LevelHistoryEntity.class);
        cleanTable(QuestHistoryEntity.class);
    }

    @Test
    public void onLevelUpUnregistered() throws Exception {
        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        UserContext userContext = userService.getUserContext(); // Simulate anonymous login

        serverLevelQuestService.onLevelUpdate(sessionId, LEVEL_2_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContext().getLevelId());
        Assert.assertNull(gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlavePlanetConfig());

        serverLevelQuestService.onLevelUpdate(sessionId, LEVEL_3_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContext().getLevelId());
        Assert.assertNull(gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlavePlanetConfig());

        serverLevelQuestService.onLevelUpdate(sessionId, LEVEL_4_ID);
        Assert.assertEquals(LEVEL_4_ID, userService.getUserContext().getLevelId());
        Assert.assertEquals(SERVER_QUEST_ID_1, gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest().getId());

        assertCount(1, QuestHistoryEntity.class);
        assertCount(3, LevelHistoryEntity.class);
        assertCount(0, UserEntity.class);
    }

    @Test
    public void onLevelUpRegister() throws Exception {
        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        UserContext userContext = userService.handleFacebookUserLogin("0000001");

        assertUser("0000001", LEVEL_1_ID, null);
        Assert.assertTrue(userService.findActiveQuests4Users(serverGameEnginePersistence.readAllQuestIds()).entrySet().isEmpty());

        serverLevelQuestService.onLevelUpdate(sessionId, LEVEL_2_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContext().getLevelId());
        assertUser("0000001", LEVEL_1_ID, null);
        Assert.assertTrue(userService.findActiveQuests4Users(serverGameEnginePersistence.readAllQuestIds()).entrySet().isEmpty());
        Assert.assertNull(gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlavePlanetConfig());

        serverLevelQuestService.onLevelUpdate(sessionId, LEVEL_3_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContext().getLevelId());
        assertUser("0000001", LEVEL_1_ID, null);
        Assert.assertTrue(userService.findActiveQuests4Users(serverGameEnginePersistence.readAllQuestIds()).entrySet().isEmpty());
        Assert.assertNull(gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlavePlanetConfig());

        serverLevelQuestService.onLevelUpdate(sessionId, LEVEL_4_ID);
        Assert.assertEquals(LEVEL_4_ID, userService.getUserContext().getLevelId());
        assertUser("0000001", LEVEL_4_ID, SERVER_QUEST_ID_1);
        Assert.assertEquals(SERVER_QUEST_ID_1, gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest().getId());

        Map<HumanPlayerId, QuestConfig> map = userService.findActiveQuests4Users(serverGameEnginePersistence.readAllQuestIds());
        Assert.assertEquals(1, map.size());
        Map.Entry<HumanPlayerId, QuestConfig> entry = CollectionUtils.getFirst(map.entrySet());
        UserEntity userEntity = userService.getUserForFacebookId("0000001");
        Assert.assertEquals(userEntity.getId(), entry.getKey().getUserId());
        Assert.assertEquals(SERVER_QUEST_ID_1, entry.getValue().getId());

        assertCount(1, QuestHistoryEntity.class);
        assertCount(3, LevelHistoryEntity.class);
        assertCount(1, UserEntity.class);
    }

    @Test
    public void onQuestPassedUnregistered() throws Exception {
        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        UserContext userContext = userService.getUserContext(); // Simulate anonymous login

        serverLevelQuestService.onLevelUpdate(sessionId, LEVEL_4_ID);
        QuestConfig actualQuestConfig1 = gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        Assert.assertEquals(SERVER_QUEST_ID_1, actualQuestConfig1.getId());

        serverLevelQuestService.onQuestPassed(userContext.getHumanPlayerId(), actualQuestConfig1);

        QuestConfig actualQuestConfig2 = gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        Assert.assertEquals(SERVER_QUEST_ID_2, actualQuestConfig2.getId());

        assertCount(3, QuestHistoryEntity.class);
        assertCount(1, LevelHistoryEntity.class);
        assertCount(0, UserEntity.class);
    }

    @Test
    public void onQuestPassedRegistered() throws Exception {
        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        UserContext userContext = userService.handleFacebookUserLogin("0000001");

        serverLevelQuestService.onLevelUpdate(sessionId, LEVEL_4_ID);
        QuestConfig actualQuestConfig1 = gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        Assert.assertEquals(SERVER_QUEST_ID_1, actualQuestConfig1.getId());

        serverLevelQuestService.onQuestPassed(userContext.getHumanPlayerId(), actualQuestConfig1);

        QuestConfig actualQuestConfig2 = gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        Assert.assertEquals(SERVER_QUEST_ID_2, actualQuestConfig2.getId());

        assertCount(3, QuestHistoryEntity.class);
        assertCount(1, LevelHistoryEntity.class);
        assertCount(1, UserEntity.class);
    }

    private void assertUser(String facebookUserId, int levelId, Integer activeQuestId) throws Exception {
        UserEntity userEntity = userService.getUserForFacebookId(facebookUserId);

        runInTransaction(em -> {
            UserEntity actualUserEntity = em.find(UserEntity.class, userEntity.getId());
            Assert.assertEquals(levelId, (int) actualUserEntity.getLevel().getId());
            if (activeQuestId == null) {
                Assert.assertNull(actualUserEntity.getActiveQuest());
            } else {
                Assert.assertNotNull(actualUserEntity.getActiveQuest());
                Assert.assertEquals(activeQuestId, actualUserEntity.getActiveQuest().getId());
            }
        });
    }


}