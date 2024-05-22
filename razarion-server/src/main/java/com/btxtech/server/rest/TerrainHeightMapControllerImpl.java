package com.btxtech.server.rest;

import com.btxtech.server.persistence.PlanetCrudPersistence;
import com.btxtech.shared.rest.TerrainHeightMapController;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

public class TerrainHeightMapControllerImpl implements TerrainHeightMapController {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private PlanetCrudPersistence planetCrudPersistence;

    @Override
    public Response getCompressedHeightMap(int planetId) {
        StreamingOutput stream = output -> {
            try {
                output.write(planetCrudPersistence.getCompressedHeightMap(planetId));
            } catch (Exception e) {
                exceptionHandler.handleException(e);
            }
        };
        return Response.ok(stream, "application/octet-stream")
                .encoding("gzip")
                .build();
    }
}
