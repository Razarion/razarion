package com.btxtech.client.system.boot;

import com.btxtech.client.user.FacebookService;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.ColdGameUiControlConfig;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.rest.GameUiControlController;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.google.gwt.user.client.Window;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 07.02.2016.
 */
@Dependent
public class LoadGameUiControlTask extends AbstractStartupTask {
    private static final String GAME_SESSION_ID_KEY = "gameSessionUuid";
    private static final String SESSION_ID_KEY = "sessionId";
    // private Logger logger = Logger.getLogger(LoadGameUiControlTask.class.getName());
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private Caller<GameUiControlController> serviceCaller;
    @Inject
    private FacebookService facebookService;
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        serviceCaller.call((RemoteCallback<ColdGameUiControlConfig>) coldGameUiControlConfig -> {
            gameUiControl.setColdGameUiControlConfig(coldGameUiControlConfig);
            facebookService.activateFacebookAppStartLogin();
            deferredStartup.finished();
        }, (message, throwable) -> {
            exceptionHandler.restErrorHandler("GameUiControlController.loadGameUiControlConfig()");
            deferredStartup.failed(throwable);
            return false;
        }).loadGameUiControlConfig(setupGameUiControlInput());
    }

    private GameUiControlInput setupGameUiControlInput() {
        GameUiControlInput gameUiControlInput = new GameUiControlInput();
        gameUiControlInput.setPlaybackGameSessionUuid(Window.Location.getParameter(GAME_SESSION_ID_KEY));
        gameUiControlInput.setPlaybackSessionUuid(Window.Location.getParameter(SESSION_ID_KEY));
        return gameUiControlInput;
    }
}
