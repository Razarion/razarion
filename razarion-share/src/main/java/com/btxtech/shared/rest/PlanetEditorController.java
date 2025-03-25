package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;

import javax.ws.rs.Path;

@Path(CommonUrl.PLANET_EDITOR_PATH)
public interface PlanetEditorController extends CrudController<PlanetConfig> {
}
