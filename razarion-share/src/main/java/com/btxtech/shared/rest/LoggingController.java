package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.LogRecordInfo;
import org.dominokit.rest.shared.request.service.annotations.RequestFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Path(CommonUrl.REMOTE_LOGGING)
@RequestFactory
public interface LoggingController {

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Path(CommonUrl.LOGGING_SIMPLE)
    void simpleLogger(String logString);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path(CommonUrl.LOGGING_JSON)
    void jsonLogger(LogRecordInfo logRecordInfo);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path(CommonUrl.LOGGING_JSON_DEBUG_DB)
    void jsonDebugDbLogger(String debugMessage);
}
