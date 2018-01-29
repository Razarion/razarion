package com.btxtech.server.rest;

/**
 * Created by Beat
 * on 27.01.2018.
 */

import com.btxtech.server.frontend.FrontendLoginState;
import com.btxtech.server.frontend.FrontendService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.FbAuthResponse;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.logging.Logger;

@Path(CommonUrl.FRONTEND_PATH)
public class FrontendProvider {
    @Inject
    private FrontendService frontendService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Logger logger;
    @Inject
    private SessionHolder sessionHolder;

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
    @Path("facebooknotauthorized")
    public void facebookNotAuthorized() {
        try {
            frontendService.facebookNotAuthorized();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @POST
    @Path("nofacebookuser")
    public void noFacebookUser() {
        try {
            frontendService.noFacebookUser();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("log")
    public void log(String message) {
        logger.warning("FrontendProvider log for session :" + sessionHolder.getPlayerSession().getHttpSessionId() + ". Message: " + message);
    }

}
