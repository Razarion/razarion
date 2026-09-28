package com.btxtech.server.rest.engine;

import com.btxtech.server.model.Roles;
import com.btxtech.server.service.ContentDigest;
import com.btxtech.server.service.engine.ServerGameEngineService;
import com.btxtech.server.service.engine.ServerTerrainShapeService;
import com.btxtech.server.service.ui.GameUiContextService;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.ServerGameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.rest.TerrainShapeController;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rest/terrainshape")
public class TerrainShapeControllerImpl {
    private static final CacheControl REVALIDATE = CacheControl.noCache().mustRevalidate();
    private static final long PREFETCH_TTL_MILLIS = 60_000;
    private final ServerTerrainShapeService serverTerrainShapeService;
    private final ServerGameEngineService serverGameEngineCrudPersistence;
    private final GameUiContextService gameUiContextService;
    private volatile PrefetchUrls prefetchUrls;

    public TerrainShapeControllerImpl(ServerTerrainShapeService serverTerrainShapeService,
                                      ServerGameEngineService serverGameEngineCrudPersistence,
                                      GameUiContextService gameUiContextService) {
        this.serverTerrainShapeService = serverTerrainShapeService;
        this.serverGameEngineCrudPersistence = serverGameEngineCrudPersistence;
        this.gameUiContextService = gameUiContextService;
    }

    /**
     * Revalidated rather than uncached: see ServerTerrainShapeService#getSerializedTerrainShape. The
     * exception for GET in NoCacheRestFilter is what lets this header through.
     */
    @GetMapping(value = "/{planetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> getTerrainShape(@PathVariable("planetId") int planetId,
                                                  @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
        ServerTerrainShapeService.SerializedTerrainShape shape = serverTerrainShapeService.getSerializedTerrainShape(planetId);
        if (ContentDigest.matches(ifNoneMatch, shape.eTag())) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(shape.eTag()).cacheControl(REVALIDATE).build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.CONTENT_ENCODING, "gzip")
                .eTag(shape.eTag())
                .cacheControl(REVALIDATE)
                .body(shape.gzippedJson());
    }

    /**
     * The terrain a new player's worker is going to ask for - exactly its urls, made the way the
     * worker makes them (TeaVMNativeTerrainShapeAccess, TerrainShapeManager#setupDimension).
     * <p>
     * The game page asks for these while its JavaScript is still arriving, so that the worker finds
     * them in the HTTP cache instead of starting the 3 MB only once it runs. Chrome against PROD
     * with a phone's network, 2026-09-27: prefetching the height map alone brought the start from
     * 15.9-16.3 s to 14.9-15.0 s. The page cannot know the planet before its game context, which
     * arrives after the JavaScript, hence the question.
     */
    @GetMapping(value = "/prefetch", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> prefetchUrls() {
        PrefetchUrls urls = prefetchUrls;
        long now = System.currentTimeMillis();
        if (urls == null || now - urls.loadedAt() > PREFETCH_TTL_MILLIS) {
            PlanetConfig planetConfig = gameUiContextService.starterPlanetConfig();
            int planetId = planetConfig.getId();
            Index tileCount = TerrainUtil.terrainPositionToTileIndexCeil(planetConfig.getSize());
            urls = new PrefetchUrls(List.of(
                    CommonUrl.terrainShapeController(planetId),
                    CommonUrl.terrainHeightMapFlatController(planetId),
                    CommonUrl.terrainHeightMapRegionController(planetId, 0, 0, tileCount.getX(), tileCount.getY())),
                    now);
            prefetchUrls = urls;
        }
        return urls.urls();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public void createTerrainShape(int planetId) {
        ServerGameEngineConfig serverGameEngineConfig = serverGameEngineCrudPersistence.read().get(0);
        serverTerrainShapeService.createTerrainShape(serverGameEngineConfig.getBotConfigs(), planetId);
    }

    private record PrefetchUrls(List<String> urls, long loadedAt) {
    }
}
