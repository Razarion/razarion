package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainObjectConfig;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Beat
 * 15.08.2015.
 */
@Path(CommonUrl.TERRAIN_OBJECT_EDITOR_PATH)
public interface TerrainObjectEditorController extends CrudController<TerrainObjectConfig>{

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readDrivewayObjectNameIds")
    List<ObjectNameId> readDrivewayObjectNameIds();
}
