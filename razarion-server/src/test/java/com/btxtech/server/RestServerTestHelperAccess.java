package com.btxtech.server;

import com.btxtech.shared.CommonUrl;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

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
}
