package com.btxtech.server.user;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.persistence.history.LevelHistoryEntity;
import com.btxtech.server.persistence.quest.ComparisonConfigEntity;
import com.btxtech.server.persistence.quest.ConditionConfigEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.server.ServerChildListCrudePersistence;
import com.btxtech.server.persistence.server.ServerGameEngineConfigEntity;
import com.btxtech.server.persistence.server.ServerGameEnginePersistence;
import com.btxtech.server.persistence.server.ServerLevelQuestEntity;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Locale;

/**
 * Created by Beat
 * 05.05.2017.
 */
public class UserServiceLevelQuestTest extends ArquillianBaseTest {
    @Inject
    private UserService userService;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private ServerGameEnginePersistence serverGameEnginePersistence;

    @Before
    public void before() throws Exception {
        setupPlanets();
    }

    @After
    public void after() throws Exception {
        cleanTable(LevelHistoryEntity.class);
        cleanTable(UserEntity.class);
        cleanPlanets();
    }

    @Test
    public void onLevelUpUnregistered() throws Exception {
        setupQuests();
        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        UnregisteredUser unregisteredUser = sessionHolder.getPlayerSession().getUnregisteredUser();
        userService.getUserContext(); // Simulate anonymous login

        userService.onLevelUpdate(sessionId, LEVEL_2_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContext().getLevelId());

        userService.onLevelUpdate(sessionId, LEVEL_3_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContext().getLevelId());

        userService.onLevelUpdate(sessionId, LEVEL_4_ID);
        Assert.assertEquals(LEVEL_4_ID, userService.getUserContext().getLevelId());

        assertCount(3, LevelHistoryEntity.class);
        assertCount(0, UserEntity.class);
        cleanQuests();
    }

    @Test
    public void onLevelUpRegister() throws Exception {
        QuestConfig questConfig11 = setupQuests();
        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        userService.handleFacebookUserLogin("0000001");

        assertUser("0000001", LEVEL_1_ID, null);

        userService.onLevelUpdate(sessionId, LEVEL_2_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContext().getLevelId());
        assertUser("0000001", LEVEL_1_ID, null);

        userService.onLevelUpdate(sessionId, LEVEL_3_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContext().getLevelId());
        assertUser("0000001", LEVEL_1_ID, null);

        userService.onLevelUpdate(sessionId, LEVEL_4_ID);
        Assert.assertEquals(LEVEL_4_ID, userService.getUserContext().getLevelId());
        assertUser("0000001", LEVEL_4_ID, questConfig11.getId());

        assertCount(3, LevelHistoryEntity.class);
        assertCount(1, UserEntity.class);
        cleanQuests();
    }

    private QuestConfig setupQuests() {
        ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, ServerLevelQuestEntity, ServerLevelQuestConfig> crud = serverGameEnginePersistence.getServerLevelQuestCrud();
        ServerLevelQuestConfig serverLevelQuestConfig1 = crud.create();
        serverLevelQuestConfig1.setMinimalLevelId(LEVEL_4_ID);
        serverLevelQuestConfig1.setInternalName("xxxx 1");
        crud.update(serverLevelQuestConfig1);
        ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerLevelQuestEntity, QuestConfigEntity, QuestConfig> questCrud = serverGameEnginePersistence.getServerQuestCrud(serverLevelQuestConfig1.getId(), Locale.ENGLISH);
        QuestConfig questConfig11 = questCrud.create();
        questConfig11.setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setCount(1)));
        questCrud.update(questConfig11);
        questCrud.create();
        questCrud.create();

        ServerLevelQuestConfig serverLevelQuestConfig2 = crud.create();
        serverLevelQuestConfig2.setMinimalLevelId(LEVEL_5_ID);
        serverLevelQuestConfig2.setInternalName("xxxx 2");
        crud.update(serverLevelQuestConfig2);
        questCrud = serverGameEnginePersistence.getServerQuestCrud(serverLevelQuestConfig2.getId(), Locale.ENGLISH);
        questCrud.create();
        questCrud.create();
        return questConfig11;
    }

    private void cleanQuests() {
        ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, ServerLevelQuestEntity, ServerLevelQuestConfig> crud = serverGameEnginePersistence.getServerLevelQuestCrud();
        for (ObjectNameId objectNameId : crud.readObjectNameIds()) {
            crud.delete(objectNameId.getId());
        }
        assertEmptyCount(ServerLevelQuestEntity.class);
        assertEmptyCountNative("SERVER_QUEST");
        assertEmptyCount(QuestConfigEntity.class);
        assertEmptyCount(ConditionConfigEntity.class);
        assertEmptyCount(ComparisonConfigEntity.class);
        assertEmptyCountNative("QUEST_COMPARISON_BASE_ITEM");
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