package com.btxtech.client.user;

import com.btxtech.shared.datatypes.RegisterInfo;
import com.btxtech.shared.rest.UserServiceProvider;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.user.UserUiService;
import com.google.gwt.user.client.Window;
import com.btxtech.shared.deprecated.Caller;
import com.btxtech.shared.deprecated.RemoteCallback;

import javax.inject.Singleton;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 15.03.2018.
 */
@Singleton
public class FacebookService {
    private Logger logger = Logger.getLogger(FacebookService.class.getName());

    private Caller<UserServiceProvider> caller;

    private ExceptionHandler exceptionHandler;

    private UserUiService userUiService;
    private Facebook.LoginStatusCallback statusCallback;

    @Inject
    public FacebookService(UserUiService userUiService, ExceptionHandler exceptionHandler, Caller<com.btxtech.shared.rest.UserServiceProvider> caller) {
        this.userUiService = userUiService;
        this.exceptionHandler = exceptionHandler;
        this.caller = caller;
    }

    public void activateFacebookAppStartLogin() {
        try {
            if(userUiService.isRegistered()) {
                return;
            }
            if (Facebook.getAppStartLogin().getFbAuthResponse() != null) {
                if (Facebook.CONNECTED.equalsIgnoreCase(Facebook.getAppStartLogin().getFbAuthResponse().status)) {
                    callInGameFacebookRegister(Facebook.getAppStartLogin().getFbAuthResponse());
                }
                Facebook.getAppStartLogin().setFbAuthResponse(null);
            } else {
                Facebook.getAppStartLogin().setFbAuthResponseCallback(response -> {
                    if (Facebook.CONNECTED.equalsIgnoreCase(response.status)) {
                        callInGameFacebookRegister(response);
                    }
                });
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void onRegisterButtonShown(Runnable onRegisteredCallback) {
        try {
            if (statusCallback == null) {
                statusCallback = response -> {
                    if (Facebook.CONNECTED.equalsIgnoreCase(response.status)) {
                        callInGameFacebookRegister(response);
                        onRegisteredCallback.run();
                    }
                };
                Facebook.getFB().getEvent().subscribe("auth.statusChange", statusCallback);
            }
            // Renders the facebook register button
            Facebook.getFB().getXFBML().parse();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void onRegisterButtonHidden() {
        if (statusCallback != null) {
            Facebook.getFB().getEvent().unsubscribe("auth.statusChange", statusCallback);
            statusCallback = null;
        }
    }

    private void callInGameFacebookRegister(Facebook.FbResponse response) {
        caller.call((RemoteCallback<RegisterInfo>) registerInfo -> {
                    if (registerInfo.isUserAlreadyExits()) {
                        // User has been logged in on the server
                        Window.Location.reload();
                    } else {
                        userUiService.onUserRegistered(false);
                    }
                },
                (message, throwable) -> {
                    logger.log(Level.SEVERE, "UserServiceProvider.inGameFacebookRegister() failed: " + message, throwable);
                    return false;
                }).inGameFacebookRegister(Facebook.toFbAuthResponse(response.authResponse));
    }
}

