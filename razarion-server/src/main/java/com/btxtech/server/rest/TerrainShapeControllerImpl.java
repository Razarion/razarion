package com.btxtech.server.rest;

import com.btxtech.server.gameengine.ServerTerrainShapeService;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShape;
import com.btxtech.shared.rest.TerrainShapeController;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 28.06.2017.
 */
public class TerrainShapeControllerImpl implements TerrainShapeController {
    @Inject
    private ServerTerrainShapeService serverTerrainShapeService;

    @Override
    public NativeTerrainShape getTerrainShape(int planetId) {
        return serverTerrainShapeService.getNativeTerrainShape(planetId);
    }

    @Override
    @SecurityCheck
    public void createTerrainShape(int planetId) {
        serverTerrainShapeService.createTerrainShape(planetId);
    }
}
