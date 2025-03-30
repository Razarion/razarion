package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Beat
 * on 11.02.2018.
 */
@Path(CommonUrl.TERRAIN_HEIGHT_MAP_CONTROLLER)
public interface TerrainHeightMapController {
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("{id}")
    Object getCompressedHeightMap(@PathParam("id") int planetId);
}
