package com.btxtech.server.service.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * The height map, re-encoded so that it compresses.
 *
 * <p>The stored map is 50 MB of little-endian unsigned 16-bit heights that gzip to 3.75 MB, and
 * that is as far as gzip gets: neighbouring heights differ only in their low bits, so the low byte
 * of the stream looks like noise and the compressor finds almost nothing to repeat. Measured on
 * planet 117, 26,214,400 values.
 *
 * <p>Subtracting each value from the one before it turns that around. Terrain is smooth, so almost
 * every difference is small, the high byte of the delta stream is nearly all zero, and gzip has
 * something to work with:
 *
 * <pre>
 *   stored today, gzip of raw u16   3,933,303 B   3.75 MB
 *   delta int16, then gzip          2,554,465 B   2.44 MB   -36%, what this sends
 *   delta int16, then brotli        ~2.20 MB                no brotli on the embedded server yet
 *   delta + varint, then gzip       ~2.08 MB                a parser on the client, not a sum
 *
 * The first two are measured through this class on the production map of planet 117; the other two
 * come from the same data through a separate tool and are here to say what was left on the table.
 * </pre>
 *
 * <p>Why this one: undoing it is a running sum over a typed array, measured at 58 ms for all 26.2
 * million values in JavaScript on a desktop and expected around 150 ms on a phone by the factor
 * Draco showed. Against 1.38 MB less to download at a measured mobile median of 1 MB/s, that trade
 * is not close. Encoding costs 2.6 s, once per planet per pod.
 * The varint form is 0.34 MB smaller and needs a byte-at-a-time parser instead, which is the wrong
 * side of the same trade.
 *
 * <p>Computed on demand and kept, keyed by the digest of the stored map: a planet whose height map
 * is re-uploaded gets a new digest and the next request recomputes. One planet's worth is 2.4 MB in
 * memory and a few seconds of CPU, once per pod.
 *
 * <p>Deliberately not done at upload time. That would need a second column, a migration for every
 * existing planet, and a change to the editor's upload path - all to save a one-off cost that is
 * paid once per planet per pod start, in the background of a request that is already being served
 * from a cache the first time anybody asks.
 */
@Service
public class HeightMapDeltaService {
    private final Logger logger = LoggerFactory.getLogger(HeightMapDeltaService.class);
    private final PlanetCrudService planetCrudService;
    /** digest of the stored map -> the delta encoding of it, gzip compressed. */
    private final ConcurrentHashMap<String, byte[]> cache = new ConcurrentHashMap<>();

    public HeightMapDeltaService(PlanetCrudService planetCrudService) {
        this.planetCrudService = planetCrudService;
    }

    /**
     * The delta encoding for this planet, gzip compressed, ready to be sent as it is.
     *
     * @param digest the digest of the stored map, which is also the cache key - the caller has it
     *               already because it is the entity tag of the response.
     */
    public byte[] getDeltaHeightMap(int planetId, String digest) {
        return cache.computeIfAbsent(digest, ignored -> encode(planetId));
    }

    private byte[] encode(int planetId) {
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
        byte[] delta = new byte[raw.length];
        int previous = 0;
        for (int i = 0; i < raw.length; i += 2) {
            // Little endian, the order the browser's Uint16Array reads.
            int value = (raw[i] & 0xFF) | ((raw[i + 1] & 0xFF) << 8);
            // Wraps on purpose: the client adds with the same 16 bit wrap, so the round trip is
            // exact without ever needing a sign or a wider type.
            int diff = (value - previous) & 0xFFFF;
            previous = value;
            delta[i] = (byte) (diff & 0xFF);
            delta[i + 1] = (byte) ((diff >> 8) & 0xFF);
        }
        byte[] compressed = gzip(delta);
        logger.info("Height map delta for planet {}: {} -> {} bytes ({} values, {} ms)",
                planetId, stored.length, compressed.length, raw.length / 2, System.currentTimeMillis() - start);
        return compressed;
    }

    private byte[] gunzip(byte[] compressed) {
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            ByteArrayOutputStream out = new ByteArrayOutputStream(compressed.length * 8);
            byte[] buffer = new byte[1 << 16];
            int read;
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Stored height map is not gzip", e);
        }
    }

    /**
     * At the highest level, not the default one.
     *
     * <p>GZIPOutputStream deflates at level 6 unless told otherwise, and on this data that leaves
     * 29 kB on the table: 2,583,384 bytes against 2,554,465 at level 9, measured on the real map.
     * It costs 715 ms against 2.6 s to encode. That CPU is paid once per planet per pod and the
     * bytes are paid by every player who starts a game, so the trade is not close. There is no
     * setter for the level, hence the subclass - the documented way to reach the Deflater the
     * stream already owns.
     */
    private byte[] gzip(byte[] raw) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(raw.length / 16);
            try (GZIPOutputStream gzip = new GZIPOutputStream(out, 1 << 16) {
                {
                    def.setLevel(Deflater.BEST_COMPRESSION);
                }
            }) {
                gzip.write(raw);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot compress the delta height map", e);
        }
    }
}
