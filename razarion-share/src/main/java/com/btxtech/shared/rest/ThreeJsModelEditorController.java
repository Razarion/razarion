package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;

import javax.ws.rs.Path;

@Path(CommonUrl.THREE_JS_MODEL_EDITOR_PATH)
public interface ThreeJsModelEditorController extends CrudController<ThreeJsModelConfig> {
}
