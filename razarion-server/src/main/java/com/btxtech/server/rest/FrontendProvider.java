package com.btxtech.server.rest;

import com.btxtech.server.frontend.FrontendLoginState;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.FbAuthResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Beat
 * on 08.02.2018.
 */
@Path(CommonUrl.FRONTEND_PATH)
public interface FrontendProvider {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("isloggedin")
    FrontendLoginState isLoggedIn(@CookieParam(CommonUrl.LOGIN_COOKIE_NAME) String loginCookieValue);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("facebookauthenticated")
    boolean facebookAuthenticated(FbAuthResponse fbAuthResponse);

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("log")
    void log(@FormParam("message") String message, @FormParam("url") String url, @FormParam("error") String error);

    @GET
    @Path("simplelog/{e}/{t}/{p}")
    @Produces({"image/jpeg", "image/png", "image/gif"})
    Response simpleLog(@PathParam("e") String errorMessage, @PathParam("t") String timestamp, @PathParam("p") String pathName);

    @GET
    @Path("noscript")
    @Produces({"image/jpeg", "image/png", "image/gif"})
    Response noScript();

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    Response loginUser(@FormParam("email") String email, @FormParam("password") String password, @FormParam("rememberMe") boolean rememberMe);

    @POST
    @Path("createunverifieduser")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    Response createUnverifiedUser(@FormParam("email") String email, @FormParam("password") String password, @FormParam("rememberMe") boolean rememberMe);

    @GET
    @Path("isemailfree/{email}")
    @Produces(MediaType.TEXT_PLAIN)
    boolean isEmailFree(@PathParam("email") String email);

    @POST
    @Path("verifyemaillink")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    boolean verifyEmailLink(@FormParam("verificationId") String verificationId);

    @POST
    @Path("sendemailforgotpassword")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    boolean sendEmailForgotPassword(@FormParam("email") String email);

    @POST
    @Path("savepassword")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    boolean savePassword(@FormParam("uuid") String uuid, @FormParam("password") String password);

    @POST
    @Path("logout")
    void logout();

    @POST
    @Path("tracknavigation")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    void trackNavigation(@FormParam("url") String url);
}
