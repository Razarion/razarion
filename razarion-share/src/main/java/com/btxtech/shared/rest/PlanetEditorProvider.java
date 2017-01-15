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
    @Path("createTerrainSlopePositions")
    @Consumes(MediaType.APPLICATION_JSON)
    void createTerrainSlopePositions(List<TerrainSlopePosition> createdSlopes);

    @PUT
    @Path("updateTerrainSlopePositions")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateTerrainSlopePositions(List<TerrainSlopePosition> updatedSlopes);

    @PUT
    @Path("deleteTerrainSlopePositions")
    @Consumes(MediaType.APPLICATION_JSON)
    void deleteTerrainSlopePositions(List<Integer> deletedSlopeIds);

    @PUT
    @Path("saveTerrainObjectPositions")
    @Consumes(MediaType.APPLICATION_JSON)
    void saveTerrainObjectPositions(List<TerrainObjectPosition> terrainObjectPositions);
}
