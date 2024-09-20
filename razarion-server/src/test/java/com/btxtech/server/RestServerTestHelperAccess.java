package com.btxtech.server;

import com.btxtech.shared.CommonUrl;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Beat
 * on 08.02.2018.
 */
@Path(CommonUrl.SERVER_TEST_HELPER)
public interface RestServerTestHelperAccess {
    @POST
    @Path("setupplanets")
    void setupPlanets();

    @DELETE
    @Path("cleanusers")
    void cleanUsers();

    @DELETE
    @Path("cleanplanets")
    void cleanPlanets();

    @POST
    @Path("startfakemailserver")
    void startFakeMailServer();

    @DELETE
    @Path("stopfakemailserver")
    void stopFakeMailServer();

    @GET
    @Path("getmessagesandclear")
    @Produces(MediaType.APPLICATION_JSON)
    List<FakeEmailDto> getMessagesAndClear();

    @GET
    @Path("getmessagesandclear/{email}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    String getEmailVerificationUuid(@PathParam("email") String email);

    @GET
    @Path("getforgotpassworduuid/{email}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    String getForgotPasswordUuid(@PathParam("email") String email);

}