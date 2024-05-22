package com.btxtech.common;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;
import elemental2.core.Uint16Array;
import elemental2.dom.Response;
import jsinterop.base.Js;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.TERRAIN_TILE_ABSOLUTE_LENGTH;
import static elemental2.dom.DomGlobal.fetch;

/**
 * Created by Beat
 * on 28.06.2017.
 */
@ApplicationScoped
public class ClientNativeTerrainShapeAccess implements NativeTerrainShapeAccess {
    private final Logger logger = Logger.getLogger(ClientNativeTerrainShapeAccess.class.getName());
    @Inject
    private TerrainService terrainService;
    private NativeTerrainShape nativeTerrainShape;
    private Uint16Array terrainHeightMap;


    @Override
    public void load(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        nativeTerrainShape = null;
        terrainHeightMap = null;

        fetch(CommonUrl.terrainShapeController(planetId))
                .then(Response::json)
                .then(data -> {
                    nativeTerrainShape = Js.uncheckedCast(data);
                    if (terrainHeightMap != null) {
                        loadedCallback.accept(nativeTerrainShape);
                    }
                    return null;
                }).
                catch_(error -> {
                    logger.warning("Error loading " + CommonUrl.terrainShapeController(planetId) + " " + error);
                    failCallback.accept(error.toString());
                    return null;
                });

        fetch(CommonUrl.terrainHeightMapController(planetId))
                .then(Response::arrayBuffer)
                .then(data -> {
                    try {
                        terrainHeightMap = Js.uncheckedCast(new Uint16Array(data));
                    } catch (Throwable t) {
                        logger.log(Level.WARNING, "Error converting HeightMap " + CommonUrl.terrainHeightMapController(planetId), t);
                        terrainHeightMap = new Uint16Array(0);
                    }
                    if (nativeTerrainShape != null) {
                        loadedCallback.accept(nativeTerrainShape);
                    }
                    return null;
                }).
                catch_(error -> {
                    logger.warning("Error loading " + CommonUrl.terrainHeightMapController(planetId) + " " + error);
                    terrainHeightMap = new Uint16Array(0);
                    if (nativeTerrainShape != null) {
                        loadedCallback.accept(nativeTerrainShape);
                    }
                    return null;
                });
    }

    @Override
    public Uint16ArrayEmu createGroundHeightMap(Index terrainTileIndex) {
        int totalTileNodes = (int) TERRAIN_TILE_ABSOLUTE_LENGTH * (int) TERRAIN_TILE_ABSOLUTE_LENGTH;
        int start = totalTileNodes * (terrainTileIndex.getY() * terrainService.getTerrainShape().getTileXCount() + terrainTileIndex.getX());
        return Js.uncheckedCast(terrainHeightMap.slice(start, start + totalTileNodes));
    }
}
