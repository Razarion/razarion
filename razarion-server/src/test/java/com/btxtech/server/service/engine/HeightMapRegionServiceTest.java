package com.btxtech.server.service.engine;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A region has to hand back exactly the heights the whole map has at those tiles. Exactly, not
 * nearly: the server runs the planet on the full map, so a client whose heights differ anywhere
 * reads a different terrain type there, walks a different path, and drifts out of sync.
 * <p>
 * The tests build a small planet - 3 by 2 tiles - rather than the real 32 by 32, so the arithmetic
 * is checkable by hand while the tile size stays the real one.
 */
class HeightMapRegionServiceTest {
    private static final int TILE = TerrainUtil.TILE_NODE_SIZE;
    private static final int TILES_X = 3;
    private static final int TILES_Y = 2;

    /** Tile (x,y) is flat at 1000 + its index, except tile (1,0) and (2,1), which are not flat. */
    private int[] planet() {
        int[] heights = new int[TILES_X * TILES_Y * TILE];
        Random random = new Random(1);
        for (int tile = 0; tile < TILES_X * TILES_Y; tile++) {
            boolean flat = tile != 1 && tile != 5;
            int base = 1000 + tile;
            for (int i = 0; i < TILE; i++) {
                heights[tile * TILE + i] = flat ? base : base + random.nextInt(50);
            }
        }
        return heights;
    }

    private HeightMapRegionService service(int[] heights) {
        PlanetCrudService planetCrudService = mock(PlanetCrudService.class);
        when(planetCrudService.getCompressedHeightMap(anyInt())).thenReturn(gzip(toLittleEndian(heights)));
        when(planetCrudService.read(anyInt())).thenReturn(new PlanetConfig()
                .size(new DecimalPosition(TILES_X * TerrainUtil.NODE_X_COUNT, TILES_Y * TerrainUtil.NODE_Y_COUNT)));
        return new HeightMapRegionService(planetCrudService);
    }

    @Test
    void aRegionHoldsTheSameHeightsAsTheWholeMap() {
        int[] heights = planet();
        byte[] region = gunzip(service(heights).getRegion(1, "d", 1, 0, 2, 2));

        assertEquals(1, readU16(region, 0));
        assertEquals(0, readU16(region, 2));
        assertEquals(2, readU16(region, 4));
        assertEquals(2, readU16(region, 6));

        // Row-major, y outer: (1,0) (2,0) (1,1) (2,1)
        int[][] expectedTiles = {tile(heights, 1, 0), tile(heights, 2, 0), tile(heights, 1, 1), tile(heights, 2, 1)};
        for (int block = 0; block < expectedTiles.length; block++) {
            assertArrayEquals(expectedTiles[block], decodeTile(region, 8 + block * TILE * 2),
                    "block " + block + " of the region");
        }
    }

    @Test
    void everyTileStartsItsOwnDelta() {
        // The point of per tile deltas: the second tile of a region must be readable without the
        // first. Decoding it on its own has to give the same answer as decoding the whole region.
        int[] heights = planet();
        byte[] region = gunzip(service(heights).getRegion(1, "d", 0, 0, 3, 1));

        assertArrayEquals(tile(heights, 2, 0), decodeTile(region, 8 + 2 * TILE * 2),
                "the third tile read without the two before it");
    }

    @Test
    void theFlatTableNamesTheFlatTilesAndTheirHeight() {
        int[] heights = planet();
        byte[] table = gunzip(service(heights).getFlatTable(1, "d"));

        assertEquals(TILES_X, readU16(table, 0));
        assertEquals(TILES_Y, readU16(table, 2));
        int tiles = TILES_X * TILES_Y;
        int bitmask = 4;
        int values = bitmask + (tiles + 7) / 8;
        for (int tile = 0; tile < tiles; tile++) {
            boolean flagged = (table[bitmask + tile / 8] & (1 << (tile % 8))) != 0;
            if (tile == 1 || tile == 5) {
                assertFalse(flagged, "tile " + tile + " is not flat and must not be flagged");
            } else {
                assertTrue(flagged, "tile " + tile + " is flat");
                assertEquals(heights[tile * TILE], readU16(table, values + tile * 2),
                        "the flat height of tile " + tile + " must be the height it actually has");
            }
        }
    }

    @Test
    void aRectangleOutsideThePlanetIsRefused() {
        HeightMapRegionService service = service(planet());
        assertThrows(IllegalArgumentException.class, () -> service.getRegion(1, "d", 2, 0, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> service.getRegion(1, "d", -1, 0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> service.getRegion(1, "d", 0, 0, 0, 1));
    }

    @Test
    void aMapThatIsNotThePlanetIsRefused() {
        // The tile offsets are computed from the planet's width. A map of another size would put
        // every tile somewhere else and the terrain would be quietly wrong rather than missing.
        int[] tooSmall = new int[(TILES_X * TILES_Y - 1) * TILE];
        HeightMapRegionService service = service(tooSmall);
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> service.getFlatTable(1, "d"));
        assertTrue(thrown.getMessage().contains("tiles"), thrown.getMessage());
    }

    private int[] tile(int[] heights, int tileX, int tileY) {
        int[] out = new int[TILE];
        System.arraycopy(heights, (tileY * TILES_X + tileX) * TILE, out, 0, TILE);
        return out;
    }

    /** The client's arithmetic: a running sum from zero, wrapping at 16 bits, one tile at a time. */
    private int[] decodeTile(byte[] region, int at) {
        int[] out = new int[TILE];
        int acc = 0;
        for (int i = 0; i < TILE; i++) {
            acc = (acc + readU16(region, at + i * 2)) & 0xFFFF;
            out[i] = acc;
        }
        return out;
    }

    private int readU16(byte[] bytes, int at) {
        return (bytes[at] & 0xFF) | ((bytes[at + 1] & 0xFF) << 8);
    }

    private byte[] toLittleEndian(int[] values) {
        byte[] bytes = new byte[values.length * 2];
        for (int i = 0; i < values.length; i++) {
            bytes[i * 2] = (byte) (values[i] & 0xFF);
            bytes[i * 2 + 1] = (byte) ((values[i] >> 8) & 0xFF);
        }
        return bytes;
    }

    private byte[] gzip(byte[] raw) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
                gzip.write(raw);
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] gunzip(byte[] compressed) {
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
