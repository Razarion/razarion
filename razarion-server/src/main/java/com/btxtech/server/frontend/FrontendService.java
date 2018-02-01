package com.btxtech.server.frontend;

import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.datatypes.FbAuthResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Locale;

/**
 * Created by Beat
 * on 29.01.2018.
 */
@Singleton
public class FrontendService {
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private UserService userService;

    public FrontendLoginState isLoggedIn() {
        FrontendLoginState frontendLoginState = new FrontendLoginState();
        if (sessionHolder.isLoggedIn()) {
            frontendLoginState.setLoggedIn(true);
        }
        frontendLoginState.setLanguage(sessionHolder.getPlayerSession().getLocale().toString());
        return frontendLoginState;
    }

    public FrontendLoginState createErrorFrontendLoginState() {
        return new FrontendLoginState().setLanguage(Locale.getDefault().toString());
    }

    public void facebookAuthenticated(FbAuthResponse fbAuthResponse) {
        if (fbAuthResponse.getUserID() == null) {
            throw new IllegalArgumentException("UserService: fbAuthResponse.getUserID() == null for sessionId: " + sessionHolder.getPlayerSession().getHttpSessionId());
        }
        userService.handleFacebookUserLogin(fbAuthResponse);
    }

}
