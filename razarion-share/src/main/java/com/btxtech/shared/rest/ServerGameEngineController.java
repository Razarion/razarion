package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path(CommonUrl.SERVER_GAME_ENGINE_PATH)
public interface ServerGameEngineController {
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
    @Path("reloadPlanetShapes")
    void reloadPlanetShapes();

    @POST
    @Path("restartBoxRegions")
    void restartBoxRegions();

    @POST
    @Path("restartPlanetWarm")
    void restartPlanetWarm();

    @POST
    @Path("restartPlanetCold")
    void restartPlanetCold();

    @DELETE
    @Path("deletebase/{baseId}")
    void deleteBase(@PathParam("baseId") int baseId);

}
