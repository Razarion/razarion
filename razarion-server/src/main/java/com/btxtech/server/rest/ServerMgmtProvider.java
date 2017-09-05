package com.btxtech.server.rest;

import com.btxtech.server.mgmt.ConnectionMgmt;
import com.btxtech.server.mgmt.OnlineInfo;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Beat
 * on 05.09.2017.
 */
@Path(RestUrl.SERVER_MGMT_PROVIDER_PATH)
public class ServerMgmtProvider {
    @Inject
    private ConnectionMgmt connectionMgmt;
    @Inject
    private ExceptionHandler exceptionHandler;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("loadallonlines")
    public List<OnlineInfo> loadAllOnlines() {
        try {
            return connectionMgmt.loadAllOnlines();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

}
