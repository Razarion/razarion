package com.btxtech.server.rest;

/*
  Created by Beat
  on 27.01.2018.
 */

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
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.logging.Logger;

@Path(CommonUrl.FRONTEND_PATH)
public class FrontendProvider {
    public static final byte[] PIXEL_BYTES = Base64.getDecoder().decode("R0lGODlhAQABAPAAAAAAAAAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==".getBytes());
    private static final String LOGIN_COOKIE_NAME = "LoginToken";
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("isloggedin")
    public Response isLoggedIn(@CookieParam(LOGIN_COOKIE_NAME) String loginCookieValue) {
        try {
            InternalLoginState internalLoginState = frontendService.isLoggedIn(loginCookieValue);
            Response.ResponseBuilder responseBuilder = Response.ok(internalLoginState.getFrontendLoginState());
            if (internalLoginState.getLoginCookieValue() != null) {
                responseBuilder = responseBuilder.cookie(generateLoginCookie(internalLoginState.getLoginCookieValue()));
            }
            return responseBuilder.build();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            return Response.ok(frontendService.createErrorFrontendLoginState()).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("facebookauthenticated")
    public boolean facebookAuthenticated(FbAuthResponse fbAuthResponse) {
        try {
            frontendService.facebookAuthenticated(fbAuthResponse);
            return true;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
        return false;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("log")
    public void log(@FormParam("message") String message, @FormParam("url") String url, @FormParam("error") String error) {
        String aditionalString = "";
        if (url != null) {
            aditionalString += "\nUrl: " + url;
        }
        if (error != null) {
            aditionalString += "\nError: " + error;
        }
        logger.warning("FrontendProvider log\nSessionId: " + sessionHolder.getPlayerSession().getHttpSessionId() + "\nUserContext: " + sessionHolder.getPlayerSession().getUserContext() + "\nMessage: " + message + aditionalString);
    }


    @GET
    @Path("simplelog/{e}/{t}/{p}")
    @Produces({"image/jpeg", "image/png", "image/gif"})
    public Response simpleLog(@PathParam("e") String errorMessage, @PathParam("t") String timestamp, @PathParam("p") String pathName) {
        logger.severe("FrontendProvider simpleLog\nSessionId: " + sessionHolder.getPlayerSession().getHttpSessionId() + "\nUserContext " + sessionHolder.getPlayerSession().getUserContext() + "\nerrorMessage: " + errorMessage + "\ntimestamp: " + timestamp + "\npathName:" + pathName);
        return Response.ok(PIXEL_BYTES).build();
    }

    @GET
    @Path("noscript")
    @Produces({"image/jpeg", "image/png", "image/gif"})
    public Response noScript() {
        logger.warning("FrontendProvider no script. SessionId: " + sessionHolder.getPlayerSession().getHttpSessionId());
        return Response.ok(PIXEL_BYTES).build();
    }

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(@FormParam("email") String email, @FormParam("password") String password, @FormParam("rememberMe") boolean rememberMe) {
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

    @POST
    @Path("createunverifieduser")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUnverifiedUser(@FormParam("email") String email, @FormParam("password") String password, @FormParam("rememberMe") boolean rememberMe) {
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

    @GET
    @Path("isemailfree/{email}")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean isEmailFree(@PathParam("email") String email) {
        try {
            return userService.verifyEmail(email) == null;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw new InternalServerErrorException();
        }
    }

    @POST
    @Path("verifyemaillink")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public boolean verifyEmailLink(@FormParam("verificationId") String verificationId) {
        try {
            registerService.onEmailVerificationPageCalled(verificationId);
            return true;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            return false;
        }
    }

    @POST
    @Path("sendemailforgotpassword")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public boolean sendEmailForgotPassword(@FormParam("email") String email) {
        try {
            registerService.onForgotPassword(email);
            return true;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            return false;
        }
    }

    @POST
    @Path("savepassword")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public boolean savePassword(@FormParam("uuid") String uuid, @FormParam("password") String password) {
        try {
            registerService.onPasswordReset(uuid, password);
            return true;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            return false;
        }
    }

    @POST
    @Path("logout")
    public void savePassword() {
        try {
            userService.logout();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @POST
    @Path("tracknavigation")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void trackNavigation(@FormParam("url") String url) {
        try {
            trackerPersistence.onFrontendNavigation(url, sessionHolder.getPlayerSession().getHttpSessionId());
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    private NewCookie generateLoginCookie(String value) {
        return new NewCookie(LOGIN_COOKIE_NAME, value, null, null, NewCookie.DEFAULT_VERSION, null, LOGIN_COOKIE_MAX_AGE, null, true, true);
    }
}
