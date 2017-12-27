package com.btxtech.shared.rest;

import com.btxtech.shared.datatypes.ErrorResult;
import com.btxtech.shared.datatypes.SetNameResult;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * on 26.12.2017.
 */
@Path(RestUrl.USER_SERVICE_PROVIDER_PATH)
public interface UserServiceProvider {
    @POST
    @Path("setname/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    SetNameResult setName(@PathParam("name") String name);

    @GET
    @Path("verifySetName/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    SetNameResult verifySetName(@PathParam("name") String name); // Top level enum as return type not allowed

}
