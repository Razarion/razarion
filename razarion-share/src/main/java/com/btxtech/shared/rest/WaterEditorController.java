package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.WaterConfig;

import javax.ws.rs.Path;

@Path(CommonUrl.WATER_EDITOR_PATH)
public interface WaterEditorController extends CrudController<WaterConfig> {
}
