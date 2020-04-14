package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;

import javax.ws.rs.Path;

@Path(CommonUrl.SLOPE_EDITOR_PATH)
public interface SlopeEditorController extends CrudController<SlopeConfig>{
}
