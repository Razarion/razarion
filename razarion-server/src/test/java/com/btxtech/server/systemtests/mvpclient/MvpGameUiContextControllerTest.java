package com.btxtech.server.systemtests.mvpclient;

import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.rest.GameUiContextController;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.wildfly.common.Assert.assertTrue;

public class MvpGameUiContextControllerTest extends AbstractSystemTest {
    private GameUiContextController gameUiContextController;

    @Before
    public void setup() {
        gameUiContextController = setupRestAccess(GameUiContextController.class);
    }

    @Test
    public void fallbackCold() {
        getDefaultRestConnection().logout();
        ColdGameUiContext coldGameUiContext = gameUiContextController.loadColdGameUiContext(new GameUiControlInput());
        assertViaJson(FallbackConfig.coldGameUiControlConfig(coldGameUiContext.getUserContext()),
                coldGameUiContext);
        assertFalse(coldGameUiContext.getUserContext().isRegistered());
        assertFalse(coldGameUiContext.getUserContext().isAdmin());
    }

    @Test
    public void fallbackColdUser() {
        getDefaultRestConnection().loginUser();
        ColdGameUiContext coldGameUiContext = gameUiContextController.loadColdGameUiContext(new GameUiControlInput());
        assertViaJson(FallbackConfig.coldGameUiControlConfig(coldGameUiContext.getUserContext()),
                coldGameUiContext);
        assertTrue(coldGameUiContext.getUserContext().isRegistered());
        assertFalse(coldGameUiContext.getUserContext().isAdmin());
    }

    @Test
    public void fallbackColdAdmin() {
        getDefaultRestConnection().loginAdmin();
        ColdGameUiContext coldGameUiContext = gameUiContextController.loadColdGameUiContext(new GameUiControlInput());
        assertViaJson(FallbackConfig.coldGameUiControlConfig(coldGameUiContext.getUserContext()),
                coldGameUiContext);
        assertTrue(coldGameUiContext.getUserContext().isRegistered());
        assertTrue(coldGameUiContext.getUserContext().isAdmin());
    }

    @Test
    public void fallbackWarm() {
        WarmGameUiContext warmGameUiContext = gameUiContextController.loadWarmGameUiContext();
        assertViaJson(FallbackConfig.warmGameUiControlConfig(),
                warmGameUiContext);
    }
}
