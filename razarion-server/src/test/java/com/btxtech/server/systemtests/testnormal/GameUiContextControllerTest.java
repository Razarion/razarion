package com.btxtech.server.systemtests.testnormal;

import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.rest.GameUiContextController;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

public class GameUiContextControllerTest extends AbstractSystemTest {
    private GameUiContextController gameUiContextController;

    @Before
    public void setup() {
        gameUiContextController = setupRestAccess(GameUiContextController.class);
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
                shape3DMatcher(),
                particleShapeMatcher(),
                particleEmitterSequenceMatcher(),
                hasProperty("warmGameUiContext", warmGameUiContextMatcher())
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
                shape3DMatcher(),
                particleShapeMatcher(),
                particleEmitterSequenceMatcher(),
                hasProperty("warmGameUiContext", warmGameUiContextMatcher())
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
                shape3DMatcher(),
                particleShapeMatcher(),
                particleEmitterSequenceMatcher(),
                hasProperty("warmGameUiContext", warmGameUiContextMatcher())
        ));
    }

    @Test
    public void warm() {
        WarmGameUiContext warmGameUiContext = gameUiContextController.loadWarmGameUiContext();

        assertThat(warmGameUiContext, warmGameUiContextMatcher());

    }

    private Matcher<ColdGameUiContext> staticGameConfigMatcher() {
        return hasProperty("staticGameConfig", allOf(
                hasProperty("groundConfigs", notNullValue()),
                hasProperty("levelConfigs", notNullValue())
        ));
    }

    private Matcher<ColdGameUiContext> shape3DMatcher() {
        return hasProperty("shape3Ds", hasSize(0));
    }

    private Matcher<ColdGameUiContext> particleShapeMatcher() {
        return hasProperty("particleShapeConfigs", hasSize(3));
    }

    private Matcher<ColdGameUiContext> particleEmitterSequenceMatcher() {
        return hasProperty("particleEmitterSequenceConfigs", hasSize(3));
    }

    private Matcher<WarmGameUiContext> warmGameUiContextMatcher() {
        return allOf(
                hasProperty("gameEngineMode", equalTo(GameEngineMode.MASTER)),
                hasProperty("planetConfig", notNullValue())
        );
    }
}
