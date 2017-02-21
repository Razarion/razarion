package com.btxtech.shared.rest;

import com.btxtech.shared.dto.LogRecordInfo;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

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
}
