package com.btxtech.server.systemtests.testnormal;

import com.btxtech.server.ServerTestHelper;
import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.rest.QuestController;
import com.btxtech.shared.rest.TerrainShapeController;
import com.btxtech.shared.rest.UserMgmtController;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

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
    public void test() {
        getDefaultRestConnection().loginAdmin();
        userMgmtController.setLevel(userMgmtController.getUserIdForEmail(ServerTestHelper.NORMAL_USER_EMAIL), LEVEL_4_ID);

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
        userMgmtController.setLevel(userMgmtController.getUserIdForEmail(ServerTestHelper.NORMAL_USER_EMAIL), LEVEL_5_ID);

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

}
