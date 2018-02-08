package com.btxtech.server.rest;

import com.btxtech.server.frontend.FrontendLoginState;
import com.btxtech.server.frontend.FrontendService;
import com.btxtech.server.frontend.InternalLoginState;
import com.btxtech.server.frontend.LoginResult;
import com.btxtech.server.persistence.tracker.TrackerPersistence;
import com.btxtech.server.user.RegisterResult;
import com.btxtech.server.user.RegisterService;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.FbAuthResponse;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.logging.Logger;

/*
  Created by Beat
  on 27.01.2018.
 */
public class FrontendProviderImpl implements FrontendProvider {
    public static final byte[] PIXEL_BYTES = Base64.getDecoder().decode("R0lGODlhAQABAPAAAAAAAAAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==".getBytes());
    private static final int LOGIN_COOKIE_MAX_AGE = 365 * 24 * 60 * 60;
    @Inject
    private FrontendService frontendService;
    @Inject
    private RegisterService registerService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Logger logger;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private UserService userService;
    @Inject
    private TrackerPersistence trackerPersistence;
    @Context
    private HttpServletResponse httpServletResponse;

    @Override
    public FrontendLoginState isLoggedIn(String loginCookieValue) {
        try {
            InternalLoginState internalLoginState = frontendService.isLoggedIn(loginCookieValue);
            if (internalLoginState.getLoginCookieValue() != null) {
                httpServletResponse.addCookie(generateLoginServletCookie(internalLoginState.getLoginCookieValue()));
            }
            return internalLoginState.getFrontendLoginState();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            return frontendService.createErrorFrontendLoginState();
        }
    }

    @Override
    public boolean facebookAuthenticated(FbAuthResponse fbAuthResponse) {
        try {
            frontendService.facebookAuthenticated(fbAuthResponse);
            return true;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
        return false;
    }

    @Override
    public void log(String message, String url, String error) {
        String aditionalString = "";
        if (url != null) {
            aditionalString += "\nUrl: " + url;
        }
        if (error != null) {
            aditionalString += "\nError: " + error;
        }
        logger.warning("FrontendProvider log\nSessionId: " + sessionHolder.getPlayerSession().getHttpSessionId() + "\nUserContext: " + sessionHolder.getPlayerSession().getUserContext() + "\nMessage: " + message + aditionalString);
    }


    @Override
    public Response simpleLog(String errorMessage, String timestamp, String pathName) {
        logger.severe("FrontendProvider simpleLog\nSessionId: " + sessionHolder.getPlayerSession().getHttpSessionId() + "\nUserContext " + sessionHolder.getPlayerSession().getUserContext() + "\nerrorMessage: " + errorMessage + "\ntimestamp: " + timestamp + "\npathName:" + pathName);
        return Response.ok(PIXEL_BYTES).build();
    }

    @Override
    public Response noScript() {
        logger.warning("FrontendProvider no script. SessionId: " + sessionHolder.getPlayerSession().getHttpSessionId());
        return Response.ok(PIXEL_BYTES).build();
    }

    @Override
    public Response loginUser(String email, String password, boolean rememberMe) {
        try {
            LoginResult loginResult = userService.loginUser(email, password);
            Response.ResponseBuilder responseBuilder = Response.ok(loginResult);
            if (loginResult == LoginResult.OK && rememberMe) {
                responseBuilder = responseBuilder.cookie(generateLoginCookie(registerService.setupLoginCookieEntry(email)));
            }
            return responseBuilder.build();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw new InternalServerErrorException();
        }
    }

    @Override
    public Response createUnverifiedUser(String email, String password, boolean rememberMe) {
        try {
            RegisterResult registerResult = userService.createUnverifiedUserAndLogin(email, password);
            Response.ResponseBuilder responseBuilder = Response.ok(registerResult);
            if (registerResult == RegisterResult.OK && rememberMe) {
                responseBuilder = responseBuilder.cookie(generateLoginCookie(registerService.setupLoginCookieEntry(email)));
            }
            return responseBuilder.build();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            return Response.ok(RegisterResult.UNKNOWN_ERROR).build();
        }
    }

    @Override
    public boolean isEmailFree(String email) {
        try {
            return userService.verifyEmail(email) == null;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw new InternalServerErrorException();
        }
    }

    @Override
    public boolean verifyEmailLink(String verificationId) {
        try {
            registerService.onEmailVerificationPageCalled(verificationId);
            return true;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            return false;
        }
    }

    @Override
    public boolean sendEmailForgotPassword(String email) {
        try {
            registerService.onForgotPassword(email);
            return true;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            return false;
        }
    }

    @Override
    public boolean savePassword(String uuid, String password) {
        try {
            registerService.onPasswordReset(uuid, password);
            return true;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            return false;
        }
    }

    @Override
    public void logout() {
        try {
            userService.logout();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Override
    public void trackNavigation(String url) {
        try {
            trackerPersistence.onFrontendNavigation(url, sessionHolder.getPlayerSession().getHttpSessionId());
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public static NewCookie generateLoginCookie(String value) {
        return new NewCookie(CommonUrl.LOGIN_COOKIE_NAME, value, "/", null, NewCookie.DEFAULT_VERSION, null, LOGIN_COOKIE_MAX_AGE, null, true, true);
    }

    public static javax.servlet.http.Cookie generateLoginServletCookie(String value) {
        javax.servlet.http.Cookie cookie = new Cookie(CommonUrl.LOGIN_COOKIE_NAME, value);
        cookie.setMaxAge(LOGIN_COOKIE_MAX_AGE);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setVersion(NewCookie.DEFAULT_VERSION);
        cookie.setPath("/");
        return cookie;
    }

    public static javax.servlet.http.Cookie generateExpiredLoginServletCookie() {
        javax.servlet.http.Cookie cookie = new Cookie(CommonUrl.LOGIN_COOKIE_NAME, "");
        cookie.setMaxAge(-1);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setVersion(NewCookie.DEFAULT_VERSION);
        cookie.setPath("/");
        return cookie;
    }
}
