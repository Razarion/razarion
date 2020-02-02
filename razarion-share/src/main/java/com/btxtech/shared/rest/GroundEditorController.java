package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.GroundSkeletonConfig;

import javax.ws.rs.Path;

@Path(CommonUrl.GROUND_EDITOR_PATH)
public interface GroundEditorController extends CrudController<GroundSkeletonConfig> {
}
