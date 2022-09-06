package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.shape.ThreeJsModelPackConfig;

import javax.ws.rs.Path;

@Path(CommonUrl.THREE_JS_MODEL_PACK_EDITOR_PATH)
public interface ThreeJsModelPackEditorController extends CrudController<ThreeJsModelPackConfig> {
}
