package com.btxtech.server.rest.engine;

import com.btxtech.server.service.engine.PlanetCrudService;
import com.btxtech.shared.rest.TerrainHeightMapController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/rest/terrainHeightMap")
public class TerrainHeightMapControllerImpl implements TerrainHeightMapController {
    private final Logger logger = LoggerFactory.getLogger(TerrainHeightMapControllerImpl.class);
    private final PlanetCrudService planetCrudPersistence;

    public TerrainHeightMapControllerImpl(PlanetCrudService planetCrudPersistence) {
        this.planetCrudPersistence = planetCrudPersistence;
    }

    @Override
    @GetMapping(value = "/{planetId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getCompressedHeightMap(@PathVariable("planetId") int planetId) {
        try {
            byte[] compressedHeightMap = planetCrudPersistence.getCompressedHeightMap(planetId);
            if (compressedHeightMap != null) {
                return ResponseEntity
                        .ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                        .header(HttpHeaders.CONTENT_ENCODING, "gzip")
                        .body(compressedHeightMap);
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
