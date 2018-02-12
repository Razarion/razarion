package com.btxtech.server.rest;

import com.btxtech.server.gameengine.TerrainShapeService;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShape;
import com.btxtech.shared.rest.TerrainShapeProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 28.06.2017.
 */
public class TerrainShapeProviderImpl implements TerrainShapeProvider {
    @Inject
    private TerrainShapeService terrainShapeService;
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    public NativeTerrainShape getTerrainShape(int planetId) {
        try {
            return terrainShapeService.getNativeTerrainShape(planetId);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

}
