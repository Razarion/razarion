package com.btxtech.server.rest;

import com.btxtech.shared.rest.RestUrl;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 18.02.2017.
 */
@Path(RestUrl.REMOTE_LOGGING)
public class LoggingProvider {
    private Logger logger = Logger.getLogger(LoggingProvider.class.getName());

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Path(RestUrl.LOGGING_SIMPLE)
    public void simpleLogger(String logString) {
        logger.severe("SimpleLogger: " + logString);
    }

}
