package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.shape.Shape3DConfig;

import javax.ws.rs.Path;

/**
 * Created by Beat
 * 16.08.2016.
 */
@Path(CommonUrl.SHAPE_3D_EDITOR_PART)
public interface Shape3DConfigEditorController extends CrudController<Shape3DConfig> {
}
