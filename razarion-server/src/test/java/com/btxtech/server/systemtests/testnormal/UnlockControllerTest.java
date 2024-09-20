package com.btxtech.server.systemtests.testnormal;

import com.btxtech.server.ServerTestHelper;
import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.btxtech.shared.gameengine.datatypes.packets.UnlockResultInfo;
import com.btxtech.shared.rest.UnlockController;
import com.btxtech.shared.rest.UserMgmtController;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

public class UnlockControllerTest extends AbstractSystemTest {
    private UnlockController unlockController;
    private UserMgmtController userMgmtController;

    @Before
    public void setup() {
        unlockController = setupRestAccess(UnlockController.class);
        userMgmtController = setupRestAccess(UserMgmtController.class);
        setupDb();
    }

    @Test
    public void unlock1() {
        getDefaultRestConnection().loginAdmin();
        userMgmtController.setLevel(userMgmtController.getUserIdForEmail(ServerTestHelper.NORMAL_USER_EMAIL), LEVEL_4_ID);

        getDefaultRestConnection().loginUser();

        List<LevelUnlockConfig> levelUnlockConfigs = unlockController.getAvailableLevelUnlockConfigs();
        assertThat(levelUnlockConfigs, hasSize(1));
        assertThat(levelUnlockConfigs, containsInAnyOrder(
                allOf(
                        hasProperty("id", equalTo(LEVEL_UNLOCK_ID_L4_1)),
                        hasProperty("internalName", equalTo("levelUnlockEntity4_1")),
                        hasProperty("baseItemType", equalTo(BASE_ITEM_TYPE_BULLDOZER_ID)),
                        hasProperty("baseItemTypeCount", equalTo(1)),
                        hasProperty("crystalCost", equalTo(0))
                )
        ));

        UnlockResultInfo unlockResultInfo = unlockController.unlockViaCrystals(LEVEL_UNLOCK_ID_L4_1);
        assertThat(unlockResultInfo,
                allOf(
                        hasProperty("notEnoughCrystals", equalTo(false)),
                        hasProperty("availableUnlocks", hasSize(0))
                ));


        assertThat(unlockController.getAvailableLevelUnlockConfigs(), hasSize(0));
    }

    @Test
    public void unlock2() {
        getDefaultRestConnection().loginAdmin();
        userMgmtController.setLevel(userMgmtController.getUserIdForEmail(ServerTestHelper.NORMAL_USER_EMAIL), LEVEL_5_ID);

        getDefaultRestConnection().loginUser();

        List<LevelUnlockConfig> levelUnlockConfigs = unlockController.getAvailableLevelUnlockConfigs();
        assertThat(levelUnlockConfigs, hasSize(3));
        assertThat(levelUnlockConfigs, containsInAnyOrder(
                allOf(
                        hasProperty("id", equalTo(LEVEL_UNLOCK_ID_L5_1)),
                        hasProperty("internalName", equalTo("levelUnlockEntity5_1")),
                        hasProperty("baseItemType", equalTo(BASE_ITEM_TYPE_ATTACKER_ID)),
                        hasProperty("baseItemTypeCount", equalTo(2)),
                        hasProperty("crystalCost", equalTo(10))
                ),
                allOf(
                        hasProperty("id", equalTo(LEVEL_UNLOCK_ID_L5_2)),
                        hasProperty("internalName", equalTo("levelUnlockEntity5_2")),
                        hasProperty("baseItemType", equalTo(BASE_ITEM_TYPE_HARVESTER_ID)),
                        hasProperty("baseItemTypeCount", equalTo(1)),
                        hasProperty("crystalCost", equalTo(20))
                ),
                allOf(
                        hasProperty("id", equalTo(LEVEL_UNLOCK_ID_L4_1)),
                        hasProperty("internalName", equalTo("levelUnlockEntity4_1")),
                        hasProperty("baseItemType", equalTo(BASE_ITEM_TYPE_BULLDOZER_ID)),
                        hasProperty("baseItemTypeCount", equalTo(1)),
                        hasProperty("crystalCost", equalTo(0))
                )
        ));

        UnlockResultInfo unlockResultInfo = unlockController.unlockViaCrystals(LEVEL_UNLOCK_ID_L5_1);
        assertThat(unlockResultInfo,
                allOf(
                        hasProperty("notEnoughCrystals", equalTo(true)),
                        hasProperty("availableUnlocks", nullValue())
                ));


        assertThat(unlockController.getAvailableLevelUnlockConfigs().size(), equalTo(3));

        getDefaultRestConnection().loginAdmin();
        userMgmtController.setCrystals(userMgmtController.getUserIdForEmail(ServerTestHelper.NORMAL_USER_EMAIL), 10);
        getDefaultRestConnection().loginUser();

        unlockResultInfo = unlockController.unlockViaCrystals(LEVEL_UNLOCK_ID_L5_1);
        assertThat(unlockResultInfo,
                allOf(
                        hasProperty("notEnoughCrystals", equalTo(false)),
                        hasProperty("availableUnlocks", hasSize(2))
                ));

    }


}
