package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.BrushConfig;

import javax.ws.rs.Path;

@Path(CommonUrl.BRUSH_EDITOR_PATH)
public interface BrushEditorController extends CrudController<BrushConfig> {
}
