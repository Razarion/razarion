package com.btxtech.server.rest.engine;

import com.btxtech.server.model.Roles;
import com.btxtech.server.service.engine.ServerGameEngineService;
import com.btxtech.server.service.engine.ServerTerrainShapeService;
import com.btxtech.shared.dto.ServerGameEngineConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;
import com.btxtech.shared.rest.TerrainShapeController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/terrainshape")
public class TerrainShapeControllerImpl {
    private final ServerTerrainShapeService serverTerrainShapeService;
    private final ServerGameEngineService serverGameEngineCrudPersistence;

    public TerrainShapeControllerImpl(ServerTerrainShapeService serverTerrainShapeService, ServerGameEngineService serverGameEngineCrudPersistence) {
        this.serverTerrainShapeService = serverTerrainShapeService;
        this.serverGameEngineCrudPersistence = serverGameEngineCrudPersistence;
    }

    @GetMapping(value = "/{planetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public NativeTerrainShape getTerrainShape(@PathVariable("planetId") int planetId) {
        return serverTerrainShapeService.getNativeTerrainShape(planetId);
    }

    @PreAuthorize("hasAuthority('ADMIN')") 
    public void createTerrainShape(int planetId) {
        ServerGameEngineConfig serverGameEngineConfig = serverGameEngineCrudPersistence.read().get(0);
        serverTerrainShapeService.createTerrainShape(serverGameEngineConfig.getBotConfigs(), planetId);
    }
}
