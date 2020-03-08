package com.btxtech.server.systemtests;

import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.rest.GameUiContextController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GameUiContextControllerTest extends AbstractSystemTest {
    private GameUiContextController gameUiContextController;

    @Before
    public void setup() {
        gameUiContextController = setupRestAccess(GameUiContextController.class);
    }

    @Test
    public void fallbackCold() {
        ColdGameUiContext coldGameUiContext = gameUiContextController.loadColdGameUiContext(new GameUiControlInput());
        Assert.fail("*** Verify ***");
    }

    @Test
    public void fallbackWarm() {
        WarmGameUiContext warmGameUiContext = gameUiContextController.loadWarmGameUiContext();
        Assert.fail("*** Verify ***");
    }
}
