package com.btxtech.client.system.boot;

import com.btxtech.client.user.Facebook;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.uiservice.user.UserUiService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 25.01.2017.
 */
@Dependent
public class CallFacebookTask extends AbstractStartupTask {
    private Logger logger = Logger.getLogger(CallFacebookTask.class.getName());
    @Inject
    private UserUiService userUiService;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        deferredStartup.setBackground();
        try {
            Facebook.getFB().getLoginStatus(response -> {
                if (response.authResponse != null) {
                    userUiService.facebookLoginState(response.status, response.authResponse.accessToken, response.authResponse.expiresIn, response.authResponse.signedRequest, response.authResponse.userID);
                } else {
                    userUiService.facebookLoginState(response.status);
                }
                deferredStartup.finished();
                return null;
            });
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "Call Facebook SDK failed", throwable);
            userUiService.facebookLoginStateFailed();
            deferredStartup.finished();
        }
    }
}
