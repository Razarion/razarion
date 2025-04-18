package com.btxtech.server.rest.engine;

import com.btxtech.server.service.engine.ServerGameEngineCrudPersistence;
import com.btxtech.server.service.engine.ServerTerrainShapeService;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.dto.ServerGameEngineConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;
import com.btxtech.shared.rest.TerrainShapeController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/gz/terrainshape")
public class TerrainShapeControllerImpl implements TerrainShapeController {
    private final ServerTerrainShapeService serverTerrainShapeService;
    private final ServerGameEngineCrudPersistence serverGameEngineCrudPersistence;

    public TerrainShapeControllerImpl(ServerTerrainShapeService serverTerrainShapeService, ServerGameEngineCrudPersistence serverGameEngineCrudPersistence) {
        this.serverTerrainShapeService = serverTerrainShapeService;
        this.serverGameEngineCrudPersistence = serverGameEngineCrudPersistence;
    }

    @Override
    @GetMapping(value = "/{planetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public NativeTerrainShape getTerrainShape(@PathVariable("planetId") int planetId) {
        return serverTerrainShapeService.getNativeTerrainShape(planetId);
    }

    @Override
    @SecurityCheck
    public void createTerrainShape(int planetId) {
        ServerGameEngineConfig serverGameEngineConfig = serverGameEngineCrudPersistence.read().get(0);
        serverTerrainShapeService.createTerrainShape(serverGameEngineConfig.getBotConfigs(), planetId);
    }
}
