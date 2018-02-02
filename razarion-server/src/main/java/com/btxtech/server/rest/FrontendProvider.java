package com.btxtech.server.rest;

/**
 * Created by Beat
 * on 27.01.2018.
 */

import com.btxtech.server.frontend.FrontendLoginState;
import com.btxtech.server.frontend.FrontendService;
import com.btxtech.server.frontend.LoginResult;
import com.btxtech.server.user.RegisterService;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.FbAuthResponse;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.logging.Logger;

@Path(CommonUrl.FRONTEND_PATH)
public class FrontendProvider {
    public static final byte[] PIXEL_BYTES = Base64.getDecoder().decode("R0lGODlhAQABAPAAAAAAAAAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==".getBytes());
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("isloggedin")
    public FrontendLoginState isLoggedIn() {
        try {
            return frontendService.isLoggedIn();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            return frontendService.createErrorFrontendLoginState();
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
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("log")
    public void log(String message) {
        logger.warning("FrontendProvider log for session: " + sessionHolder.getPlayerSession().getHttpSessionId() + ". Message: " + message);
    }


    @GET
    @Path("simplelog/{e}/{t}/{p}")
    @Produces({"image/jpeg", "image/png", "image/gif"})
    public Response simpleLog(@PathParam("e") String errorMessage, @PathParam("t") String timestamp, @PathParam("p") String pathName) {
        logger.severe("FrontendProvider: SessionId: " + sessionHolder.getPlayerSession().getHttpSessionId() + " User " + sessionHolder.getPlayerSession().getUserContext());
        logger.severe("FrontendProvider: errorMessage: " + errorMessage + "\ntimestamp: " + timestamp + "\npathName:" + pathName);
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
    @Consumes(MediaType.TEXT_PLAIN)
    public LoginResult loginUser(String email, String password) {
        try {
            return userService.loginUser(email, password);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw new InternalServerErrorException();
        }
    }

    @POST
    @Path("createunverifieduser")
    @Consumes(MediaType.TEXT_PLAIN)
    public boolean createUnverifiedUser(String email, String password) {
        try {
            return userService.createUnverifiedUserAndLogin(email, password);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw new InternalServerErrorException();
        }
    }

    @GET
    @Path("isemailfree")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean isEmailFree(String email) {
        try {
            return userService.verifyEmail(email) == null;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw new InternalServerErrorException();
        }
    }

    @POST
    @Path("verifyemaillink")
    @Consumes(MediaType.TEXT_PLAIN)
    public boolean verifyEmailLink(String verificationId) {
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
    @Consumes(MediaType.TEXT_PLAIN)
    public boolean sendEmailForgotPassword(String email) {
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
    @Consumes(MediaType.TEXT_PLAIN)
    public boolean savePassword(String uuid, String password) {
        try {
            registerService.onPasswordReset(uuid, password);
            return true;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            return false;
        }
    }
}
