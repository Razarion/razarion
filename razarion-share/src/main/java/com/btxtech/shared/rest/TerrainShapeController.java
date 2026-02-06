package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * on 11.02.2018.
 */
@Path(CommonUrl.TERRAIN_SHAPE_CONTROLLER)
public interface TerrainShapeController {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    NativeTerrainShape getTerrainShape(@PathParam("id") int planetId);

    @PUT
    @Path("{id}")
    void createTerrainShape(@PathParam("id") int planetId);
}
