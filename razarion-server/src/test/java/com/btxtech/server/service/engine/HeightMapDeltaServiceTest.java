package com.btxtech.server.service.engine;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The delta encoding has to be exactly undone by the running sum the worker does, and the sum is
 * written in JavaScript in another module. So the round trip is asserted here against the same
 * arithmetic, including the 16 bit wrap that carries values whose difference is negative.
 */
class HeightMapDeltaServiceTest {

    @Test
    void roundTripIsExact() {
        int[] heights = {20000, 20001, 19999, 0, 65535, 65535, 0, 12345, 12345};
        byte[] encoded = encodeThrough(heights);
        assertArrayEquals(heights, decodeLikeTheWorker(encoded, heights.length),
                "a value that goes down wraps the difference, and the worker's sum has to wrap back");
    }

    @Test
    void roundTripIsExactForRealisticTerrain() {
        // A smooth surface with noise, which is what a height map is and what the encoding is for.
        Random random = new Random(42);
        int[] heights = new int[200_000];
        int height = 20000;
        for (int i = 0; i < heights.length; i++) {
            height += random.nextInt(7) - 3;
            heights[i] = Math.max(0, Math.min(65535, height));
        }
        assertArrayEquals(heights, decodeLikeTheWorker(encodeThrough(heights), heights.length));
    }

    /**
     * The point of the whole exercise: the delta form has to compress, and the raw form does not.
     * Asserted as a ratio rather than a size so it does not go stale, and generously - the real map
     * measured 3.75 MB against 2.42 MB, which is a third off.
     */
    @Test
    void deltaCompressesWhereTheRawFormDoesNot() {
        Random random = new Random(7);
        int[] heights = new int[500_000];
        int height = 20000;
        for (int i = 0; i < heights.length; i++) {
            height += random.nextInt(7) - 3;
            heights[i] = Math.max(0, Math.min(65535, height));
        }
        int raw = gzip(toLittleEndian(heights)).length;
        int delta = encodeThrough(heights).length;
        assertTrue(delta < raw * 0.8,
                "delta " + delta + " should be clearly smaller than raw " + raw);
    }

    @Test
    void refusesAHalfValue() {
        PlanetCrudService planetCrudService = mock(PlanetCrudService.class);
        when(planetCrudService.getCompressedHeightMap(anyInt())).thenReturn(gzip(new byte[]{1, 2, 3}));
        HeightMapDeltaService service = new HeightMapDeltaService(planetCrudService);

        IllegalStateException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class, () -> service.getDeltaHeightMap(1, "digest"));
        assertTrue(thrown.getMessage().contains("16 bit"), thrown.getMessage());
    }

    @Test
    void encodesOncePerDigest() {
        PlanetCrudService planetCrudService = mock(PlanetCrudService.class);
        when(planetCrudService.getCompressedHeightMap(anyInt()))
                .thenReturn(gzip(toLittleEndian(new int[]{1, 2, 3, 4})));
        HeightMapDeltaService service = new HeightMapDeltaService(planetCrudService);

        byte[] first = service.getDeltaHeightMap(1, "digest");
        byte[] second = service.getDeltaHeightMap(1, "digest");

        assertEquals(first, second, "the same digest must hand back the very same array, not a new one");
    }

    /** Runs the bytes through the service, with the stored map mocked, and returns what it sends. */
    private byte[] encodeThrough(int[] heights) {
        PlanetCrudService planetCrudService = mock(PlanetCrudService.class);
        when(planetCrudService.getCompressedHeightMap(anyInt())).thenReturn(gzip(toLittleEndian(heights)));
        return new HeightMapDeltaService(planetCrudService).getDeltaHeightMap(1, "digest");
    }

    /**
     * The worker's loop, in Java: acc = (acc + delta) & 0xFFFF over a Uint16Array. Kept identical
     * to the JSBody in TeaVMNativeTerrainShapeAccess.undoDelta on purpose - if that one changes,
     * this is what says so.
     */
    private int[] decodeLikeTheWorker(byte[] gzipped, int count) {
        byte[] deltas = gunzip(gzipped);
        int[] heights = new int[count];
        int acc = 0;
        for (int i = 0; i < count; i++) {
            int delta = (deltas[i * 2] & 0xFF) | ((deltas[i * 2 + 1] & 0xFF) << 8);
            acc = (acc + delta) & 0xFFFF;
            heights[i] = acc;
        }
        return heights;
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
