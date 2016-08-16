package com.btxtech.shared;

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
    @Path("saveTerrainSlopePositions")
    @Consumes(MediaType.APPLICATION_JSON)
    void saveTerrainSlopePositions(List<TerrainSlopePosition> terrainSlopePositions);

    @PUT
    @Path("saveTerrainObjectPositions")
    @Consumes(MediaType.APPLICATION_JSON)
    void saveTerrainObjectPositions(List<TerrainObjectPosition> terrainObjectPositions);

}
