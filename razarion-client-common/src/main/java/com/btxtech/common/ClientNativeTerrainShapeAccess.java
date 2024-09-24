package com.btxtech.common;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;
import com.btxtech.shared.system.ExceptionHandler;
import elemental2.core.ArrayBufferView;
import elemental2.core.Uint16Array;
import elemental2.dom.Response;
import jsinterop.base.Js;

import javax.inject.Singleton;
import javax.inject.Inject;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.*;
import static elemental2.dom.DomGlobal.fetch;

/**
 * Created by Beat
 * on 28.06.2017.
 */
@Singleton
public class ClientNativeTerrainShapeAccess implements NativeTerrainShapeAccess {
    private final Logger logger = Logger.getLogger(ClientNativeTerrainShapeAccess.class.getName());

    private TerrainService terrainService;

    private ExceptionHandler exceptionHandler;
    private NativeTerrainShape nativeTerrainShape;
    private Uint16Array terrainHeightMap;

    @Inject
    public ClientNativeTerrainShapeAccess(ExceptionHandler exceptionHandler, TerrainService terrainService) {
        this.exceptionHandler = exceptionHandler;
        this.terrainService = terrainService;
    }


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
    public Uint16ArrayEmu createTileGroundHeightMap(Index terrainTileIndex) {
        int tileHeightMapStart = getTileHeightMapStart(terrainTileIndex);
        int nextXTileHeightMapStart = getTileHeightMapStart(terrainTileIndex.add(1, 0));
        int nextYTileHeightMapStart = getTileHeightMapStart(terrainTileIndex.add(0, 1));
        int nextXYTileHeightMapStart = getTileHeightMapStart(terrainTileIndex.add(1, 1));

        Uint16Array resultArray = new Uint16Array((NODE_X_COUNT + 1) * (NODE_Y_COUNT + 1));

        for (int i = 0; i < NODE_Y_COUNT; i++) {
            int sourceYOffset = i * NODE_X_COUNT;
            int sourceHeightMapStart = tileHeightMapStart + sourceYOffset;
            int sourceHeightMapEnd = sourceHeightMapStart + NODE_X_COUNT;
            int destHeightMapStart = i * (NODE_X_COUNT + 1);
            try {
                ArrayBufferView arrayBufferView = Js.uncheckedCast(terrainHeightMap.slice(sourceHeightMapStart, sourceHeightMapEnd));
                resultArray.set(arrayBufferView, destHeightMapStart);
                // Add from next X tile
                int sourceNextTileHeightMapStart;
                if (terrainTileIndex.getX() + 1 < terrainService.getTerrainShape().getTileXCount()) {
                    // Inside
                    sourceNextTileHeightMapStart = nextXTileHeightMapStart + sourceYOffset;
                } else {
                    // Outside
                    sourceNextTileHeightMapStart = sourceHeightMapEnd + 1;
                }
                ArrayBufferView arrayBufferViewEast = Js.uncheckedCast(terrainHeightMap.slice(sourceNextTileHeightMapStart, sourceNextTileHeightMapStart + 1));
                resultArray.set(arrayBufferViewEast, destHeightMapStart + NODE_X_COUNT);

                // Add last north row with next values
                if (i == NODE_Y_COUNT - 1) {
                    if (terrainTileIndex.getY() + 1 < terrainService.getTerrainShape().getTileYCount()) {
                        ArrayBufferView arrayBufferViewEastNorth = Js.uncheckedCast(terrainHeightMap.slice(nextYTileHeightMapStart, nextYTileHeightMapStart + NODE_X_COUNT));
                        resultArray.set(arrayBufferViewEastNorth, destHeightMapStart + NODE_X_COUNT + 1);
                        // Add from next X tile
                        if (terrainTileIndex.getX() + 1 < terrainService.getTerrainShape().getTileXCount()) {
                            // Inside
                            sourceNextTileHeightMapStart = nextXYTileHeightMapStart;
                        } else {
                            // Outside
                            sourceNextTileHeightMapStart = nextYTileHeightMapStart + NODE_X_COUNT + 1;
                        }
                        ArrayBufferView arrayBufferViewNorthEast = Js.uncheckedCast(terrainHeightMap.slice(sourceNextTileHeightMapStart, sourceNextTileHeightMapStart + 1));
                        resultArray.set(arrayBufferViewNorthEast, destHeightMapStart + NODE_X_COUNT + 1 + NODE_X_COUNT);
                    } else {
                        resultArray.set(arrayBufferView, destHeightMapStart + NODE_X_COUNT + 1);
                        // Add from next X tile
                        if (terrainTileIndex.getX() + 1 < terrainService.getTerrainShape().getTileXCount()) {
                            // Inside
                            sourceNextTileHeightMapStart = nextXTileHeightMapStart + sourceYOffset;
                        } else {
                            // Outside
                            sourceNextTileHeightMapStart = sourceHeightMapEnd;
                        }
                        ArrayBufferView arrayBufferViewNorthEast = Js.uncheckedCast(terrainHeightMap.slice(sourceNextTileHeightMapStart, sourceNextTileHeightMapStart + 1));
                        resultArray.set(arrayBufferViewNorthEast, destHeightMapStart + NODE_X_COUNT + 1 + NODE_X_COUNT);
                    }
                }
            } catch (Throwable t) {
                exceptionHandler.handleException("sourceHeightMapStart: " + sourceHeightMapStart + " sourceHeightMapEnd: " + sourceHeightMapEnd + " tileHeightMapStart: " + tileHeightMapStart, t);
            }
        }
        return Js.uncheckedCast(resultArray);
    }

    private int getTileHeightMapStart(Index terrainTileIndex) {
        return terrainTileIndex.getY() * (terrainService.getTerrainShape().getTileXCount() * TILE_NODE_SIZE) + terrainTileIndex.getX() * TILE_NODE_SIZE;
    }

    @Override
    public int getGroundHeightAt(int index) {
        return terrainHeightMap.getAt(index).intValue();
    }
}
