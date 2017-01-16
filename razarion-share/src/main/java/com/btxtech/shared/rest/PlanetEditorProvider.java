package com.btxtech.shared.rest;

import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Beat
 * 07.07.2016.
 */
@Path(RestUrl.PLANET_EDITOR_SERVICE_PATH)
public interface PlanetEditorProvider {
    @PUT
    @Path("createTerrainObjectPositions")
    @Consumes(MediaType.APPLICATION_JSON)
    void createTerrainObjectPositions(List<TerrainObjectPosition> createdTerrainObjects);

    @PUT
    @Path("updateTerrainObjectPositions")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateTerrainObjectPositions(List<TerrainObjectPosition> updatedTerrainObjects);

    @PUT
    @Path("deleteTerrainObjectPositionIds")
    @Consumes(MediaType.APPLICATION_JSON)
    void deleteTerrainObjectPositionIds(List<Integer> deletedTerrainIds);

    @PUT
    @Path("createTerrainSlopePositions")
    @Consumes(MediaType.APPLICATION_JSON)
    void createTerrainSlopePositions(List<TerrainSlopePosition> createdSlopes);

    @PUT
    @Path("updateTerrainSlopePositions")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateTerrainSlopePositions(List<TerrainSlopePosition> updatedSlopes);

    @PUT
    @Path("deleteTerrainSlopePositionIds")
    @Consumes(MediaType.APPLICATION_JSON)
    void deleteTerrainSlopePositionIds(List<Integer> deletedSlopeIds);
}
