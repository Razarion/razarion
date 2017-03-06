package com.btxtech.client.system.boot;

import com.btxtech.client.user.Facebook;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
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
    private static final long FACEBOOK_CALL_TIMEOUT = 20000;
    private Logger logger = Logger.getLogger(CallFacebookTask.class.getName());
    @Inject
    private UserUiService userUiService;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    private DeferredStartup deferredStartup;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        this.deferredStartup = deferredStartup;
        deferredStartup.setDeferred();
        deferredStartup.setBackground();
        SimpleScheduledFuture future = simpleExecutorService.schedule(FACEBOOK_CALL_TIMEOUT, this::onLoginTimeout, SimpleExecutorService.Type.UNSPECIFIED);
        try {
            Facebook.getFB().getLoginStatus(response -> {
                future.cancel();
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

    private void onLoginTimeout() {
        logger.warning("Facebook login timed out");
        userUiService.facebookLoginStateFailed();
        deferredStartup.finished();
    }
}
