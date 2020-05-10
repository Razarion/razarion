package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.shared.dto.TerrainEditorLoad;
import com.btxtech.shared.dto.TerrainEditorUpdate;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * 07.07.2016.
 */
@Path(CommonUrl.PLANET_EDITOR_SERVICE_PATH)
public interface TerrainEditorController {
    @GET
    @Path("readTerrainSlopePositions/{planetId}")
    @Produces(MediaType.APPLICATION_JSON)
    TerrainEditorLoad readTerrainEditorLoad(@PathParam("planetId") int planetId);

    @PUT
    @Path("updateTerrain/{planetId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateTerrain(@PathParam("planetId") int planetId, TerrainEditorUpdate terrainEditorUpdate);

    @PUT
    @Path("restartPlanetWarm/{planetId}")
    @Deprecated
    void restartPlanetWarm(@PathParam("planetId") int planetId);

    @PUT
    @Path("restartPlanetCold/{planetId}")
    void restartPlanetCold(@PathParam("planetId") int planetId);

    @PUT
    @Path("updatePlanetVisualConfig/{planetId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void updatePlanetVisualConfig(@PathParam("planetId") int planetId, PlanetVisualConfig planetVisualConfig);

    @PUT
    @Path("updateMiniMapImage/{planetId}")
    @Consumes(MediaType.TEXT_PLAIN)
    void updateMiniMapImage(@PathParam("planetId") int planetId, String dataUrl);
}
