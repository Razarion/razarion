package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.TerrainObjectConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import static com.btxtech.shared.CommonUrl.UPDATE_RADIUS;

/**
 * Created by Beat
 * 15.08.2015.
 */
@Path(CommonUrl.TERRAIN_OBJECT_EDITOR_PATH)
public interface TerrainObjectEditorController extends CrudController<TerrainObjectConfig>{
    @POST
    @Path(UPDATE_RADIUS + "/{terrainObjectId}/{radius}")
    void updateRadius(@PathParam("terrainObjectId") int terrainObjectId, @PathParam("radius") double radius);
}
