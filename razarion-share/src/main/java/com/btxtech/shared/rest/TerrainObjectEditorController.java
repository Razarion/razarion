package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.TerrainObjectConfig;

import javax.ws.rs.Path;

/**
 * Created by Beat
 * 15.08.2015.
 */
@Path(CommonUrl.TERRAIN_OBJECT_EDITOR_PATH)
public interface TerrainObjectEditorController extends CrudController<TerrainObjectConfig>{
}
