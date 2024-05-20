package com.btxtech.server.rest;

import com.btxtech.shared.rest.TerrainHeightMapController;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

public class TerrainHeightMapControllerImpl implements TerrainHeightMapController {
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    public Response getHeightMap(int planetId) {
        StreamingOutput stream = output -> {
            try {
                if (TerrainEditorControllerImpl.zippedHeightMap != null) {
                    output.write(TerrainEditorControllerImpl.zippedHeightMap);
                } else {
                    output.write(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
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
