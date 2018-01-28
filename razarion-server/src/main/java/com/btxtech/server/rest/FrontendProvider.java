package com.btxtech.server.rest;

/**
 * Created by Beat
 * on 27.01.2018.
 */

import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.rest.RestUrl;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(RestUrl.FRONTEND_PATH)
public class FrontendProvider {
    @Inject
    private SessionHolder sessionHolder;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("isloggedin")
    public boolean isLoggedIn() {
        System.out.println("--------------- isLoggedIn ---------------------: " + sessionHolder.isLoggedIn());
        // return sessionHolder.isLoggedIn();
        return false;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("facebookauthenticated")
    public void facebookAuthenticated() {
        System.out.println("--------------- facebookAuthenticated ---------------------");
    }

    @POST
    @Path("facebooknotauthorized")
    public void facebookNotAuthorized() {
        System.out.println("--------------- facebookAuthenticated ---------------------");
    }

    @POST
    @Path("nofacebookuser")
    public void noFacebookUser() {
        System.out.println("--------------- noFacebookUser ---------------------");
    }

}
