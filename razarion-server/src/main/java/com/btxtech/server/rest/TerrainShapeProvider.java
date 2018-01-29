package com.btxtech.server.rest;

import com.btxtech.server.gameengine.TerrainShapeService;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShape;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * on 28.06.2017.
 */
@Path(CommonUrl.TERRAIN_SHAPE_PROVIDER)
public class TerrainShapeProvider {
    @Inject
    private TerrainShapeService terrainShapeService;
    @Inject
    private ExceptionHandler exceptionHandler;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public NativeTerrainShape getTerrainShape(@PathParam("id") int planetId) {
        try {
            return terrainShapeService.getNativeTerrainShape(planetId);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

}
