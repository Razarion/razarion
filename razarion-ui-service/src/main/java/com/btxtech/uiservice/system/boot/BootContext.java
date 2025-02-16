package com.btxtech.uiservice.system.boot;

import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.user.UserUiService;
import elemental2.promise.Promise;

public class BootContext {
    private final SimpleExecutorService simpleExecutorService;
    private final GameEngineControl gameEngineControl;
    private final GameUiControl gameUiControl;
    private final UserUiService userUiService;

    public BootContext(SimpleExecutorService simpleExecutorService, GameEngineControl gameEngineControl, GameUiControl gameUiControl, UserUiService userUiService) {
        this.simpleExecutorService = simpleExecutorService;
        this.gameEngineControl = gameEngineControl;
        this.gameUiControl = gameUiControl;
        this.userUiService = userUiService;
    }

    public SimpleExecutorService getSimpleExecutorService() {
        return simpleExecutorService;
    }

    public GameUiControl getGameUiControl() {
        return gameUiControl;
    }

    public GameEngineControl getGameEngineControl() {
        return gameEngineControl;
    }

    public UserUiService getUserUiService() {
        return userUiService;
    }

    public void loadWorker(DeferredStartup deferredStartup) {

    }

    public void activateFacebookAppStartLogin() {

    }

    public Promise<Void> loadThreeJsModels() {
        return null;
    }

    public void initGameEngineControl(ColdGameUiContext coldGameUiContext, DeferredStartup deferredStartup) {

    }

    public void runRenderer() {

    }
}
