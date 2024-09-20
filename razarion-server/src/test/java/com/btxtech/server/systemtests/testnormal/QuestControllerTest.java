package com.btxtech.server.systemtests.testnormal;

import com.btxtech.server.ServerTestHelper;
import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.rest.QuestController;
import com.btxtech.shared.rest.UserMgmtController;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

public class QuestControllerTest extends AbstractSystemTest {
    private QuestController questController;
    private UserMgmtController userMgmtController;

    @Before
    public void setup() {
        questController = setupRestAccess(QuestController.class);
        userMgmtController = setupRestAccess(UserMgmtController.class);
        setupDb();
    }

    @Test
    public void activate() {
        getDefaultRestConnection().loginAdmin();
        int userId = userMgmtController.getUserIdForEmail(ServerTestHelper.NORMAL_USER_EMAIL);
        userMgmtController.setLevel(userId, LEVEL_4_ID);

        getDefaultRestConnection().loginUser();
        List<QuestConfig>  questConfigs = questController.readMyOpenQuests();
        assertThat(questConfigs, contains(
                allOf(
                        hasProperty("id", equalTo(SERVER_QUEST_ID_L4_1))
                ),
                allOf(
                        hasProperty("id", equalTo(SERVER_QUEST_ID_L4_2))
                )
        ));

        getDefaultRestConnection().loginAdmin();
        userMgmtController.setLevel(userId, LEVEL_5_ID);

        getDefaultRestConnection().loginUser();
        questConfigs = questController.readMyOpenQuests();
        assertThat(questConfigs, contains(
                allOf(
                        hasProperty("id", equalTo(SERVER_QUEST_ID_L4_1))
                ),
                allOf(
                        hasProperty("id", equalTo(SERVER_QUEST_ID_L4_2))
                ),
                allOf(
                        hasProperty("id", equalTo(SERVER_QUEST_ID_L5_1))
                ),
                allOf(
                        hasProperty("id", equalTo(SERVER_QUEST_ID_L5_2))
                ),
                allOf(
                        hasProperty("id", equalTo(SERVER_QUEST_ID_L5_3))
                )
        ));

        questController.activateNextPossibleQuest();
        questConfigs = questController.readMyOpenQuests();
        assertThat(questConfigs, contains(
                allOf(
                        hasProperty("id", equalTo(SERVER_QUEST_ID_L4_2))
                ),
                allOf(
                        hasProperty("id", equalTo(SERVER_QUEST_ID_L5_1))
                ),
                allOf(
                        hasProperty("id", equalTo(SERVER_QUEST_ID_L5_2))
                ),
                allOf(
                        hasProperty("id", equalTo(SERVER_QUEST_ID_L5_3))
                )
        ));

        questController.activateQuest(SERVER_QUEST_ID_L4_2);
        questConfigs = questController.readMyOpenQuests();
        assertThat(questConfigs, contains(
                allOf(
                        hasProperty("id", equalTo(SERVER_QUEST_ID_L4_1))
                ),
                allOf(
                        hasProperty("id", equalTo(SERVER_QUEST_ID_L5_1))
                ),
                allOf(
                        hasProperty("id", equalTo(SERVER_QUEST_ID_L5_2))
                ),
                allOf(
                        hasProperty("id", equalTo(SERVER_QUEST_ID_L5_3))
                )
        ));

    }

    @Test
    public void completedQuests() {
        getDefaultRestConnection().loginAdmin();
        int userId = userMgmtController.getUserIdForEmail(ServerTestHelper.NORMAL_USER_EMAIL);
        userMgmtController.setLevel(userId, LEVEL_5_ID);
        userMgmtController.setCompletedQuests(userId, Arrays.asList(SERVER_QUEST_ID_L4_1, SERVER_QUEST_ID_L4_2, SERVER_QUEST_ID_L5_2));

        getDefaultRestConnection().loginUser();
        List<QuestConfig>  questConfigs = questController.readMyOpenQuests();
        assertThat(questConfigs, contains(
                allOf(
                        hasProperty("id", equalTo(SERVER_QUEST_ID_L5_1))
                ),
                allOf(
                        hasProperty("id", equalTo(SERVER_QUEST_ID_L5_3))
                )
        ));

    }
}
