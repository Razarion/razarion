package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.FbAuthResponse;
import com.btxtech.shared.dto.ClientLogRecord;
import com.btxtech.shared.dto.FrontendLoginState;
import com.btxtech.shared.dto.LoginResult;
import com.btxtech.shared.dto.RegisterResult;
import com.btxtech.shared.dto.UserRequest;

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
public interface FrontendController {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("isloggedin")
    FrontendLoginState isLoggedIn(@CookieParam(CommonUrl.LOGIN_COOKIE_NAME) String loginCookieValue, @CookieParam(CommonUrl.RAZARION_COOKIE_NAME) String razarionCookie);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("facebookauthenticated")
    boolean facebookAuthenticated(FbAuthResponse fbAuthResponse);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("log")
    void log(ClientLogRecord clientLogRecord);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("windowclosed/{url}/{time}/{event}")
    String windowClosed(@PathParam("url") String url, @PathParam("time") String stringDate, @PathParam("event") String stringEvent);

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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    LoginResult loginUser(UserRequest userRequest);

    @POST
    @Path("createunverifieduser")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    RegisterResult createUnverifiedUser(@FormParam("email") String email, @FormParam("password") String password, @FormParam("rememberMe") boolean rememberMe);

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
    @Path("clearrememberme")
    void clearRememberMe();

    @POST
    @Path("logout")
    void logout();

    @POST
    @Path("tracknavigation")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    void trackNavigation(@FormParam("url") String url);
}
