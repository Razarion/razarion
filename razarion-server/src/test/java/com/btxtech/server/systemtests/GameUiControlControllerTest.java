package com.btxtech.server.systemtests;

import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.rest.GameUiControlController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GameUiControlControllerTest extends RestServerTestBase {
    private GameUiControlController gameUiControlController;

    @Before
    public void setup() {
        gameUiControlController = setupRestAccess(GameUiControlController.class);
    }

    @Test
    public void loadGameUiControlConfig() {
        gameUiControlController.loadGameUiControlConfig(new GameUiControlInput());
        Assert.fail("*** Verify ***");
    }

}
