package com.btxtech.server.rest;

import com.btxtech.server.DataUrlDecoder;
import com.btxtech.server.persistence.PlanetCrudPersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.TerrainEditorUpdate;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * 08.07.2016.
 */
@Path(CommonUrl.PLANET_EDITOR_SERVICE_PATH)
public class TerrainEditorController {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private PlanetCrudPersistence planetCrudPersistence;

    @PUT
    @Path("updateTerrain/{planetId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @SecurityCheck
    public void updateTerrain(@PathParam("planetId") int planetId, TerrainEditorUpdate terrainEditorUpdate) {
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
            exceptionHandler.handleException(e);
            throw e;
        }
    }


    @PUT
    @Path("updateMiniMapImage/{planetId}")
    @Consumes(MediaType.TEXT_PLAIN)
    @SecurityCheck
    public void updateMiniMapImage(@PathParam("planetId") int planetId, String dataUrl) {
        try {
            DataUrlDecoder dataUrlDecoder = new DataUrlDecoder(dataUrl);
            planetCrudPersistence.updateMiniMapImage(planetId, dataUrlDecoder.getData());
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @POST
    @Path("updateCompressedHeightMap/{planetId}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @SecurityCheck
    public void updateCompressedHeightMap(@PathParam("planetId") int planetId, byte[] zippedHeightMap) {
        try {
            planetCrudPersistence.updateCompressedHeightMap(planetId, zippedHeightMap);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
