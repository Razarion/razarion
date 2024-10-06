package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.TerrainEditorUpdate;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * 07.07.2016.
 */
@Path(CommonUrl.PLANET_EDITOR_SERVICE_PATH)
public interface TerrainEditorController {

    @PUT
    @Path("updateTerrain/{planetId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateTerrain(@PathParam("planetId") int planetId, TerrainEditorUpdate terrainEditorUpdate);

    @PUT
    @Path("updateMiniMapImage/{planetId}")
    @Consumes(MediaType.TEXT_PLAIN)
    void updateMiniMapImage(@PathParam("planetId") int planetId, String dataUrl);

    @POST
    @Path("updateCompressedHeightMap/{planetId}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    void updateCompressedHeightMap(@PathParam("planetId") int planetId, byte[] zippedHeightMap);
}
