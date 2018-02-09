package com.btxtech.server;

import com.btxtech.shared.CommonUrl;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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