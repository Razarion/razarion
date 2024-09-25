package com.btxtech.client.system.boot;

import com.btxtech.client.Caller;
import com.btxtech.client.RemoteCallback;
import com.btxtech.client.user.FacebookService;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.rest.GameUiContextController;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.google.gwt.user.client.Window;

import javax.inject.Inject;

/**
 * Created by Beat
 * 07.02.2016.
 */

public class LoadGameUiContextlTask extends AbstractStartupTask {
    private static final String GAME_SESSION_ID_KEY = "gameSessionUuid";
    private static final String SESSION_ID_KEY = "sessionId";

    // private Logger logger = Logger.getLogger(LoadGameUiContextlTask.class.getName());
    private GameUiControl gameUiControl;

    private Caller<GameUiContextController> serviceCaller;

    private FacebookService facebookService;

    private ClientExceptionHandlerImpl exceptionHandler;

    @Inject
    public LoadGameUiContextlTask(ClientExceptionHandlerImpl exceptionHandler, FacebookService facebookService, Caller<com.btxtech.shared.rest.GameUiContextController> serviceCaller, GameUiControl gameUiControl) {
        this.exceptionHandler = exceptionHandler;
        this.facebookService = facebookService;
        this.serviceCaller = serviceCaller;
        this.gameUiControl = gameUiControl;
    }

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        serviceCaller.call((RemoteCallback<ColdGameUiContext>) coldGameUiContext -> {
            try {
                gameUiControl.setColdGameUiContext(coldGameUiContext);
                facebookService.activateFacebookAppStartLogin();
                deferredStartup.finished();
            } catch (Throwable throwable) {
                deferredStartup.failed(throwable);
            }
        }, (message, throwable) -> {
            exceptionHandler.restErrorHandler("GameUiContextController.loadColdGameUiContext()");
            deferredStartup.failed(throwable);
            return false;
        }).loadColdGameUiContext(setupGameUiControlInput());
    }

    private GameUiControlInput setupGameUiControlInput() {
        GameUiControlInput gameUiControlInput = new GameUiControlInput();
        gameUiControlInput.setPlaybackGameSessionUuid(Window.Location.getParameter(GAME_SESSION_ID_KEY));
        gameUiControlInput.setPlaybackSessionUuid(Window.Location.getParameter(SESSION_ID_KEY));
        return gameUiControlInput;
    }
}
