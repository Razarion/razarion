package com.btxtech.server.rest;

import com.btxtech.server.gameengine.ServerTerrainShapeService;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShape;
import com.btxtech.shared.rest.TerrainShapeController;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 28.06.2017.
 */
public class TerrainShapeControllerImpl implements TerrainShapeController {
    @Inject
    private ServerTerrainShapeService serverTerrainShapeService;
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    public NativeTerrainShape getTerrainShape(int planetId) {
        try {
            return serverTerrainShapeService.getNativeTerrainShape(planetId);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

}
