package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.shared.dto.SlopeTerrainEditorUpdate;
import com.btxtech.shared.dto.TerrainEditorUpdate;
import com.btxtech.shared.dto.TerrainSlopePosition;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Beat
 * 07.07.2016.
 */
@Path(CommonUrl.PLANET_EDITOR_SERVICE_PATH)
public interface TerrainEditorController {

    @PUT
    @Path("updateSlopes/{planetId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateSlopes(@PathParam("planetId") int planetId, SlopeTerrainEditorUpdate slopeTerrainEditorUpdate);

    @GET
    @Path("readTerrainSlopePositions/{planetId}")
    @Produces(MediaType.APPLICATION_JSON)
    List<TerrainSlopePosition> readTerrainSlopePositions(@PathParam("planetId") int planetId);

    @PUT
    @Path("updateTerrain/{planetId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateTerrain(@PathParam("planetId") int planetId, TerrainEditorUpdate terrainEditorUpdate);

    @PUT
    @Path("updatePlanetVisualConfig/{planetId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void updatePlanetVisualConfig(@PathParam("planetId") int planetId, PlanetVisualConfig planetVisualConfig);

    @PUT
    @Path("updateMiniMapImage/{planetId}")
    @Consumes(MediaType.TEXT_PLAIN)
    void updateMiniMapImage(@PathParam("planetId") int planetId, String dataUrl);

    @POST
    @Path("updateCompressedHeightMap/{planetId}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    void updateCompressedHeightMap(@PathParam("planetId") int planetId, byte[] zippedHeightMap);
}
