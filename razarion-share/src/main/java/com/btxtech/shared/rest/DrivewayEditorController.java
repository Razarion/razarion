package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.DrivewayConfig;

import javax.ws.rs.Path;

@Path(CommonUrl.DRIVEWAY_EDITOR_PATH)
public interface DrivewayEditorController extends CrudController<DrivewayConfig> {
}
