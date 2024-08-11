package com.btxtech.server.rest;

import com.btxtech.server.frontend.FrontendService;
import com.btxtech.server.frontend.InternalLoginState;
import com.btxtech.server.persistence.tracker.TrackerPersistence;
import com.btxtech.server.user.RegisterService;
import com.btxtech.server.user.UserService;
import com.btxtech.server.util.DateUtil;
import com.btxtech.server.util.ServerUtil;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.FbAuthResponse;
import com.btxtech.shared.dto.ClientLogRecord;
import com.btxtech.shared.dto.FrontendLoginState;
import com.btxtech.shared.dto.LoginResult;
import com.btxtech.shared.dto.RegisterResult;
import com.btxtech.shared.dto.UserRequest;
import com.btxtech.shared.rest.FrontendController;
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
public class FrontendControllerImpl implements FrontendController {
    public static final byte[] PIXEL_BYTES = Base64.getDecoder().decode("R0lGODlhAQABAPAAAAAAAAAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==".getBytes());
    private static final int LOGIN_COOKIE_MAX_AGE = 365 * 24 * 60 * 60;
    private static final int RAZARION_COOKIE_MAX_AGE = 10 * 365 * 24 * 60 * 60;
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
    public FrontendLoginState isLoggedIn(String loginCookieValue, String razarionCookie) {
        try {
            InternalLoginState internalLoginState = frontendService.isLoggedIn(loginCookieValue);
            if (internalLoginState.getLoginCookieValue() != null) {
                httpServletResponse.addCookie(generateLoginServletCookie(internalLoginState.getLoginCookieValue()));
            }
            String razarionCookieValue = frontendService.handleRazarionCookie(razarionCookie);
            if (razarionCookieValue != null) {
                httpServletResponse.addCookie(generateRazarionServletCookie(razarionCookieValue));
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
    public void log(ClientLogRecord clientLogRecord) {
        String aditionalString = "";
        if (clientLogRecord.getUrl() != null) {
            aditionalString += "\nUrl: " + clientLogRecord.getUrl();
        }
        if (clientLogRecord.getError() != null) {
            aditionalString += "\nError: " + clientLogRecord.getError();
        }
        logger.warning("FrontendLogger log: " + LoggingProviderImpl.setupUserWebString(sessionHolder) + "\nMessage: " + clientLogRecord.getMessage() + aditionalString);
    }

    @Override
    public String windowClosed(String url, String stringDate, String stringEvent) {
        try {
            trackerPersistence.onWindowClose(url, ServerUtil.removeTrailingQuotas(stringDate), stringEvent, sessionHolder.getPlayerSession().getHttpSessionId());
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
        return "";
    }

    @Override
    public Response simpleLog(String errorMessage, String timestamp, String pathName) {
        logger.severe("FrontendLogger simpleLog: " + LoggingProviderImpl.setupUserWebString(sessionHolder) + "\nError message: " + errorMessage + "\nTimestamp: " + DateUtil.getDateStringMillis(timestamp) + "\nPath name:" + pathName);
        return Response.ok(PIXEL_BYTES).build();
    }

    @Override
    public Response noScript() {
        logger.warning("FrontendController no script. SessionId: " + sessionHolder.getPlayerSession().getHttpSessionId());
        return Response.ok(PIXEL_BYTES).build();
    }

    @Override
    public LoginResult loginUser(UserRequest userRequest) {
        try {
            LoginResult loginResult = userService.loginUser(userRequest.getEmail(), userRequest.getPassword());
            if (loginResult == LoginResult.OK && userRequest.isRememberMe()) {
                httpServletResponse.addCookie(generateLoginServletCookie(registerService.setupLoginCookieEntry(userRequest.getEmail())));
            }
            return loginResult;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public RegisterResult createUnverifiedUser(String email, String password, boolean rememberMe) {
        try {
            RegisterResult registerResult = userService.createUnverifiedUserAndLogin(email, password);
            if (registerResult == RegisterResult.OK && rememberMe) {
                httpServletResponse.addCookie(generateLoginServletCookie(registerService.setupLoginCookieEntry(email)));
            }
            return registerResult;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            return RegisterResult.UNKNOWN_ERROR;
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
    public void clearRememberMe() {
        httpServletResponse.addCookie(FrontendControllerImpl.generateExpiredLoginServletCookie());
    }

    @Override
    public void trackNavigation(String url) {
        try {
            trackerPersistence.onFrontendNavigation(url, sessionHolder.getPlayerSession().getHttpSessionId());
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
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

    public static javax.servlet.http.Cookie generateRazarionServletCookie(String value) {
        javax.servlet.http.Cookie cookie = new Cookie(CommonUrl.RAZARION_COOKIE_NAME, value);
        cookie.setMaxAge(RAZARION_COOKIE_MAX_AGE);
        cookie.setHttpOnly(true);
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
