package com.btxtech.worker;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeDecimalPosition;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeBabylonDecal;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeBotGround;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeBotGroundSlopeBox;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeObjectList;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeObjectPosition;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeTile;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeVertex;
import com.btxtech.worker.jso.JsConsole;
import com.btxtech.worker.jso.JsFetch;
import com.btxtech.worker.jso.JsUtils;
import com.btxtech.worker.jso.dto.JsNativeBabylonDecal;
import com.btxtech.worker.jso.dto.JsNativeBotGround;
import com.btxtech.worker.jso.dto.JsNativeBotGroundSlopeBox;
import com.btxtech.worker.jso.dto.JsNativeDecimalPosition;
import com.btxtech.worker.jso.dto.JsNativeTerrainShape;
import com.btxtech.worker.jso.dto.JsNativeTerrainShapeObjectList;
import com.btxtech.worker.jso.dto.JsNativeTerrainShapeObjectPosition;
import com.btxtech.worker.jso.dto.JsNativeTerrainShapeTile;
import com.btxtech.worker.jso.dto.JsNativeVertex;
import com.btxtech.worker.jso.JsUint16ArrayWrapper;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.ArrayBufferView;
import org.teavm.jso.typedarrays.Uint16Array;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.NODE_X_COUNT;
import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.NODE_Y_COUNT;
import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.TILE_NODE_SIZE;

/**
 * TeaVM implementation of NativeTerrainShapeAccess
 * Handles terrain data loading using Fetch API
 */
@Singleton
public class TeaVMNativeTerrainShapeAccess implements NativeTerrainShapeAccess {
    private final TerrainService terrainService;
    private NativeTerrainShape nativeTerrainShape;
    /**
     * One Uint16Array per tile, or null for a tile that has not been loaded. Indexed the way the
     * height map is laid out: {@code tileY * tileXCount + tileX}, tiles contiguous, rows contiguous
     * inside a tile.
     */
    private JSObject tileStore;
    /** One height per tile, the value a flat tile has edge to edge. See {@link #flatValues}. */
    private Uint16Array flatValues;
    private int tileXCount;
    private int tileYCount;
    /** Set once both the flat table and the first region have arrived. */
    private boolean heightsLoaded;

    @Inject
    public TeaVMNativeTerrainShapeAccess(TerrainService terrainService) {
        this.terrainService = terrainService;
    }

    /**
     * Three fetches, and the game waits for all of them.
     * <p>
     * The height map arrives by the tile now rather than as one array. Today that is still the
     * whole planet in one rectangle, so nothing about what is loaded has changed - only how it is
     * held, which is what lets a later change ask for the player's corner and stream the rest.
     * <p>
     * The flat table is the piece that makes a partly loaded map safe: for the 70% of tiles that
     * are one height edge to edge it answers exactly, not approximately. That matters because the
     * server runs the planet on the full map - a client whose height differs anywhere reads a
     * different terrain type there, walks a different path, and drifts out of sync.
     */
    @Override
    public void load(int planetId, int tileXCount, int tileYCount, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        nativeTerrainShape = null;
        this.tileXCount = tileXCount;
        this.tileYCount = tileYCount;
        this.heightsLoaded = false;
        this.flatValues = null;
        this.tileStore = createTileStore(tileXCount * tileYCount);

        loadTerrainShape(planetId, loadedCallback, failCallback);
        loadHeights(planetId, loadedCallback);
    }

    /**
     * The flat table first, then the heights - in that order, because a height read that arrives
     * before its tile does has to fall back on the table, and a table that is not there yet would
     * answer zero. Zero is sea level minus two hundred metres, which the pathing would read as
     * water.
     */
    private void loadHeights(int planetId, Consumer<NativeTerrainShape> loadedCallback) {
        fetchArrayBuffer(CommonUrl.terrainHeightMapFlatController(planetId), flatBuffer -> {
            try {
                flatValues = readFlatTable(flatBuffer, tileXCount, tileYCount);
            } catch (Throwable t) {
                JsConsole.warn("Error reading the flat height table: " + t.getMessage());
                flatValues = createUint16Array(tileXCount * tileYCount);
            }
            loadRegion(planetId, 0, 0, tileXCount, tileYCount, loadedCallback);
        }, error -> {
            JsConsole.warn("Failed to load the flat height table: " + error);
            flatValues = createUint16Array(tileXCount * tileYCount);
            loadRegion(planetId, 0, 0, tileXCount, tileYCount, loadedCallback);
        });
    }

    /**
     * A rectangle of tiles into the store. Each tile in the response is delta encoded from zero, so
     * a tile can be read without the tiles before it - see HeightMapRegionService on the server.
     * <p>
     * A failure leaves the store as it is and carries on: every tile then answers from the flat
     * table, which is a flat planet rather than no planet. The same choice the single fetch made
     * before, for the same reason - telemetry and terrain are not worth a start.
     */
    private void loadRegion(int planetId, int tileX, int tileY, int countX, int countY,
                            Consumer<NativeTerrainShape> loadedCallback) {
        fetchArrayBuffer(CommonUrl.terrainHeightMapRegionController(planetId, tileX, tileY, countX, countY), buffer -> {
            try {
                storeRegion(buffer, tileStore, tileXCount, TILE_NODE_SIZE, NODE_X_COUNT);
            } catch (Throwable t) {
                // An error rather than a warning, and said out loud: this is the one failure that is
                // otherwise invisible. Wrong heights draw a planet of the wrong shape and nothing
                // complains - no exception, no missing tile, just cliffs where there is meadow.
                JsConsole.error("Height map region rejected: " + t.getMessage());
            }
            heightsLoaded = true;
            checkBothLoaded(loadedCallback);
        }, error -> {
            JsConsole.warn("Failed to load a height map region: " + error);
            heightsLoaded = true;
            checkBothLoaded(loadedCallback);
        });
    }

    private void loadTerrainShape(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        String url = CommonUrl.terrainShapeController(planetId);

        fetchJson(url, json -> {
            try {
                this.nativeTerrainShape = convertToNativeTerrainShape(json);
                checkBothLoaded(loadedCallback);
            } catch (Throwable t) {
                JsConsole.error("Error parsing terrain shape: " + t.getMessage());
                failCallback.accept(t.getMessage());
            }
        }, error -> {
            JsConsole.error("Failed to load terrain shape: " + error);
            failCallback.accept(error);
        });
    }

    private void checkBothLoaded(Consumer<NativeTerrainShape> loadedCallback) {
        if (nativeTerrainShape != null && heightsLoaded) {
            loadedCallback.accept(nativeTerrainShape);
        }
    }

    @Override
    public Uint16ArrayEmu createTileGroundHeightMap(Index terrainTileIndex) {
        int tileHeightMapStart = getTileHeightMapStart(terrainTileIndex);
        int nextXTileHeightMapStart = getTileHeightMapStart(terrainTileIndex.add(1, 0));
        int nextYTileHeightMapStart = getTileHeightMapStart(terrainTileIndex.add(0, 1));
        int nextXYTileHeightMapStart = getTileHeightMapStart(terrainTileIndex.add(1, 1));

        Uint16Array resultArray = createUint16Array((NODE_X_COUNT + 1) * (NODE_Y_COUNT + 1));

        for (int i = 0; i < NODE_Y_COUNT; i++) {
            int sourceYOffset = i * NODE_X_COUNT;
            int sourceHeightMapStart = tileHeightMapStart + sourceYOffset;
            int sourceHeightMapEnd = sourceHeightMapStart + NODE_X_COUNT;
            int destHeightMapStart = i * (NODE_X_COUNT + 1);

            try {
                ArrayBufferView slice = sliceFromStore(tileStore, flatValues, TILE_NODE_SIZE, sourceHeightMapStart, sourceHeightMapEnd);
                setUint16ArraySlice(resultArray, slice, destHeightMapStart);

                // Add from next X tile
                int sourceNextTileHeightMapStart;
                if (terrainTileIndex.getX() + 1 < terrainService.getTerrainShape().getTileXCount()) {
                    sourceNextTileHeightMapStart = nextXTileHeightMapStart + sourceYOffset;
                } else {
                    sourceNextTileHeightMapStart = sourceHeightMapEnd + 1;
                }
                ArrayBufferView sliceEast = sliceFromStore(tileStore, flatValues, TILE_NODE_SIZE, sourceNextTileHeightMapStart, sourceNextTileHeightMapStart + 1);
                setUint16ArraySlice(resultArray, sliceEast, destHeightMapStart + NODE_X_COUNT);

                // Add last north row
                if (i == NODE_Y_COUNT - 1) {
                    if (terrainTileIndex.getY() + 1 < terrainService.getTerrainShape().getTileYCount()) {
                        ArrayBufferView sliceNorth = sliceFromStore(tileStore, flatValues, TILE_NODE_SIZE, nextYTileHeightMapStart, nextYTileHeightMapStart + NODE_X_COUNT);
                        setUint16ArraySlice(resultArray, sliceNorth, destHeightMapStart + NODE_X_COUNT + 1);

                        if (terrainTileIndex.getX() + 1 < terrainService.getTerrainShape().getTileXCount()) {
                            sourceNextTileHeightMapStart = nextXYTileHeightMapStart;
                        } else {
                            sourceNextTileHeightMapStart = nextYTileHeightMapStart + NODE_X_COUNT + 1;
                        }
                        ArrayBufferView sliceNorthEast = sliceFromStore(tileStore, flatValues, TILE_NODE_SIZE, sourceNextTileHeightMapStart, sourceNextTileHeightMapStart + 1);
                        setUint16ArraySlice(resultArray, sliceNorthEast, destHeightMapStart + NODE_X_COUNT + 1 + NODE_X_COUNT);
                    } else {
                        setUint16ArraySlice(resultArray, slice, destHeightMapStart + NODE_X_COUNT + 1);

                        if (terrainTileIndex.getX() + 1 < terrainService.getTerrainShape().getTileXCount()) {
                            sourceNextTileHeightMapStart = nextXTileHeightMapStart + sourceYOffset;
                        } else {
                            sourceNextTileHeightMapStart = sourceHeightMapEnd;
                        }
                        ArrayBufferView sliceNorthEast = sliceFromStore(tileStore, flatValues, TILE_NODE_SIZE, sourceNextTileHeightMapStart, sourceNextTileHeightMapStart + 1);
                        setUint16ArraySlice(resultArray, sliceNorthEast, destHeightMapStart + NODE_X_COUNT + 1 + NODE_X_COUNT);
                    }
                }
            } catch (Throwable t) {
                JsConsole.error("createTileGroundHeightMap error: " + t.getMessage());
            }
        }
        return asUint16ArrayEmu(resultArray);
    }

    private int getTileHeightMapStart(Index terrainTileIndex) {
        return terrainTileIndex.getY() * (terrainService.getTerrainShape().getTileXCount() * TILE_NODE_SIZE)
                + terrainTileIndex.getX() * TILE_NODE_SIZE;
    }

    @Override
    public int getGroundHeightAt(int index) {
        return heightFromStore(tileStore, flatValues, TILE_NODE_SIZE, index);
    }

    // Native JavaScript helpers via @JSBody

    /** One slot per tile, all empty. A slot stays null until its tile has been loaded. */
    @JSBody(params = {"tiles"}, script = "return new Array(tiles).fill(null);")
    private static native JSObject createTileStore(int tiles);

    /**
     * One height, from its tile or from the flat table.
     *
     * <p>One crossing of the bridge, the same as the array read this replaces: the whole lookup
     * happens on the JavaScript side, because this is called once per node by the pathing and the
     * crossing costs more than the arithmetic.
     *
     * <p>An index outside the planet answers zero, which is what the array read did by falling off
     * its end - kept so that a bad index stays as harmless as it was.
     */
    @JSBody(params = {"store", "flat", "tileValues", "index"}, script =
            "if (index < 0) { return 0; }" +
            "var t = (index / tileValues) | 0;" +
            "if (t >= store.length) { return 0; }" +
            "var tile = store[t];" +
            "return tile ? tile[index - t * tileValues] : flat[t];")
    private static native int heightFromStore(JSObject store, Uint16Array flat, int tileValues, int index);

    /**
     * A run of heights, as the slice of the global array it used to be.
     *
     * <p>Every run this is asked for lies inside one tile: a row of a tile is contiguous, and the
     * callers ask for a row, the first row of the tile above, or a single value. The fast path
     * therefore slices one tile's array. The loop below it is the honest fallback for a run that
     * does cross a boundary - it cannot happen today, and if it ever does it must not silently
     * return the wrong tile's heights.
     *
     * <p>A tile that is not loaded answers with its flat height repeated, which is exact for the
     * seventy per cent of tiles that are flat.
     */
    @JSBody(params = {"store", "flat", "tileValues", "from", "to"}, script =
            "var length = to - from;" +
            "var t = (from / tileValues) | 0;" +
            "var offset = from - t * tileValues;" +
            "if (offset + length <= tileValues && t < store.length) {" +
            "  var tile = store[t];" +
            "  if (tile) { return tile.slice(offset, offset + length); }" +
            "  var flatRun = new Uint16Array(length);" +
            "  flatRun.fill(flat[t]);" +
            "  return flatRun;" +
            "}" +
            "var out = new Uint16Array(length);" +
            "for (var i = 0; i < length; i++) {" +
            "  var index = from + i;" +
            "  var ti = (index / tileValues) | 0;" +
            "  if (ti >= store.length) { out[i] = 0; continue; }" +
            "  var owner = store[ti];" +
            "  out[i] = owner ? owner[index - ti * tileValues] : flat[ti];" +
            "}" +
            "return out;")
    private static native ArrayBufferView sliceFromStore(JSObject store, Uint16Array flat, int tileValues, int from, int to);

    /**
     * Reads the flat table: {@code u16 tileXCount, u16 tileYCount}, a bitmask with the bit set for
     * a flat tile, then one u16 per tile. Only a flat tile's height means anything, so a tile that
     * is not flat keeps whatever the table says and is never read from here while it is loaded.
     *
     * <p>Throws if the table describes a different grid than the planet config does. That would
     * mean every tile offset lands somewhere else, and the result would be wrong terrain rather
     * than missing terrain - the one outcome worth refusing to start for.
     */
    @JSBody(params = {"buffer", "tileXCount", "tileYCount"}, script =
            "var head = new Uint16Array(buffer, 0, 2);" +
            "if (head[0] !== tileXCount || head[1] !== tileYCount) {" +
            "  throw new Error('flat table is ' + head[0] + 'x' + head[1] + ', planet is ' + tileXCount + 'x' + tileYCount);" +
            "}" +
            "var tiles = tileXCount * tileYCount;" +
            "var values = 4 + ((tiles + 7) >> 3);" +
            "return new Uint16Array(buffer.slice(values, values + tiles * 2));")
    private static native Uint16Array readFlatTable(ArrayBuffer buffer, int tileXCount, int tileYCount);

    /**
     * Unpacks a region into the store, one tile at a time.
     *
     * <p>Each value is the difference from what its three already-decoded neighbours predict -
     * left + above - corner, over the tile's own 160x160 grid, with neighbours outside the tile
     * read as zero. See HeightMapRegionService on the server, which subtracts exactly this. The
     * wrap at sixteen bits is the format, not a safety net: the server subtracted with the same
     * wrap.
     *
     * <p>The prediction never reaches outside the tile, so the tiles stay independent and a
     * rectangle is still a slice of the planet rather than a replay of everything before it.
     *
     * <p>The header's checksum is recomputed here from what came out and compared. It costs nothing
     * - it rides the loop that is already running - and it catches the failure that has no other
     * symptom: bytes written by one encoding and read by another. That is not hypothetical. On
     * 2026-09-14 a browser answered this very fetch out of its cache with the previous encoding,
     * because the entity tag covered the heights and the rectangle but not the format, and the
     * planet came out full of cliffs and canyons with nothing logged anywhere. Neither curl, which
     * has no cache, nor ctrl-F5, which does not reach a fetch made from inside a worker, could see
     * it.
     *
     * <p>Written in JavaScript for the same reason as the reads: this is 25,600 values per tile and
     * the bridge is not worth crossing for each one.
     */
    @JSBody(params = {"buffer", "store", "tileXCount", "tileValues", "tileRow"}, script =
            "var head = new Uint16Array(buffer, 0, 4);" +
            "var tileX = head[0], tileY = head[1], countX = head[2], countY = head[3];" +
            "var expected = new Int32Array(buffer, 8, 1)[0];" +
            "var data = new Uint16Array(buffer, 12);" +
            "var at = 0;" +
            "var checksum = 1;" +
            "for (var y = 0; y < countY; y++) {" +
            "  for (var x = 0; x < countX; x++) {" +
            "    var tile = new Uint16Array(tileValues);" +
            "    for (var row = 0; row < tileRow; row++) {" +
            "      var base = row * tileRow;" +
            "      for (var column = 0; column < tileRow; column++) {" +
            "        var i = base + column;" +
            "        var predicted;" +
            "        if (column === 0) {" +
            "          predicted = row > 0 ? tile[i - tileRow] : 0;" +
            "        } else if (row === 0) {" +
            "          predicted = tile[i - 1];" +
            "        } else {" +
            "          predicted = tile[i - 1] + tile[i - tileRow] - tile[i - tileRow - 1];" +
            "        }" +
            "        var height = (data[at + i] + predicted) & 0xFFFF;" +
            "        tile[i] = height;" +
            "        checksum = (Math.imul(checksum, 31) + height) | 0;" +
            "      }" +
            "    }" +
            "    at += tileValues;" +
            "    store[(tileY + y) * tileXCount + (tileX + x)] = tile;" +
            "  }" +
            "}" +
            "if (checksum !== expected) {" +
            "  throw new Error('height checksum ' + checksum + ', server said ' + expected" +
            "    + ' - these bytes were not written by the encoder that is reading them');" +
            "}")
    private static native void storeRegion(ArrayBuffer buffer, JSObject store, int tileXCount,
                                           int tileValues, int tileRow);

    @JSBody(params = {"length"}, script = "return new Uint16Array(length);")
    private static native Uint16Array createUint16Array(int length);

    @JSBody(script = "return new Uint16Array(0);")
    private static native Uint16Array createEmptyUint16Array();

    @JSBody(params = {"array", "start", "end"}, script = "return array.slice(start, end);")
    private static native ArrayBufferView sliceUint16Array(Uint16Array array, int start, int end);

    @JSBody(params = {"dest", "src", "offset"}, script = "dest.set(src, offset);")
    private static native void setUint16ArraySlice(Uint16Array dest, ArrayBufferView src, int offset);

    @JSBody(params = {"array", "index"}, script = "return array[index];")
    private static native int getUint16ArrayValue(Uint16Array array, int index);

    // Type conversion - convert from JSO interfaces to Java classes
    private static NativeTerrainShape convertToNativeTerrainShape(JSObject jsObj) {
        if (JsUtils.isNullOrUndefined(jsObj)) {
            return null;
        }

        JsNativeTerrainShape jsShape = castToTerrainShape(jsObj);
        NativeTerrainShape result = new NativeTerrainShape();

        int xLen = jsShape.getTilesXLength();

        if (xLen > 0 && xLen < 10000) { // Sanity check
            result.nativeTerrainShapeTiles = new NativeTerrainShapeTile[xLen][];
            for (int x = 0; x < xLen; x++) {
                int yLen = jsShape.getTilesYLength(x);
                if (yLen > 0 && yLen < 10000) { // Sanity check
                    result.nativeTerrainShapeTiles[x] = new NativeTerrainShapeTile[yLen];
                    for (int y = 0; y < yLen; y++) {
                        JsNativeTerrainShapeTile jsTile = jsShape.getTile(x, y);
                        result.nativeTerrainShapeTiles[x][y] = convertToNativeTerrainShapeTile(jsTile);
                    }
                }
            }
        }
        return result;
    }

    @JSBody(params = {"obj"}, script = "console.log('[TeaVM Debug] Object keys:', Object.keys(obj)); console.log('[TeaVM Debug] Object:', JSON.stringify(obj).substring(0, 500));")
    private static native void logObjectKeys(JSObject obj);

    private static NativeTerrainShapeTile convertToNativeTerrainShapeTile(JsNativeTerrainShapeTile jsTile) {
        if (isJsNullOrUndefined(jsTile)) {
            return null;
        }
        NativeTerrainShapeTile result = new NativeTerrainShapeTile();

        // Object lists - use direct JSBody accessors
        int objectListsLen = jsTile.getObjectListsLength();
        if (objectListsLen > 0) {
            result.nativeTerrainShapeObjectLists = new NativeTerrainShapeObjectList[objectListsLen];
            for (int i = 0; i < objectListsLen; i++) {
                JsNativeTerrainShapeObjectList jsList = jsTile.getObjectList(i);
                result.nativeTerrainShapeObjectLists[i] = convertToNativeTerrainShapeObjectList(jsList);
            }
        }

        // Babylon decals - use direct JSBody accessors
        int decalsLen = jsTile.getDecalsLength();
        if (decalsLen > 0) {
            result.nativeBabylonDecals = new NativeBabylonDecal[decalsLen];
            for (int i = 0; i < decalsLen; i++) {
                JsNativeBabylonDecal jsDecal = jsTile.getDecal(i);
                result.nativeBabylonDecals[i] = convertToNativeBabylonDecal(jsDecal);
            }
        }

        // Bot grounds - use direct JSBody accessors
        int botGroundsLen = jsTile.getBotGroundsLength();
        if (botGroundsLen > 0) {
            result.nativeBotGrounds = new NativeBotGround[botGroundsLen];
            for (int i = 0; i < botGroundsLen; i++) {
                JsNativeBotGround jsBotGround = jsTile.getBotGround(i);
                result.nativeBotGrounds[i] = convertToNativeBotGround(jsBotGround);
            }
        }

        return result;
    }

    private static NativeTerrainShapeObjectList convertToNativeTerrainShapeObjectList(JsNativeTerrainShapeObjectList jsList) {
        if (jsList == null || isJsNullOrUndefined(jsList)) {
            return null;
        }
        NativeTerrainShapeObjectList result = new NativeTerrainShapeObjectList();
        result.terrainObjectConfigId = jsList.getTerrainObjectConfigId();

        // Use direct JSBody accessors
        int positionsLen = jsList.getPositionsLength();
        if (positionsLen > 0) {
            result.terrainShapeObjectPositions = new NativeTerrainShapeObjectPosition[positionsLen];
            for (int i = 0; i < positionsLen; i++) {
                JsNativeTerrainShapeObjectPosition jsPos = jsList.getPosition(i);
                result.terrainShapeObjectPositions[i] = convertToNativeTerrainShapeObjectPosition(jsPos);
            }
        }
        return result;
    }

    private static NativeTerrainShapeObjectPosition convertToNativeTerrainShapeObjectPosition(JsNativeTerrainShapeObjectPosition jsPos) {
        if (jsPos == null || isJsNullOrUndefined(jsPos)) {
            return null;
        }
        NativeTerrainShapeObjectPosition result = new NativeTerrainShapeObjectPosition();
        result.terrainObjectId = jsPos.getTerrainObjectId();
        // Use safe getter to handle undefined/NaN values from JavaScript
        result.x = safeGetDoubleX(jsPos);
        result.y = safeGetDoubleY(jsPos);
        result.scale = convertToNativeVertex(jsPos.getScale());
        result.rotation = convertToNativeVertex(jsPos.getRotation());
        result.offset = convertToNativeVertex(jsPos.getOffset());
        return result;
    }

    private static NativeVertex convertToNativeVertex(JsNativeVertex jsVertex) {
        if (jsVertex == null || isJsNullOrUndefined(jsVertex)) {
            return null;
        }
        NativeVertex result = new NativeVertex();
        // Use safe getter to handle undefined/NaN values from JavaScript
        result.x = safeGetVertexX(jsVertex);
        result.y = safeGetVertexY(jsVertex);
        result.z = safeGetVertexZ(jsVertex);
        return result;
    }

    // JavaScript-side safe getters that return 0 for undefined/null/NaN
    @JSBody(params = {"obj"}, script =
            "var val = obj.x; if (val === undefined || val === null || Number.isNaN(val)) return 0; return val;")
    private static native double safeGetDoubleX(JSObject obj);

    @JSBody(params = {"obj"}, script =
            "var val = obj.y; if (val === undefined || val === null || Number.isNaN(val)) return 0; return val;")
    private static native double safeGetDoubleY(JSObject obj);

    @JSBody(params = {"obj"}, script =
            "var val = obj.x; if (val === undefined || val === null || Number.isNaN(val)) return 0; return val;")
    private static native double safeGetVertexX(JSObject obj);

    @JSBody(params = {"obj"}, script =
            "var val = obj.y; if (val === undefined || val === null || Number.isNaN(val)) return 0; return val;")
    private static native double safeGetVertexY(JSObject obj);

    @JSBody(params = {"obj"}, script =
            "var val = obj.z; if (val === undefined || val === null || Number.isNaN(val)) return 0; return val;")
    private static native double safeGetVertexZ(JSObject obj);

    @JSBody(params = {"obj"}, script =
            "var val = obj.xPos; if (val === undefined || val === null || Number.isNaN(val)) return 0; return val;")
    private static native double safeGetSlopeBoxXPos(JSObject obj);

    @JSBody(params = {"obj"}, script =
            "var val = obj.yPos; if (val === undefined || val === null || Number.isNaN(val)) return 0; return val;")
    private static native double safeGetSlopeBoxYPos(JSObject obj);

    @JSBody(params = {"obj"}, script =
            "var val = obj.height; if (val === undefined || val === null || Number.isNaN(val)) return 0; return val;")
    private static native double safeGetSlopeBoxHeight(JSObject obj);

    @JSBody(params = {"obj"}, script =
            "var val = obj.yRot; if (val === undefined || val === null || Number.isNaN(val)) return 0; return val;")
    private static native double safeGetSlopeBoxYRot(JSObject obj);

    @JSBody(params = {"obj"}, script =
            "var val = obj.zRot; if (val === undefined || val === null || Number.isNaN(val)) return 0; return val;")
    private static native double safeGetSlopeBoxZRot(JSObject obj);

    private static NativeBabylonDecal convertToNativeBabylonDecal(JsNativeBabylonDecal jsDecal) {
        if (jsDecal == null || isJsNullOrUndefined(jsDecal)) {
            return null;
        }
        NativeBabylonDecal result = new NativeBabylonDecal();
        result.babylonMaterialId = jsDecal.getBabylonMaterialId();
        // Use safe getters to handle undefined/NaN values from JavaScript
        result.xPos = safeGetDecalXPos(jsDecal);
        result.yPos = safeGetDecalYPos(jsDecal);
        result.xSize = safeGetDecalXSize(jsDecal);
        result.ySize = safeGetDecalYSize(jsDecal);
        return result;
    }

    // Safe getters for decal properties
    @JSBody(params = {"obj"}, script =
            "var val = obj.xPos; if (val === undefined || val === null || Number.isNaN(val)) return 0; return val;")
    private static native double safeGetDecalXPos(JSObject obj);

    @JSBody(params = {"obj"}, script =
            "var val = obj.yPos; if (val === undefined || val === null || Number.isNaN(val)) return 0; return val;")
    private static native double safeGetDecalYPos(JSObject obj);

    @JSBody(params = {"obj"}, script =
            "var val = obj.xSize; if (val === undefined || val === null || Number.isNaN(val)) return 0; return val;")
    private static native double safeGetDecalXSize(JSObject obj);

    @JSBody(params = {"obj"}, script =
            "var val = obj.ySize; if (val === undefined || val === null || Number.isNaN(val)) return 0; return val;")
    private static native double safeGetDecalYSize(JSObject obj);

    private static NativeBotGround convertToNativeBotGround(JsNativeBotGround jsBotGround) {
        if (jsBotGround == null || isJsNullOrUndefined(jsBotGround)) {
            return null;
        }
        NativeBotGround result = new NativeBotGround();
        result.height = jsBotGround.getHeight();

        // Positions - use direct JSBody accessors, filter out invalid positions
        int positionsLen = jsBotGround.getPositionsLength();
        if (positionsLen > 0) {
            List<NativeDecimalPosition> validPositions = new ArrayList<>();
            for (int i = 0; i < positionsLen; i++) {
                JsNativeDecimalPosition jsPos = jsBotGround.getPosition(i);
                NativeDecimalPosition pos = convertToNativeDecimalPosition(jsPos);
                if (pos != null) {
                    validPositions.add(pos);
                }
            }
            if (!validPositions.isEmpty()) {
                result.positions = validPositions.toArray(new NativeDecimalPosition[0]);
            }
        }

        // Slope boxes - use safe JSBody accessors to avoid TeaVM @JSProperty naming issues
        int slopeBoxesLen = jsBotGround.getSlopeBoxesLength();
        if (slopeBoxesLen > 0) {
            result.botGroundSlopeBoxes = new NativeBotGroundSlopeBox[slopeBoxesLen];
            for (int i = 0; i < slopeBoxesLen; i++) {
                JsNativeBotGroundSlopeBox jsBox = jsBotGround.getSlopeBox(i);
                result.botGroundSlopeBoxes[i] = convertToNativeBotGroundSlopeBox(jsBox);
            }
        }

        return result;
    }

    private static NativeDecimalPosition convertToNativeDecimalPosition(JsNativeDecimalPosition jsPos) {
        if (jsPos == null || isJsNullOrUndefined(jsPos)) {
            return null;
        }
        // Check for valid position data using JavaScript-side validation
        if (!jsPos.isValid()) {
            JsConsole.warn("convertToNativeDecimalPosition: invalid position data, skipping");
            return null;
        }
        NativeDecimalPosition result = new NativeDecimalPosition();
        // Use safe getter to handle undefined/NaN values from JavaScript
        result.x = safeGetDoubleX(jsPos);
        result.y = safeGetDoubleY(jsPos);
        return result;
    }

    private static NativeBotGroundSlopeBox convertToNativeBotGroundSlopeBox(JsNativeBotGroundSlopeBox jsBox) {
        if (jsBox == null || isJsNullOrUndefined(jsBox)) {
            return null;
        }
        NativeBotGroundSlopeBox result = new NativeBotGroundSlopeBox();
        // Use safe getter for xPos and yPos
        result.xPos = safeGetSlopeBoxXPos(jsBox);
        result.yPos = safeGetSlopeBoxYPos(jsBox);
        result.height = safeGetSlopeBoxHeight(jsBox);
        result.yRot = safeGetSlopeBoxYRot(jsBox);
        result.zRot = safeGetSlopeBoxZRot(jsBox);
        return result;
    }

    // WASM-GC compatible helper methods - use @JSBody instead of Java casts

    @JSBody(params = {"obj"}, script = "return obj;")
    private static native JsNativeTerrainShape castToTerrainShape(JSObject obj);

    @JSBody(params = {"obj"}, script = "return obj === null || obj === undefined;")
    private static native boolean isJsNullOrUndefined(JSObject obj);

    // Array helper methods
    @JSBody(params = {"array"}, script = "return array === null || array === undefined;")
    private static native boolean isArrayNullOrUndefined(JSObject array);

    @JSBody(params = {"array"}, script = "return array ? array.length : 0;")
    private static native int getArrayLength(JSObject array);

    private static Uint16ArrayEmu asUint16ArrayEmu(Uint16Array array) {
        return JsUint16ArrayWrapper.wrap(array);
    }

    // Fetch helpers with callbacks
    @JSBody(params = {"url", "successCallback", "errorCallback"}, script =
            "fetch(url)" +
            ".then(function(response) { " +
            "  if (!response.ok) throw new Error(response.status + ' ' + response.statusText);" +
            "  return response.json();" +
            "})" +
            ".then(function(json) { successCallback(json); })" +
            ".catch(function(error) { errorCallback(error.toString()); });")
    private static native void fetchJson(String url, JsonCallback successCallback, ErrorCallback errorCallback);

    @JSBody(params = {"url", "successCallback", "errorCallback"}, script =
            "fetch(url)" +
            ".then(function(response) { " +
            "  if (!response.ok) throw new Error(response.status + ' ' + response.statusText);" +
            "  return response.arrayBuffer();" +
            "})" +
            ".then(function(buffer) { successCallback(buffer); })" +
            ".catch(function(error) { errorCallback(error.toString()); });")
    private static native void fetchArrayBuffer(String url, ArrayBufferCallback successCallback, ErrorCallback errorCallback);

    @org.teavm.jso.JSFunctor
    public interface JsonCallback extends JSObject {
        void onSuccess(JSObject json);
    }

    @org.teavm.jso.JSFunctor
    public interface ArrayBufferCallback extends JSObject {
        void onSuccess(ArrayBuffer buffer);
    }

    @org.teavm.jso.JSFunctor
    public interface ErrorCallback extends JSObject {
        void onError(String error);
    }
}
