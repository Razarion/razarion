package com.btxtech.shared.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * Created by Beat
 * on 29.08.2017.
 */
@Path(RestUrl.SERVER_GAME_ENGINE_CONTROL_PROVIDER_PATH)
public interface ServerGameEngineControlProvider {
    @POST
    @Path("restartBots")
    void restartBots();

}
