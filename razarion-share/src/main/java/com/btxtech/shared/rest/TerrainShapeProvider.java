package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShape;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * on 11.02.2018.
 */
@Path(CommonUrl.TERRAIN_SHAPE_PROVIDER)
public interface TerrainShapeProvider {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    NativeTerrainShape getTerrainShape(@PathParam("id") int planetId);
}
