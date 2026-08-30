package com.btxtech.server.rest.engine;

import com.btxtech.server.service.ContentDigest;
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

    public TerrainHeightMapControllerImpl(PlanetCrudService planetCrudPersistence) {
        this.planetCrudPersistence = planetCrudPersistence;
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
