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
import com.btxtech.uiservice.system.boot.BootContext;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.google.gwt.user.client.Window;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 07.02.2016.
 */

public class LoadGameUiContextlTask extends AbstractStartupTask {
    private static final String GAME_SESSION_ID_KEY = "gameSessionUuid";
    private static final String SESSION_ID_KEY = "sessionId";
    private static final Logger logger = Logger.getLogger(LoadGameUiContextlTask.class.getName());
    private final BootContext bootContext;

    private Caller<GameUiContextController> serviceCaller;

    @Inject
    public LoadGameUiContextlTask(BootContext bootContext) {
        this.bootContext = bootContext;
    }

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        serviceCaller.call((RemoteCallback<ColdGameUiContext>) coldGameUiContext -> {
            try {
                bootContext.getGameUiControl().setColdGameUiContext(coldGameUiContext);
                bootContext.activateFacebookAppStartLogin();
                deferredStartup.finished();
            } catch (Throwable throwable) {
                deferredStartup.failed(throwable);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "LoadGameUiContextlTask failed: " + message, throwable);
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
