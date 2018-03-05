package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.ServerState;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * on 16.02.2018.
 */
@Path(CommonUrl.SERVER_MGMT)
public interface ServerMgmtProvider {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("serverstatus")
    // Enum as return value not working. Not proper JSON. Too many quotation marks.
    String getServerStatus();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("interfaceVersion")
    int getInterfaceVersion();
}
