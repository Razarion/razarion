package com.btxtech.server.service.engine;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * The height map by the tile, so a game can start on the corner the player is in.
 *
 * <p>A start waits for the whole planet today: 26,214,400 heights, 32 by 32 tiles of 160 by 160
 * nodes, 2.44 MB delta encoded. Measured on planet 117, almost none of it is needed to begin:
 *
 * <pre>
 *   whole map                          2.44 MB
 *   tiles 0..4 / 0..4  (800 x 800 m)    302 kB
 *   tiles 0..2 / 0..2  (480 x 480 m)    111 kB
 *   median tile                          0.1 kB
 *   721 of 1024 tiles are flat - one height, edge to edge
 * </pre>
 *
 * <p>The whole content of that planet sits in tiles x 0..11, y 0..12. The rest is filler, and the
 * filler is not what costs: compression already deals with it. What costs is that a player waits
 * for the far side of a world before seeing their own base.
 *
 * <h3>Two responses</h3>
 *
 * <p><b>The flat table</b> is every tile's constant height, about 2 kB for the planet. It exists so
 * that a client which has not yet loaded a tile can still answer a height read <em>exactly</em>
 * rather than guess - and for 70% of tiles an exact answer is one number. This matters more than it
 * sounds: the server runs the planet on the full map, and a client that answered a height
 * differently would read a different terrain type, walk a different path, and drift out of sync.
 * A guess is worse than a wait.
 *
 * <p><b>A region</b> is a rectangle of tiles, each delta encoded on its own. Per tile rather than
 * across the whole map on purpose: a tile has to be decodable without the tiles before it. It costs
 * nothing - 2.43 MB against 2.44 for the whole planet - and it is what makes a rectangle a slice
 * instead of a replay.
 *
 * <p>Both are little endian throughout, which is what a browser's Uint16Array reads without
 * conversion.
 */
@Service
public class HeightMapRegionService {
    /** Values per tile: 160 x 160, laid out row-major within the tile. */
    private static final int TILE_VALUES = TerrainUtil.TILE_NODE_SIZE;
    /** Values along one edge of a tile. The prediction in {@link #getRegion} needs the grid, not
     *  only its area, and the two counts are equal by the planet's definition. */
    private static final int TILE_NODE_ROW = TerrainUtil.NODE_X_COUNT;
    /** {@code u16 tileX, tileY, countX, countY} and then the checksum - see {@link #getRegion}. */
    private static final int HEADER_BYTES = 12;
    /**
     * How many finished region responses are kept. The whole-planet rectangle is what every client
     * asks for, so one would do; four leaves room for a second planet and for whatever the editor
     * and the studio ask for without evicting the one that matters on every start.
     */
    private static final int REGION_CACHE_MAX = 4;
    private final Logger logger = LoggerFactory.getLogger(HeightMapRegionService.class);
    private final PlanetCrudService planetCrudService;
    /**
     * Digest of the stored map -> the decompressed map and its tile geometry.
     *
     * <p>50 MB per planet, and worth it: the alternative is decompressing 50 MB on every region
     * request, which is a second and a half for a response that is otherwise twenty milliseconds.
     * Keyed by the digest, so a re-uploaded map replaces it by itself. The game engine on this same
     * server already holds the same heights as an int[], which is twice this.
     */
    private final ConcurrentHashMap<String, Map> maps = new ConcurrentHashMap<>();
    /**
     * Finished region responses, keyed by the digest and the rectangle.
     *
     * <p>The comment this replaces said a region is twenty milliseconds of work and that there are a
     * hundred thousand rectangles a client could ask for, so caching them was not worth it. Both
     * halves were wrong about the case that matters. Every client asks for one rectangle - the whole
     * planet - and for that one the work is not twenty milliseconds but <b>5.7 seconds</b>, almost
     * all of it gzipping 52 MB at maximum compression. Measured on planet 117 on 2026-09-14, after
     * it had been live since the morning: every single start was paying it.
     *
     * <p>Bounded, because the reasoning about a hundred thousand rectangles was right about the
     * risk even while being wrong about the cost. Entries are a couple of megabytes each and the
     * common case needs exactly one of them.
     */
    private final java.util.Map<String, byte[]> regions = java.util.Collections.synchronizedMap(
            new java.util.LinkedHashMap<>(REGION_CACHE_MAX + 1, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(java.util.Map.Entry<String, byte[]> eldest) {
                    return size() > REGION_CACHE_MAX;
                }
            });

    public HeightMapRegionService(PlanetCrudService planetCrudService) {
        this.planetCrudService = planetCrudService;
    }

    /**
     * Encodes the whole-planet rectangle before anybody asks for it.
     *
     * <p>Without this the first player after every restart pays for it: 50 MB decompressed and
     * 52 MB gzipped at maximum compression, six seconds measured on planet 117, in the middle of
     * their start. One unlucky player per deploy is a small number and a bad one - it lands on
     * somebody who has just arrived, which is where the funnel is thinnest.
     *
     * <p>On a thread of its own, so a server that is ready says so and starts serving. A request
     * that arrives during the warm-up computes the same bytes a second time rather than waiting,
     * which is wasteful for one request and simpler than a lock that would have to be right.
     *
     * <p>The whole planet is the only rectangle asked for today - see {@link #regions}. When the
     * client starts asking for the player's corner instead, this should warm that corner.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        Thread thread = new Thread(this::warmUpRegions, "height-map-warm-up");
        thread.setDaemon(true);
        thread.start();
    }

    private void warmUpRegions() {
        for (PlanetConfig planetConfig : planetCrudService.read()) {
            try {
                String digest = planetCrudService.getCompressedHeightMapDigest(planetConfig.getId());
                if (digest == null) {
                    continue;
                }
                Map map = map(planetConfig.getId(), digest);
                getFlatTable(planetConfig.getId(), digest);
                getRegion(planetConfig.getId(), digest, 0, 0, map.tileXCount, map.tileYCount);
            } catch (Throwable t) {
                // One planet that cannot be warmed must not stop the others, and none of this is
                // worth failing a start over: the request path computes it on demand as before.
                logger.warn("Could not warm the height map of planet {}: {}",
                        planetConfig.getId(), t.getMessage());
            }
        }
    }

    /**
     * Every tile's constant height, and which tiles have one.
     *
     * <p>Layout: {@code u16 tileXCount, u16 tileYCount, bitmask of ceil(n/8) bytes with the bit set
     * for a flat tile, u16[n] heights} - the height of a tile that is not flat is written as zero
     * and means nothing. Tiles are in row-major order, y outer.
     */
    public byte[] getFlatTable(int planetId, String digest) {
        Map map = map(planetId, digest);
        int tiles = map.tileXCount * map.tileYCount;
        byte[] out = new byte[4 + (tiles + 7) / 8 + tiles * 2];
        writeU16(out, 0, map.tileXCount);
        writeU16(out, 2, map.tileYCount);
        int bitmask = 4;
        int values = bitmask + (tiles + 7) / 8;
        for (int tile = 0; tile < tiles; tile++) {
            int start = tile * TILE_VALUES;
            int first = map.heights[start] & 0xFFFF;
            boolean flat = true;
            for (int i = 1; i < TILE_VALUES; i++) {
                if ((map.heights[start + i] & 0xFFFF) != first) {
                    flat = false;
                    break;
                }
            }
            if (flat) {
                out[bitmask + tile / 8] |= (byte) (1 << (tile % 8));
                writeU16(out, values + tile * 2, first);
            }
        }
        return gzip(out);
    }

    /**
     * The heights of a rectangle of tiles, each one encoded against a prediction from its own
     * neighbours.
     *
     * <p>Layout: {@code u16 tileX, u16 tileY, u16 countX, u16 countY, u32 checksum}, then
     * countX * countY blocks of {@link #TILE_VALUES} little endian residuals, the tiles in
     * row-major order, y outer. The header repeats what was asked for so that a response can be
     * read without its request.
     *
     * <p><b>The checksum</b> is {@code h = h * 31 + height} over the heights in the order they are
     * written, starting at one. The client recomputes it from what it decoded and says so if the two
     * differ. That exists because the failure it catches is silent and expensive: a client that
     * decodes these bytes differently than they were written gets a planet of the wrong shape and no
     * error anywhere. On 2026-09-14 that happened for real - a browser answered the worker's fetch
     * out of its cache with bytes in the previous encoding, because the entity tag covered the
     * heights and the rectangle but not the format. The tag carries the format now, and this is the
     * belt to that pair of braces.
     *
     * <p><b>The prediction</b> is left + above - corner, taken over the tile's own 160x160 grid.
     * Terrain is locally a plane, and three corners of a square fix the fourth exactly: on a slope,
     * however steep, the residual is zero. The predecessor subtracted the previous value in reading
     * order, which is the same thing along a row and a jump right across the tile at every row end -
     * 160 of them per tile, each as large as the terrain is wide.
     *
     * <p>Measured over the whole planet, 26.2 million values: 2,539,040 bytes gzipped before,
     * 2,112,058 after. The same data with brotli instead of gzip would be 1,719,658, which is the
     * next thing worth doing and a change to the transport rather than to this format.
     *
     * <p>Every tile still stands alone - the prediction never reaches outside it - so a rectangle is
     * still a slice of the planet rather than a replay of everything before it. Out-of-tile
     * neighbours read as zero, which makes the first row and column encode against nothing and
     * costs a few hundred bytes per tile; the alternative is a tile that cannot be read on its own.
     *
     * <p>Cached by digest and rectangle - see {@link #regions}. The response is a pure function of
     * those two, which is also what the entity tag says, so anything else would be two answers to
     * one question.
     */
    public byte[] getRegion(int planetId, String digest, int tileX, int tileY, int countX, int countY) {
        String key = digest + "-" + tileX + "-" + tileY + "-" + countX + "-" + countY;
        byte[] cached = regions.get(key);
        if (cached != null) {
            return cached;
        }
        byte[] encoded = encodeRegion(planetId, digest, tileX, tileY, countX, countY);
        regions.put(key, encoded);
        return encoded;
    }

    private byte[] encodeRegion(int planetId, String digest, int tileX, int tileY, int countX, int countY) {
        long started = System.currentTimeMillis();
        Map map = map(planetId, digest);
        if (tileX < 0 || tileY < 0 || countX <= 0 || countY <= 0
                || tileX + countX > map.tileXCount || tileY + countY > map.tileYCount) {
            throw new IllegalArgumentException("Region " + tileX + "/" + tileY + " " + countX + "x" + countY
                    + " is not inside the planet's " + map.tileXCount + "x" + map.tileYCount + " tiles");
        }
        byte[] out = new byte[HEADER_BYTES + countX * countY * TILE_VALUES * 2];
        writeU16(out, 0, tileX);
        writeU16(out, 2, tileY);
        writeU16(out, 4, countX);
        writeU16(out, 6, countY);
        int at = HEADER_BYTES;
        int checksum = 1;
        for (int y = tileY; y < tileY + countY; y++) {
            for (int x = tileX; x < tileX + countX; x++) {
                int start = (y * map.tileXCount + x) * TILE_VALUES;
                for (int row = 0; row < TILE_NODE_ROW; row++) {
                    for (int column = 0; column < TILE_NODE_ROW; column++) {
                        int i = row * TILE_NODE_ROW + column;
                        int value = map.heights[start + i] & 0xFFFF;
                        checksum = checksum * 31 + value;
                        writeU16(out, at, (value - predict(map.heights, start, row, column)) & 0xFFFF);
                        at += 2;
                    }
                }
            }
        }
        writeU32(out, 8, checksum);
        byte[] compressed = gzip(out);
        logger.info("Height map region {}/{} {}x{} of planet {} encoded: {} bytes, {} ms",
                tileX, tileY, countX, countY, planetId, compressed.length,
                System.currentTimeMillis() - started);
        return compressed;
    }

    /**
     * What the value at this spot should be, judged from the three neighbours already written.
     * Zero outside the tile, so the top row predicts from the left alone, the left column from
     * above alone, and the very first value from nothing.
     */
    private static int predict(short[] heights, int start, int row, int column) {
        int left = column > 0 ? heights[start + row * TILE_NODE_ROW + column - 1] & 0xFFFF : 0;
        int above = row > 0 ? heights[start + (row - 1) * TILE_NODE_ROW + column] & 0xFFFF : 0;
        if (column == 0 || row == 0) {
            return column > 0 ? left : above;
        }
        int corner = heights[start + (row - 1) * TILE_NODE_ROW + column - 1] & 0xFFFF;
        return left + above - corner;
    }

    private Map map(int planetId, String digest) {
        return maps.computeIfAbsent(digest, ignored -> load(planetId));
    }

    private Map load(int planetId) {
        long start = System.currentTimeMillis();
        byte[] stored = planetCrudService.getCompressedHeightMap(planetId);
        if (stored == null) {
            throw new IllegalStateException("Planet " + planetId + " has no compressed heightmap");
        }
        byte[] raw = gunzip(stored);
        if (raw.length % 2 != 0) {
            throw new IllegalStateException("Height map of planet " + planetId
                    + " is not a whole number of 16 bit values: " + raw.length + " bytes");
        }
        short[] heights = new short[raw.length / 2];
        for (int i = 0; i < heights.length; i++) {
            heights[i] = (short) ((raw[i * 2] & 0xFF) | ((raw[i * 2 + 1] & 0xFF) << 8));
        }
        PlanetConfig planetConfig = planetCrudService.read(planetId);
        Index tiles = TerrainUtil.terrainPositionToTileIndexCeil(planetConfig.getSize());
        // The stored map has to be exactly the planet it belongs to, or every tile offset computed
        // from the planet's width lands somewhere else and the terrain is quietly wrong.
        int expected = tiles.getX() * tiles.getY() * TILE_VALUES;
        if (heights.length != expected) {
            throw new IllegalStateException("Planet " + planetId + " is " + tiles.getX() + "x" + tiles.getY()
                    + " tiles, which is " + expected + " heights, but the stored map has " + heights.length);
        }
        logger.info("Height map of planet {} read: {} tiles, {} values, {} ms",
                planetId, tiles.getX() * tiles.getY(), heights.length, System.currentTimeMillis() - start);
        return new Map(heights, tiles.getX(), tiles.getY());
    }

    private static void writeU16(byte[] target, int at, int value) {
        target[at] = (byte) (value & 0xFF);
        target[at + 1] = (byte) ((value >> 8) & 0xFF);
    }

    private static void writeU32(byte[] target, int at, int value) {
        writeU16(target, at, value & 0xFFFF);
        writeU16(target, at + 2, (value >>> 16) & 0xFFFF);
    }

    private byte[] gunzip(byte[] compressed) {
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(compressed), 1 << 16)) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Stored height map is not gzip", e);
        }
    }

    /** Level 9 for the same reason as in {@link HeightMapDeltaService}: encoded once, sent often. */
    private byte[] gzip(byte[] raw) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(raw.length / 16 + 64);
            try (GZIPOutputStream gzip = new GZIPOutputStream(out, 1 << 16) {
                {
                    def.setLevel(Deflater.BEST_COMPRESSION);
                }
            }) {
                gzip.write(raw);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot compress the height map region", e);
        }
    }

    /** The decompressed map and the tile grid it is laid out on. */
    private record Map(short[] heights, int tileXCount, int tileYCount) {
    }
}
