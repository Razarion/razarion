package com.btxtech.server.rest.engine;

import com.btxtech.server.service.ContentDigest;
import com.btxtech.server.service.engine.HeightMapDeltaService;
import com.btxtech.server.service.engine.HeightMapRegionService;
import com.btxtech.server.service.engine.PlanetCrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
/**
 * Deliberately no longer {@code implements TerrainHeightMapController}. That interface is the
 * contract the TypeScript client is generated from, and this method now takes an If-None-Match
 * header - which is transport, not API: the browser sets it by itself and a generated client has
 * no business knowing it exists. The interface stays as it is, and the editor's generated client
 * with it.
 */
@RequestMapping("/rest/terrainHeightMap")
public class TerrainHeightMapControllerImpl {
    /**
     * May be kept, must be asked about - the pair of this and an entity tag is what replaces the
     * blanket no-store for this one response. See {@link #getCompressedHeightMap}.
     */
    private static final CacheControl REVALIDATE = CacheControl.noCache().mustRevalidate();
    private final Logger logger = LoggerFactory.getLogger(TerrainHeightMapControllerImpl.class);
    private final PlanetCrudService planetCrudPersistence;
    private final HeightMapDeltaService heightMapDeltaService;
    private final HeightMapRegionService heightMapRegionService;

    public TerrainHeightMapControllerImpl(PlanetCrudService planetCrudPersistence,
                                          HeightMapDeltaService heightMapDeltaService,
                                          HeightMapRegionService heightMapRegionService) {
        this.planetCrudPersistence = planetCrudPersistence;
        this.heightMapDeltaService = heightMapDeltaService;
        this.heightMapRegionService = heightMapRegionService;
    }

    /**
     * Every tile's constant height, for the tiles that have one - about 2 kB for a planet.
     * <p>
     * The counterpart of a region: a client that has not loaded a tile yet can answer a height read
     * out of this, exactly, for 70% of the tiles. Exactly is the point. The server runs the planet
     * on the full map, so a client that answered a height differently would see a different terrain
     * type, walk a different path, and drift out of sync - a guess would be worse than a wait.
     */
    @GetMapping(value = "/{planetId}/flat", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getFlatTable(@PathVariable("planetId") int planetId,
                                               @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false)
                                               String ifNoneMatch) {
        return conditional(planetId, ifNoneMatch, "-flat",
                digest -> heightMapRegionService.getFlatTable(planetId, digest), "getFlatTable");
    }

    /**
     * The heights of a rectangle of tiles, each tile delta encoded from zero so it can be read on
     * its own. Tiles are 160 by 160 nodes; the whole planet is 32 by 32 of them.
     * <p>
     * Measured on planet 117: the five by five tiles around the start are 302 kB against 2.44 MB
     * for the planet, and that is the point of the whole endpoint - a game should not wait for the
     * far side of a world to show the player their own base.
     */
    @GetMapping(value = "/{planetId}/region/{tileX}/{tileY}/{countX}/{countY}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getRegion(@PathVariable("planetId") int planetId,
                                            @PathVariable("tileX") int tileX,
                                            @PathVariable("tileY") int tileY,
                                            @PathVariable("countX") int countX,
                                            @PathVariable("countY") int countY,
                                            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false)
                                            String ifNoneMatch) {
        // The v2 is the encoding, and it has to be in the tag. The digest covers the heights and the
        // rest of the variant covers the rectangle, so a change to how those heights are written
        // leaves the tag untouched - and a browser then answers the next request out of its cache
        // with bytes in the old encoding while the client decodes them in the new one. That is not a
        // hypothetical: it cost an afternoon on 2026-09-14, and it hid behind curl, which has no
        // cache, and behind ctrl-F5, which does not reach a fetch made from inside a web worker.
        // Every future change to the format takes the next number.
        return conditional(planetId, ifNoneMatch,
                "-region-v2-" + tileX + "-" + tileY + "-" + countX + "-" + countY,
                digest -> heightMapRegionService.getRegion(planetId, digest, tileX, tileY, countX, countY),
                "getRegion");
    }

    /**
     * The conditional GET the three height map responses share.
     * <p>
     * The entity tag is the stored map's digest plus what this response is made of. Every encoding
     * and every rectangle therefore has a tag of its own - two responses that differ must never
     * carry the same one, or a browser hands the client the wrong bytes and the planet is quietly
     * the wrong shape.
     */
    private ResponseEntity<byte[]> conditional(int planetId, String ifNoneMatch, String variant,
                                               java.util.function.Function<String, byte[]> body, String what) {
        try {
            String digest = planetCrudPersistence.getCompressedHeightMapDigest(planetId);
            if (digest == null) {
                logger.error("Planet {} has no compressed heightmap", planetId);
                throw new RuntimeException("Planet " + planetId + " has no compressed heightmap");
            }
            String eTag = ContentDigest.eTag(digest + variant);
            if (ContentDigest.matches(ifNoneMatch, eTag)) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(eTag).cacheControl(REVALIDATE).build();
            }
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .header(HttpHeaders.CONTENT_ENCODING, "gzip")
                    .eTag(eTag)
                    .cacheControl(REVALIDATE)
                    .body(body.apply(digest));
        } catch (IllegalArgumentException e) {
            // A rectangle outside the planet is the caller's mistake, not the server's.
            logger.warn("{} for planet {}: {}", what, planetId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error {} for planet {} exception:", what, planetId, e);
            throw new RuntimeException("Error " + what + " for planet " + planetId, e);
        }
    }

    /**
     * The same height map, delta encoded, for a client that knows how to undo it.
     * <p>
     * 3.75 MB becomes 2.42 MB, on the download the engine worker waits for before the game can
     * run at all - measured as the largest single block of a start (INIT_WORKER, median 5.6 s of
     * a 12.7 s start, against a measured patience of 14.7 s). Undoing it is a running sum, 58 ms
     * for all 26.2 million values on a desktop. See {@link HeightMapDeltaService}.
     * <p>
     * A path of its own rather than a change to the one above, because the two cannot be told
     * apart by looking at the bytes: a client that fetched deltas and added nothing would render a
     * plausible, wrong planet. A URL that says which encoding it is makes that mistake impossible,
     * and lets an older cached worker keep asking for what it understands.
     */
    @GetMapping(value = "/{planetId}/delta", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getDeltaHeightMap(@PathVariable("planetId") int planetId,
                                                    @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false)
                                                    String ifNoneMatch) {
        try {
            String digest = planetCrudPersistence.getCompressedHeightMapDigest(planetId);
            if (digest == null) {
                logger.error("Planet {} has no compressed heightmap", planetId);
                throw new RuntimeException("Planet " + planetId + " has no compressed heightmap");
            }
            // Derived from the stored map's digest rather than from the delta bytes: it is the same
            // content in another encoding, and the two responses must never share an entity tag.
            String eTag = ContentDigest.eTag(digest + "-delta");
            if (ContentDigest.matches(ifNoneMatch, eTag)) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                        .eTag(eTag)
                        .cacheControl(REVALIDATE)
                        .build();
            }
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .header(HttpHeaders.CONTENT_ENCODING, "gzip")
                    .eTag(eTag)
                    .cacheControl(REVALIDATE)
                    .body(heightMapDeltaService.getDeltaHeightMap(planetId, digest));
        } catch (Exception e) {
            logger.error("Error getDeltaHeightMap for planet {} exception:", planetId, e);
            throw new RuntimeException("Error getDeltaHeightMap for planet " + planetId, e);
        }
    }

    /**
     * The terrain height map, as a conditional GET.
     * <p>
     * This is one of the two downloads the game engine worker waits for before the game can run -
     * four megabytes of it - and every start asked for all of it, because {@code NoCacheRestFilter}
     * marks everything under /rest/ as no-store. That header protects something real: a re-uploaded
     * height map has to reach the player, or they walk on terrain that no longer exists. But
     * no-store forbids keeping the file at all, which is heavier than the guarantee needs.
     * <p>
     * PROD, 2026-08-30: of the sessions that got their user interface up and never a running game,
     * all the recent ones were waiting on this task. The bytes are already gzip in the database, so
     * there is nothing to win by compressing them again - only by not sending them twice.
     */
    @GetMapping(value = "/{planetId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getCompressedHeightMap(@PathVariable("planetId") int planetId,
                                                        @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false)
                                                        String ifNoneMatch) {
        try {
            String digest = planetCrudPersistence.getCompressedHeightMapDigest(planetId);
            if (digest != null) {
                String eTag = ContentDigest.eTag(digest);
                if (ContentDigest.matches(ifNoneMatch, eTag)) {
                    return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                            .eTag(eTag)
                            .cacheControl(REVALIDATE)
                            .build();
                }
                return ResponseEntity
                        .ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                        .header(HttpHeaders.CONTENT_ENCODING, "gzip")
                        .eTag(eTag)
                        .cacheControl(REVALIDATE)
                        .body(planetCrudPersistence.getCompressedHeightMap(planetId));
            } else {
                logger.error("Planet {} has no compressed heightmap", planetId);
                throw new RuntimeException("Planet " + planetId + " has no compressed heightmap");
            }
        } catch (Exception e) {
            logger.error("Error getCompressedHeightMap for planet {} exception:", planetId, e);
            throw new RuntimeException("Error getCompressedHeightMap for planet " + planetId, e);
        }
    }
}
