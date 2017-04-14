package com.btxtech.shared.rest;

import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Beat
 * 07.07.2016.
 */
@Path(RestUrl.PLANET_EDITOR_SERVICE_PATH)
public interface PlanetEditorProvider {
    @POST
    @Path("createTerrainObjectPositions/{planetId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void createTerrainObjectPositions(@PathParam("planetId") int planetId, List<TerrainObjectPosition> createdTerrainObjects);

    @PUT
    @Path("updateTerrainObjectPositions/{planetId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateTerrainObjectPositions(@PathParam("planetId") int planetId, List<TerrainObjectPosition> updatedTerrainObjects);

    @DELETE
    @Path("deleteTerrainObjectPositionIds/{planetId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void deleteTerrainObjectPositionIds(@PathParam("planetId") int planetId, List<Integer> deletedTerrainIds);

    @POST
    @Path("createTerrainSlopePositions/{planetId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void createTerrainSlopePositions(@PathParam("planetId") int planetId, List<TerrainSlopePosition> createdSlopes);

    @PUT
    @Path("updateTerrainSlopePositions/{planetId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateTerrainSlopePositions(@PathParam("planetId") int planetId, List<TerrainSlopePosition> updatedSlopes);

    @DELETE
    @Path("deleteTerrainSlopePositionIds/{planetId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void deleteTerrainSlopePositionIds(@PathParam("planetId") int planetId, List<Integer> deletedSlopeIds);
}
