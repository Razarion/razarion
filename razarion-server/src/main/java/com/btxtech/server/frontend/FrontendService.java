package com.btxtech.server.frontend;

import com.btxtech.server.user.RegisterService;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.datatypes.FbAuthResponse;
import com.btxtech.shared.system.ExceptionHandler;

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
    @Inject
    private RegisterService registerService;
    @Inject
    private ExceptionHandler exceptionHandler;

    public InternalLoginState isLoggedIn(String loginCookieValue) {
        InternalLoginState internalLoginState = new InternalLoginState();
        internalLoginState.setFrontendLoginState(new FrontendLoginState());
        if (sessionHolder.isLoggedIn()) {
            internalLoginState.getFrontendLoginState().setLoggedIn(true);
        } else {
            if (loginCookieValue != null && !loginCookieValue.trim().isEmpty()) {
                try {
                    String newLoginCookieValue = registerService.cookieLogin(loginCookieValue);
                    if (newLoginCookieValue != null) {
                        internalLoginState.getFrontendLoginState().setLoggedIn(true);
                        internalLoginState.setLoginCookieValue(newLoginCookieValue);
                    }
                } catch (Throwable t) {
                    exceptionHandler.handleException(t);
                }
            }
        }
        internalLoginState.getFrontendLoginState().setLanguage(sessionHolder.getPlayerSession().getLocale().toString());
        return internalLoginState;
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
