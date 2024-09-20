package com.btxtech.server.systemtests.testempty;

import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.rest.GameUiContextController;
import com.btxtech.test.JsonAssert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class GameUiContextControllerTest extends AbstractSystemTest {
    private GameUiContextController gameUiContextController;

    @Before
    public void setup() {
        gameUiContextController = setupRestAccess(GameUiContextController.class);
    }

    @Test
    public void cold() {
        getDefaultRestConnection().logout();
        ColdGameUiContext coldGameUiContext = gameUiContextController.loadColdGameUiContext(new GameUiControlInput());
        JsonAssert.assertViaJson("/systemtests/testempty/GameUiContextControllerTest_fallbackCold.json",
                s -> s.replace("\"$USER_ID$\"", Integer.toString(coldGameUiContext.getUserContext().getUserId())),
                null, getClass(),
                coldGameUiContext);
    }

    @Test
    public void coldUser() {
        getDefaultRestConnection().loginUser();
        ColdGameUiContext coldGameUiContext = gameUiContextController.loadColdGameUiContext(new GameUiControlInput());
        JsonAssert.assertViaJson("/systemtests/testempty/GameUiContextControllerTest_fallbackColdUser.json",
                s -> s.replace("\"$USER_ID$\"", Integer.toString(NORMAL_USER_ID)),
                null, getClass(),
                coldGameUiContext);
    }

    @Test
    public void coldAdmin() {
        getDefaultRestConnection().loginAdmin();
        ColdGameUiContext coldGameUiContext = gameUiContextController.loadColdGameUiContext(new GameUiControlInput());
        JsonAssert.assertViaJson("/systemtests/testempty/GameUiContextControllerTest_fallbackColdAdmin.json",
                s -> s.replace("\"$USER_ID$\"", Integer.toString(ADMIN_USER_ID)),
                null, getClass(),
                coldGameUiContext);
    }

    @Test
    public void warm() {
        assertNull(gameUiContextController.loadWarmGameUiContext());
    }
}
