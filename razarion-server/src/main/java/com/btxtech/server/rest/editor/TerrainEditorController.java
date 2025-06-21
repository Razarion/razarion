package com.btxtech.server.rest.editor;

import com.btxtech.server.model.DataUrlDecoder;
import com.btxtech.server.model.Roles;
import com.btxtech.server.service.engine.PlanetCrudPersistence;
import com.btxtech.shared.dto.TerrainEditorUpdate;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/editor/planeteditor")
public class TerrainEditorController {
    private final Logger logger = LoggerFactory.getLogger(TerrainEditorController.class);
    private final PlanetCrudPersistence planetCrudPersistence;

    public TerrainEditorController(PlanetCrudPersistence planetCrudPersistence) {
        this.planetCrudPersistence = planetCrudPersistence;
    }

    @PutMapping(value = "updateTerrain/{planetId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed(Roles.ADMIN)
    public void updateTerrain(@PathVariable("planetId") int planetId, @RequestBody TerrainEditorUpdate terrainEditorUpdate) {
        try {
            // Check if terrain is valid
            // this does not make any sense terrainShapeService.setupTerrainShapeDryRun(planetId, terrainEditorUpdate);

            if (terrainEditorUpdate.getCreatedTerrainObjects() != null && !terrainEditorUpdate.getCreatedTerrainObjects().isEmpty()) {
                planetCrudPersistence.createTerrainObjectPositions(planetId, terrainEditorUpdate.getCreatedTerrainObjects());
            }
            if (terrainEditorUpdate.getUpdatedTerrainObjects() != null && !terrainEditorUpdate.getUpdatedTerrainObjects().isEmpty()) {
                planetCrudPersistence.updateTerrainObjectPositions(planetId, terrainEditorUpdate.getUpdatedTerrainObjects());
            }
            if (terrainEditorUpdate.getDeletedTerrainObjectsIds() != null && !terrainEditorUpdate.getDeletedTerrainObjectsIds().isEmpty()) {
                planetCrudPersistence.deleteTerrainObjectPositionIds(planetId, terrainEditorUpdate.getDeletedTerrainObjectsIds());
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }


    @PutMapping(value = "updateMiniMapImage/{planetId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed(Roles.ADMIN)
    public void updateMiniMapImage(@PathVariable("planetId") int planetId, @RequestBody String dataUrl) {
        try {
            DataUrlDecoder dataUrlDecoder = new DataUrlDecoder(dataUrl);
            planetCrudPersistence.updateMiniMapImage(planetId, dataUrlDecoder.getData());
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping(value = "updateCompressedHeightMap/{planetId}", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @RolesAllowed(Roles.ADMIN)
    public void updateCompressedHeightMap(@PathVariable("planetId") int planetId, @RequestBody byte[] zippedHeightMap) {
        try {
            planetCrudPersistence.updateCompressedHeightMap(planetId, zippedHeightMap);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }
}
