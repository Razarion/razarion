package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;

import javax.ws.rs.Path;

/**
 * Created by Beat
 * 16.08.2016.
 */
@Path(CommonUrl.SHAPE_3D_EDITOR_PATH)
public interface Shape3DEditorController extends CrudController<Shape3DConfig> {
}
