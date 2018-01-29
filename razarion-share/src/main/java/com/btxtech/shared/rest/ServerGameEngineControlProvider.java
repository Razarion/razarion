package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * Created by Beat
 * on 29.08.2017.
 */
@Path(CommonUrl.SERVER_GAME_ENGINE_CONTROL_PROVIDER_PATH)
public interface ServerGameEngineControlProvider {
    @POST
    @Path("restartBots")
    void restartBots();

    @POST
    @Path("reloadStatic")
    void reloadStatic();

    @POST
    @Path("restartResourceRegions")
    void restartResourceRegions();

    @POST
    @Path("reloadPlanet")
    void reloadPlanet();

    @POST
    @Path("restartBoxRegions")
    void restartBoxRegions();
}
