package com.btxtech.server.rest;

import com.btxtech.server.persistence.PlanetCrudPersistence;
import com.btxtech.shared.rest.TerrainHeightMapController;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.util.logging.Logger;

public class TerrainHeightMapControllerImpl implements TerrainHeightMapController {
    private static final Logger LOG = Logger.getLogger(TerrainHeightMapControllerImpl.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private PlanetCrudPersistence planetCrudPersistence;

    @Override
    public Response getCompressedHeightMap(int planetId) {
        StreamingOutput stream = output -> {
            try {
                byte[] compressedHeightMap = planetCrudPersistence.getCompressedHeightMap(planetId);
                if (compressedHeightMap != null) {
                    output.write(compressedHeightMap);
                } else {
                    LOG.severe("Planet " + planetId + " has no compressed heightmap");
                }
            } catch (Exception e) {
                exceptionHandler.handleException(e);
            }
        };
        return Response.ok(stream, "application/octet-stream")
                .encoding("gzip")
                .build();
    }
}
