package com.btxtech.shared.rest;

import com.btxtech.shared.dto.LogRecordInfo;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Path(RestUrl.REMOTE_LOGGING)
public interface LoggingProvider {

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Path(RestUrl.LOGGING_SIMPLE)
    void simpleLogger(String logString);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path(RestUrl.LOGGING_JSON)
    void jsonLogger(LogRecordInfo logRecordInfo);

    @GET
    @Path("simpleweb/{e}/{t}/{p}")
    @Produces({"image/jpeg", "image/png", "image/gif"})
    Response simpleWebLogger(@PathParam("e") String errorMessage, @PathParam("t") String timestamp, @PathParam("p") String pathName);
}
