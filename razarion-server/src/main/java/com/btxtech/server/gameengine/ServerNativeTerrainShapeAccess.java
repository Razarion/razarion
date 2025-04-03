package com.btxtech.server.gameengine;

import com.btxtech.server.service.engine.ServerTerrainShapeService;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class ServerNativeTerrainShapeAccess implements NativeTerrainShapeAccess {
    private final ServerTerrainShapeService serverTerrainShapeService;
    private int[] groundHeightMap;

    public ServerNativeTerrainShapeAccess(ServerTerrainShapeService serverTerrainShapeService) {
        this.serverTerrainShapeService = serverTerrainShapeService;
    }

    @Override
    public void load(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        loadedCallback.accept(serverTerrainShapeService.getNativeTerrainShape(planetId));
    }

    @Override
    public Uint16ArrayEmu createTileGroundHeightMap(Index terrainTileIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getGroundHeightAt(int index) {
        return serverTerrainShapeService.getGroundHeightAt(index);
    }
}
