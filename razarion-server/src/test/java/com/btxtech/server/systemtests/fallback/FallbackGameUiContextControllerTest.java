package com.btxtech.server.systemtests.fallback;

import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.rest.GameUiContextController;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class FallbackGameUiContextControllerTest extends AbstractSystemTest {
    private GameUiContextController gameUiContextController;

    @Before
    public void setup() {
        gameUiContextController = setupRestAccess(GameUiContextController.class);
    }

    @Test
    public void fallbackCold() {
        getDefaultRestConnection().logout();
        ColdGameUiContext coldGameUiContext = gameUiContextController.loadColdGameUiContext(new GameUiControlInput());
        assertViaJson("/systemtests/fallback/FallbackGameUiContextControllerTest_fallbackCold.json",
                s -> s.replace("\"$USER_ID$\"", Integer.toString(coldGameUiContext.getUserContext().getUserId())),
                getClass(),
                coldGameUiContext);
    }

    @Test
    public void fallbackColdUser() {
        getDefaultRestConnection().loginUser();
        ColdGameUiContext coldGameUiContext = gameUiContextController.loadColdGameUiContext(new GameUiControlInput());
        assertViaJson("/systemtests/fallback/FallbackGameUiContextControllerTest_fallbackColdUser.json", null, getClass(), coldGameUiContext);
    }

    @Test
    public void fallbackColdAdmin() {
        getDefaultRestConnection().loginAdmin();
        ColdGameUiContext coldGameUiContext = gameUiContextController.loadColdGameUiContext(new GameUiControlInput());
        assertViaJson("/systemtests/fallback/FallbackGameUiContextControllerTest_fallbackColdAdmin.json", null, getClass(), coldGameUiContext);
    }

    @Test
    public void fallbackWarm() {
        assertNull(gameUiContextController.loadWarmGameUiContext());
    }
}
