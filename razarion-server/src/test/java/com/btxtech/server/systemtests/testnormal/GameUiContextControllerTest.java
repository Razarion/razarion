package com.btxtech.server.systemtests.testnormal;

import com.btxtech.server.ServerTestHelper;
import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.rest.GameUiContextController;
import com.btxtech.shared.rest.UserMgmtController;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;

public class GameUiContextControllerTest extends AbstractSystemTest {
    private GameUiContextController gameUiContextController;
    private UserMgmtController userMgmtController;

    @Before
    public void setup() {
        gameUiContextController = setupRestAccess(GameUiContextController.class);
        userMgmtController = setupRestAccess(UserMgmtController.class);
        setupDb();
    }

    @Test
    public void cold() {
        getDefaultRestConnection().logout();
        ColdGameUiContext coldGameUiContext = gameUiContextController.loadColdGameUiContext(new GameUiControlInput());

        assertThat(coldGameUiContext, allOf(
                hasProperty("userContext", allOf(
                        hasProperty("userId", notNullValue()),
                        hasProperty("registerState", equalTo(UserContext.RegisterState.UNREGISTERED)),
                        hasProperty("admin", equalTo(false)),
                        hasProperty("levelId", equalTo(LEVEL_1_ID))
                )),
                staticGameConfigMatcher(),
                hasProperty("warmGameUiContext", warmGameUiContextMatcher(false, PLANET_1_ID, GameEngineMode.MASTER))
        ));
    }

    @Test
    public void coldUser() {
        getDefaultRestConnection().loginUser();
        ColdGameUiContext coldGameUiContext = gameUiContextController.loadColdGameUiContext(new GameUiControlInput());

        assertThat(coldGameUiContext, allOf(
                hasProperty("userContext", allOf(
                        hasProperty("userId", notNullValue()),
                        hasProperty("registerState", equalTo(UserContext.RegisterState.EMAIL_VERIFIED)),
                        hasProperty("admin", equalTo(false)),
                        hasProperty("levelId", equalTo(LEVEL_1_ID))
                )),
                staticGameConfigMatcher(),
                hasProperty("warmGameUiContext", warmGameUiContextMatcher(false, PLANET_1_ID, GameEngineMode.MASTER))
        ));
    }

    @Test
    public void coldAdmin() {
        getDefaultRestConnection().loginAdmin();
        ColdGameUiContext coldGameUiContext = gameUiContextController.loadColdGameUiContext(new GameUiControlInput());

        assertThat(coldGameUiContext, allOf(
                hasProperty("userContext", allOf(
                        hasProperty("userId", notNullValue()),
                        hasProperty("registerState", equalTo(UserContext.RegisterState.EMAIL_VERIFIED)),
                        hasProperty("admin", equalTo(true)),
                        hasProperty("levelId", equalTo(LEVEL_1_ID))
                )),
                staticGameConfigMatcher(),
                hasProperty("warmGameUiContext", warmGameUiContextMatcher(false, PLANET_1_ID, GameEngineMode.MASTER))
        ));
    }

    @Test
    public void coldUserLevel1() {
        getDefaultRestConnection().loginAdmin();
        userMgmtController.setLevel(userMgmtController.getUserIdForEmail(ServerTestHelper.NORMAL_USER_EMAIL), LEVEL_1_ID);

        getDefaultRestConnection().loginUser();
        ColdGameUiContext coldGameUiContext = gameUiContextController.loadColdGameUiContext(new GameUiControlInput());

        assertThat(coldGameUiContext, allOf(
                hasProperty("userContext", allOf(
                        hasProperty("userId", notNullValue()),
                        hasProperty("registerState", equalTo(UserContext.RegisterState.EMAIL_VERIFIED)),
                        hasProperty("admin", equalTo(false)),
                        hasProperty("levelId", equalTo(LEVEL_1_ID))
                )),
                staticGameConfigMatcher(),
                hasProperty("warmGameUiContext", warmGameUiContextMatcher(false, PLANET_1_ID, GameEngineMode.MASTER))
        ));
    }

    @Test
    public void coldUserLevel4() {
        getDefaultRestConnection().loginAdmin();
        userMgmtController.setLevel(userMgmtController.getUserIdForEmail(ServerTestHelper.NORMAL_USER_EMAIL), LEVEL_4_ID);

        getDefaultRestConnection().loginUser();
        ColdGameUiContext coldGameUiContext = gameUiContextController.loadColdGameUiContext(new GameUiControlInput());

        assertThat(coldGameUiContext, allOf(
                hasProperty("userContext", allOf(
                        hasProperty("userId", notNullValue()),
                        hasProperty("registerState", equalTo(UserContext.RegisterState.EMAIL_VERIFIED)),
                        hasProperty("admin", equalTo(false)),
                        hasProperty("levelId", equalTo(LEVEL_4_ID))
                )),
                staticGameConfigMatcher(),
                hasProperty("warmGameUiContext", warmGameUiContextMatcher(true, PLANET_2_ID, GameEngineMode.SLAVE))
        ));
    }

    @Test
    public void warm() {
        WarmGameUiContext warmGameUiContext = gameUiContextController.loadWarmGameUiContext();

        assertThat(warmGameUiContext, warmGameUiContextMatcher(false, PLANET_1_ID, GameEngineMode.MASTER));

    }

    private Matcher<ColdGameUiContext> staticGameConfigMatcher() {
        return hasProperty("staticGameConfig", allOf(
                hasProperty("groundConfigs", notNullValue()),
                hasProperty("levelConfigs", notNullValue())
        ));
    }

    private Matcher<WarmGameUiContext> warmGameUiContextMatcher(boolean availableUnlocks, int planetConfigId, GameEngineMode gameEngineMode) {
        return allOf(
                hasProperty("gameEngineMode", equalTo(gameEngineMode)),
                hasProperty("planetConfig", hasProperty("id", equalTo(planetConfigId))),
                hasProperty("availableUnlocks", equalTo(availableUnlocks))
        );
    }
}
