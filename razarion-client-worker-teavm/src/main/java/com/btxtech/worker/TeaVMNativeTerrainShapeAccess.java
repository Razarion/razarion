package com.btxtech.worker;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeDecimalPosition;
import com.btxtech.shared.gameengine.planet.terrain.HeightMapBlocks;
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
import org.teavm.jso.typedarrays.Uint8Array;

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
    /**
     * Rings of tiles around the start tile that have to be there before the game may start.
     *
     * <p><b>The invariant, and it is not obvious: what may be drawn is one ring smaller than what
     * is loaded.</b> {@link #createTileGroundHeightMap} reads one row into the tile to the east and
     * one into the tile to the north, so building the outermost loaded tile reaches into a tile
     * that is not there. Measured on the real map, ignoring that costs 1120 reads into unloaded
     * non-flat tiles during a start; respecting it costs none.
     *
     * <p>Two rings loaded therefore means one ring - three by three tiles, 480 by 480 metres -
     * may be built while the rest of the map is still arriving. The camera at a start sees a
     * fraction of that.
     *
     * <p>Cost on the measured planet: 140 kB for a start tile at the planet's corner, against
     * 2494 kB for the whole map.
     */
    private static final int START_RADIUS_TILES = 2;
    /** How long the rest of the map waits for the page to say the models are in. */
    private static final int REST_FALLBACK_MILLIS = 30_000;
    private final TerrainService terrainService;
    private NativeTerrainShape nativeTerrainShape;
    /**
     * One Uint16Array per tile, or null for a tile that has not been loaded. Indexed the way the
     * height map is laid out: {@code tileY * tileXCount + tileX}, tiles contiguous, rows contiguous
     * inside a tile.
     */
    private JSObject tileStore;
    /** One height per tile - what a flat tile has edge to edge. Only meaningful where flagged. */
    private Uint16Array flatValues;
    /**
     * One byte per tile, non-zero where the tile is flat.
     * <p>
     * Separate from the heights because the two answer different questions: the height says what
     * an unloaded tile reads as, the flag says whether that answer is the truth or a stand-in. A
     * height alone could not tell the difference, and the difference is the whole safety of
     * starting before the map is complete.
     */
    private Uint8Array flatTiles;
    private int tileXCount;
    private int tileYCount;
    /** Set once both the flat table and the first region have arrived. */
    private boolean heightsLoaded;
    /** The rest of the map, waiting for the game to be released - see {@link #checkBothLoaded}. */
    private Runnable restAfterRelease;
    private double restStartedAt;

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
    public void load(int planetId, int tileXCount, int tileYCount, Index startTile, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        nativeTerrainShape = null;
        this.tileXCount = tileXCount;
        this.tileYCount = tileYCount;
        this.heightsLoaded = false;
        this.restAfterRelease = null;
        this.flatValues = null;
        this.flatTiles = null;
        this.tileStore = createTileStore(tileXCount * tileYCount);

        loadTerrainShape(planetId, loadedCallback, failCallback);
        loadHeights(planetId, startTile, loadedCallback);
    }

    /**
     * The flat table first, then the heights - in that order, because a height read that arrives
     * before its tile does has to fall back on the table, and a table that is not there yet would
     * answer zero. Zero is sea level minus two hundred metres, which the pathing would read as
     * water.
     */
    private void loadHeights(int planetId, Index startTile, Consumer<NativeTerrainShape> loadedCallback) {
        fetchArrayBuffer(CommonUrl.terrainHeightMapFlatController(planetId), flatBuffer -> {
            try {
                flatValues = readFlatTable(flatBuffer, tileXCount, tileYCount);
                flatTiles = readFlatFlags(flatBuffer, tileXCount, tileYCount);
            } catch (Throwable t) {
                JsConsole.warn("Error reading the flat height table: " + t.getMessage());
                flatValues = createUint16Array(tileXCount * tileYCount);
                flatTiles = createUint8Array(tileXCount * tileYCount);
            }
            loadStartRegion(planetId, startTile, loadedCallback);
        }, error -> {
            JsConsole.warn("Failed to load the flat height table: " + error);
            flatValues = createUint16Array(tileXCount * tileYCount);
            flatTiles = createUint8Array(tileXCount * tileYCount);
            loadStartRegion(planetId, startTile, loadedCallback);
        });
    }

    /**
     * The part of the map the game may not start without, and then the rest behind it.
     *
     * <p>Without a start tile it is every block of the map that is not flat (see {@link #loadRest}),
     * all of it before the game starts - what a warm start, a player with a base and the editor get.
     *
     * <p>With one it is {@link #START_RADIUS_TILES} rings around it - five by five tiles, 800 by
     * 800 metres, measured at 302 kB against 2.44 MB for the planet. Two rings rather than one
     * because the camera can be moved while the rest is still coming, and because building a
     * terrain tile reads one row into each neighbour.
     *
     * <p>The rest follows once the game runs and its models are in (see {@link #checkBothLoaded}),
     * block by block (see {@link #loadRest}). Until then the far tiles answer stand-in heights,
     * counted in {@code RAZ_heightMisses} on the worker's global object - see
     * {@link #heightFromStore} - and what was kept from them is redone when the map is complete.
     */
    private void loadStartRegion(int planetId, Index startTile, Consumer<NativeTerrainShape> loadedCallback) {
        if (startTile == null) {
            // The same blocks the rest is fetched in, before the game starts instead of after.
            loadBlocks(planetId, contentBlocks(), 0, false, () -> {
                heightsLoaded = true;
                checkBothLoaded(loadedCallback);
            });
            return;
        }
        int fromX = Math.max(0, startTile.getX() - START_RADIUS_TILES);
        int fromY = Math.max(0, startTile.getY() - START_RADIUS_TILES);
        int toX = Math.min(tileXCount, startTile.getX() + START_RADIUS_TILES + 1);
        int toY = Math.min(tileYCount, startTile.getY() + START_RADIUS_TILES + 1);
        terrainService.onHeightMapPartial();
        restAfterRelease = () -> loadRest(planetId);
        loadRegion(planetId, fromX, fromY, toX - fromX, toY - fromY, loadedCallback, null);
    }

    /**
     * The rest of the map, in the blocks {@link HeightMapBlocks} names - the same for everybody,
     * warmed by the server when it starts - one after another, so only one block's buffer is alive
     * at a time. The start region comes again with them; that is about 100 kB.
     */
    private void loadRest(int planetId) {
        restStartedAt = now();
        loadBlocks(planetId, contentBlocks(), 0, true, this::restComplete);
    }

    private void loadBlocks(int planetId, List<int[]> blocks, int index, boolean background, Runnable done) {
        if (index >= blocks.size()) {
            done.run();
            return;
        }
        int[] block = blocks.get(index);
        loadRegion(planetId, block[0], block[1], block[2], block[3], null,
                () -> loadBlocks(planetId, blocks, index + 1, background, done), background);
    }

    /** Every block when the flat table is missing: then no tile is known to be flat. */
    private List<int[]> contentBlocks() {
        Uint8Array flags = flatTiles;
        return HeightMapBlocks.contentBlocks(tileXCount, tileYCount, tile -> flags != null && flatFlag(flags, tile) != 0);
    }

    @JSBody(params = {"flatTiles", "index"}, script = "return flatTiles[index];")
    private static native int flatFlag(Uint8Array flatTiles, int index);

    /**
     * The whole map is in. Reported as a warning so it reaches the server log: how long the far
     * tiles took, and how many height reads had to be answered from stand-ins meanwhile. Then the
     * engine redoes what it kept from those reads - see TerrainService#onHeightMapComplete.
     */
    private void restComplete() {
        JsConsole.warn("[HeightGate] rest of the height map in " + Math.round(now() - restStartedAt)
                + " ms, stand-in height reads " + heightMisses());
        try {
            terrainService.onHeightMapComplete();
        } catch (Throwable t) {
            JsConsole.error("[HeightGate] completing the height map failed: " + t.getMessage());
        }
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
                            Consumer<NativeTerrainShape> loadedCallback, Runnable then) {
        loadRegion(planetId, tileX, tileY, countX, countY, loadedCallback, then, false);
    }

    /**
     * @param background the rest of the map behind a running game: asked for at low priority, so
     *                   the models the player is waiting to see go first on a shared connection.
     */
    private void loadRegion(int planetId, int tileX, int tileY, int countX, int countY,
                            Consumer<NativeTerrainShape> loadedCallback, Runnable then, boolean background) {
        fetchArrayBuffer(CommonUrl.terrainHeightMapRegionController(planetId, tileX, tileY, countX, countY), background, buffer -> {
            try {
                storeRegion(buffer, tileStore, flatTiles, tileXCount, TILE_NODE_SIZE, NODE_X_COUNT);
            } catch (Throwable t) {
                // An error rather than a warning, and said out loud: this is the one failure that is
                // otherwise invisible. Wrong heights draw a planet of the wrong shape and nothing
                // complains - no exception, no missing tile, just cliffs where there is meadow.
                JsConsole.error("Height map region rejected: " + t.getMessage());
            }
            regionArrived(loadedCallback, then);
        }, error -> {
            JsConsole.warn("Failed to load a height map region: " + error);
            regionArrived(loadedCallback, then);
        });
    }

    /** A region is in. If the game was waiting for this one, let it go; then whatever follows. */
    private void regionArrived(Consumer<NativeTerrainShape> loadedCallback, Runnable then) {
        if (loadedCallback != null) {
            heightsLoaded = true;
            checkBothLoaded(loadedCallback);
        }
        if (then != null) {
            then.run();
        }
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

    /**
     * Releases the game once the shape and the first heights are in. The rest of the map waits
     * longer still, for the models: both are asked for the moment the game is released, and on a
     * shared connection the rest's 2 MB came in first and held the builder and the placer's model
     * back by the same amount (phone, 5 Mbit/s, 2026-10-16). The page says when the models are in;
     * {@link #REST_FALLBACK_MILLIS} is for a page that never does.
     */
    private void checkBothLoaded(Consumer<NativeTerrainShape> loadedCallback) {
        if (nativeTerrainShape != null && heightsLoaded) {
            loadedCallback.accept(nativeTerrainShape);
            Runnable rest = restAfterRelease;
            restAfterRelease = null;
            if (rest != null) {
                boolean[] started = new boolean[1];
                RestGo go = () -> {
                    if (!started[0]) {
                        started[0] = true;
                        rest.run();
                    }
                };
                armRestGo(go, REST_FALLBACK_MILLIS);
            }
        }
    }

    @Override
    public boolean isTileDrawable(Index terrainTileIndex) {
        return tileRingLoaded(tileStore, flatTiles, tileXCount, tileYCount, terrainTileIndex.getX(), terrainTileIndex.getY());
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
                ArrayBufferView slice = sliceFromStore(tileStore, flatValues, flatTiles, TILE_NODE_SIZE, sourceHeightMapStart, sourceHeightMapEnd);
                setUint16ArraySlice(resultArray, slice, destHeightMapStart);

                // Add from next X tile
                int sourceNextTileHeightMapStart;
                if (terrainTileIndex.getX() + 1 < terrainService.getTerrainShape().getTileXCount()) {
                    sourceNextTileHeightMapStart = nextXTileHeightMapStart + sourceYOffset;
                } else {
                    sourceNextTileHeightMapStart = sourceHeightMapEnd + 1;
                }
                ArrayBufferView sliceEast = sliceFromStore(tileStore, flatValues, flatTiles, TILE_NODE_SIZE, sourceNextTileHeightMapStart, sourceNextTileHeightMapStart + 1);
                setUint16ArraySlice(resultArray, sliceEast, destHeightMapStart + NODE_X_COUNT);

                // Add last north row
                if (i == NODE_Y_COUNT - 1) {
                    if (terrainTileIndex.getY() + 1 < terrainService.getTerrainShape().getTileYCount()) {
                        ArrayBufferView sliceNorth = sliceFromStore(tileStore, flatValues, flatTiles, TILE_NODE_SIZE, nextYTileHeightMapStart, nextYTileHeightMapStart + NODE_X_COUNT);
                        setUint16ArraySlice(resultArray, sliceNorth, destHeightMapStart + NODE_X_COUNT + 1);

                        if (terrainTileIndex.getX() + 1 < terrainService.getTerrainShape().getTileXCount()) {
                            sourceNextTileHeightMapStart = nextXYTileHeightMapStart;
                        } else {
                            sourceNextTileHeightMapStart = nextYTileHeightMapStart + NODE_X_COUNT + 1;
                        }
                        ArrayBufferView sliceNorthEast = sliceFromStore(tileStore, flatValues, flatTiles, TILE_NODE_SIZE, sourceNextTileHeightMapStart, sourceNextTileHeightMapStart + 1);
                        setUint16ArraySlice(resultArray, sliceNorthEast, destHeightMapStart + NODE_X_COUNT + 1 + NODE_X_COUNT);
                    } else {
                        setUint16ArraySlice(resultArray, slice, destHeightMapStart + NODE_X_COUNT + 1);

                        if (terrainTileIndex.getX() + 1 < terrainService.getTerrainShape().getTileXCount()) {
                            sourceNextTileHeightMapStart = nextXTileHeightMapStart + sourceYOffset;
                        } else {
                            sourceNextTileHeightMapStart = sourceHeightMapEnd;
                        }
                        ArrayBufferView sliceNorthEast = sliceFromStore(tileStore, flatValues, flatTiles, TILE_NODE_SIZE, sourceNextTileHeightMapStart, sourceNextTileHeightMapStart + 1);
                        setUint16ArraySlice(resultArray, sliceNorthEast, destHeightMapStart + NODE_X_COUNT + 1 + NODE_X_COUNT);
                    }
                }
            } catch (Throwable t) {
                JsConsole.error("createTileGroundHeightMap error: " + t.getMessage());
            }
        }
        return asUint16ArrayEmu(resultArray);
    }

    @Override
    public Uint16ArrayEmu createTileGroundRelief(Index terrainTileIndex) {
        try {
            return asUint16ArrayEmu(computeRelief(tileStore, flatValues, TILE_NODE_SIZE, NODE_X_COUNT,
                    tileXCount, tileYCount, terrainTileIndex.getX(), terrainTileIndex.getY()));
        } catch (Throwable t) {
            // Only the look suffers: the renderer draws the tile without relief.
            JsConsole.warn("createTileGroundRelief failed: " + t.getMessage());
            return null;
        }
    }

    private int getTileHeightMapStart(Index terrainTileIndex) {
        return terrainTileIndex.getY() * (terrainService.getTerrainShape().getTileXCount() * TILE_NODE_SIZE)
                + terrainTileIndex.getX() * TILE_NODE_SIZE;
    }

    @Override
    public int getGroundHeightAt(int index) {
        return heightFromStore(tileStore, flatValues, flatTiles, TILE_NODE_SIZE, index);
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
     *
     * <p>A read into a tile that is neither loaded nor flat is the one answer this cannot give
     * correctly, and it is counted rather than hidden: {@code self.RAZ_heightMisses} in the worker.
     * It happens while the rest of the map is still arriving - the engine works out the positions of
     * units far away on every tick - and the answer meanwhile is the tile's table entry. What is
     * kept from such a read is redone once the map is complete (TerrainService#onHeightMapComplete);
     * the count goes to the server log with the [HeightGate] line.
     */
    @JSBody(params = {"store", "flat", "flatTiles", "tileValues", "index"}, script =
            "if (index < 0) { return 0; }" +
            "var t = (index / tileValues) | 0;" +
            "if (t >= store.length) { return 0; }" +
            "var tile = store[t];" +
            "if (tile) { return tile[index - t * tileValues]; }" +
            "if (flatTiles[t]) { return flat[t]; }" +
            "self.RAZ_heightMisses = (self.RAZ_heightMisses || 0) + 1;" +
            "return flat[t];")
    private static native int heightFromStore(JSObject store, Uint16Array flat, Uint8Array flatTiles, int tileValues, int index);

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
    @JSBody(params = {"store", "flat", "flatTiles", "tileValues", "from", "to"}, script =
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
    private static native ArrayBufferView sliceFromStore(JSObject store, Uint16Array flat, Uint8Array flatTiles, int tileValues, int from, int to);

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
    @JSBody(params = {"buffer", "store", "flatTiles", "tileXCount", "tileValues", "tileRow"}, script =
            "var head = new Uint16Array(buffer, 0, 4);" +
            "var tileX = head[0], tileY = head[1], countX = head[2], countY = head[3];" +
            "var expected = new Int32Array(buffer, 8, 1)[0];" +
            "var data = new Uint16Array(buffer, 12);" +
            "var at = 0;" +
            "var checksum = 1;" +
            "var scratch = new Uint16Array(tileValues);" +
            "for (var y = 0; y < countY; y++) {" +
            "  for (var x = 0; x < countX; x++) {" +
            "    var tile = scratch;" +
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
            "    var t = (tileY + y) * tileXCount + (tileX + x);" +
            "    if (!(flatTiles && flatTiles[t])) { store[t] = tile.slice(); }" +
            "  }" +
            "}" +
            "if (checksum !== expected) {" +
            "  throw new Error('height checksum ' + checksum + ', server said ' + expected" +
            "    + ' - these bytes were not written by the encoder that is reading them');" +
            "}")
    private static native void storeRegion(ArrayBuffer buffer, JSObject store, Uint8Array flatTiles, int tileXCount,
                                           int tileValues, int tileRow);

    /**
     * Sky visibility and curvature for every node of one tile, (n + 1) x (n + 1) like the tile's
     * height map. Packed per node: high byte = sky visibility (255 = open sky), low byte =
     * curvature (128 = flat, below = hollow, above = crest), see
     * NativeTerrainShapeAccess.createTileGroundRelief().
     *
     * <p>Sky visibility: in 8 directions the steepest rise of the ground within 24 m, as the sine
     * of its horizon angle; their mean is what the surroundings hide of the sky. Curvature: the
     * node against the mean of a ring 10 m around it, +-3 m to the full byte.
     *
     * <p>All in one call on the JavaScript side - 24 height reads per node, ~620,000 per tile,
     * would cost far more as bridge crossings than as arithmetic. Reads across tile edges come
     * from the neighbours (or from the flat table), so adjacent tiles agree at their seam.
     */
    @JSBody(params = {"store", "flat", "tileValues", "n", "tileXCount", "tileYCount", "tileX", "tileY"}, script =
            "var maxX = tileXCount * n - 1, maxY = tileYCount * n - 1;" +
            "function h(x, y) {" +
            "  x = x < 0 ? 0 : (x > maxX ? maxX : x); y = y < 0 ? 0 : (y > maxY ? maxY : y);" +
            "  var tx = (x / n) | 0, ty = (y / n) | 0, t = ty * tileXCount + tx, tile = store[t];" +
            "  return (tile ? tile[(y - ty * n) * n + (x - tx * n)] : flat[t]) * 0.01 - 200;" +
            "}" +
            "var DX = [1, 0.7071, 0, -0.7071, -1, -0.7071, 0, 0.7071];" +
            "var DY = [0, 0.7071, 1, 0.7071, 0, -0.7071, -1, -0.7071];" +
            "var RADII = [3, 8, 24];" +
            "var size = n + 1, out = new Uint16Array(size * size);" +
            "var ox = tileX * n, oy = tileY * n;" +
            "for (var y = 0; y < size; y++) {" +
            "  for (var x = 0; x < size; x++) {" +
            "    var gx = ox + x, gy = oy + y, h0 = h(gx, gy), hidden = 0, ring = 0;" +
            "    for (var d = 0; d < 8; d++) {" +
            "      var steepest = 0;" +
            "      for (var r = 0; r < 3; r++) {" +
            "        var dist = RADII[r];" +
            "        var dh = h(Math.round(gx + DX[d] * dist), Math.round(gy + DY[d] * dist)) - h0;" +
            "        if (r === 1) { ring += dh; }" +
            "        var slope = dh / dist;" +
            "        if (slope > steepest) { steepest = slope; }" +
            "      }" +
            "      hidden += steepest / Math.sqrt(1 + steepest * steepest);" +
            "    }" +
            "    var sky = 1 - hidden / 8;" +
            "    var curvature = -ring / 8 / 3;" +
            "    curvature = curvature < -1 ? -1 : (curvature > 1 ? 1 : curvature);" +
            "    out[y * size + x] = (Math.round(sky * 255) << 8) | Math.round((curvature + 1) * 127.5);" +
            "  }" +
            "}" +
            "return out;")
    private static native Uint16Array computeRelief(JSObject store, Uint16Array flat, int tileValues, int n,
                                                    int tileXCount, int tileYCount, int tileX, int tileY);

    /**
     * The bitmask of the flat table, unpacked to a byte per tile.
     * <p>
     * A byte rather than the bits it came as, because this is read once per height lookup and a
     * shift and a mask per read is not worth saving 1 kB of memory once.
     */
    @JSBody(params = {"buffer", "tileXCount", "tileYCount"}, script =
            "var tiles = tileXCount * tileYCount;" +
            "var bits = new Uint8Array(buffer, 4, (tiles + 7) >> 3);" +
            "var out = new Uint8Array(tiles);" +
            "for (var i = 0; i < tiles; i++) {" +
            "  out[i] = (bits[i >> 3] & (1 << (i % 8))) ? 1 : 0;" +
            "}" +
            "return out;")
    private static native Uint8Array readFlatFlags(ArrayBuffer buffer, int tileXCount, int tileYCount);

    /** All zero, which reads as "no tile is flat" - every unloaded tile then counts as a miss. */
    @JSBody(params = {"length"}, script = "return new Uint8Array(length);")
    private static native Uint8Array createUint8Array(int length);

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

    private static void fetchArrayBuffer(String url, ArrayBufferCallback successCallback, ErrorCallback errorCallback) {
        fetchArrayBuffer(url, false, successCallback, errorCallback);
    }

    /** {@code priority} is a hint; a browser without it ignores the option. */
    @JSBody(params = {"url", "low", "successCallback", "errorCallback"}, script =
            "fetch(url, low ? {priority: 'low'} : undefined)" +
            ".then(function(response) { " +
            "  if (!response.ok) throw new Error(response.status + ' ' + response.statusText);" +
            "  return response.arrayBuffer();" +
            "})" +
            ".then(function(buffer) { successCallback(buffer); })" +
            ".catch(function(error) { errorCallback(error.toString()); });")
    private static native void fetchArrayBuffer(String url, boolean low, ArrayBufferCallback successCallback, ErrorCallback errorCallback);

    @JSBody(params = {"go", "fallbackMillis"}, script = "self.RAZ_heightRestGo = function () { go(); };"
            + "if (self.RAZ_heightRestGoEarly) { go(); }"
            + "setTimeout(function () { go(); }, fallbackMillis);")
    private static native void armRestGo(RestGo go, int fallbackMillis);

    @org.teavm.jso.JSFunctor
    public interface RestGo extends JSObject {
        void go();
    }

    @JSBody(script = "return self.RAZ_heightMisses || 0;")
    private static native int heightMisses();

    @JSBody(script = "return performance.now();")
    private static native double now();

    /**
     * Whether a tile and the eight around it are loaded or flat - what building it reads. Off the
     * planet counts as there: the builder clamps at the edge.
     */
    @JSBody(params = {"store", "flatTiles", "tileXCount", "tileYCount", "x", "y"}, script =
            "for (var dy = -1; dy <= 1; dy++) {" +
            "  for (var dx = -1; dx <= 1; dx++) {" +
            "    var tx = x + dx, ty = y + dy;" +
            "    if (tx < 0 || ty < 0 || tx >= tileXCount || ty >= tileYCount) { continue; }" +
            "    var t = ty * tileXCount + tx;" +
            "    if (!store[t] && !(flatTiles && flatTiles[t])) { return false; }" +
            "  }" +
            "}" +
            "return true;")
    private static native boolean tileRingLoaded(JSObject store, Uint8Array flatTiles, int tileXCount, int tileYCount, int x, int y);

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
