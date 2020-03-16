package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;

import javax.ws.rs.Path;

/**
 * Created by Beat
 * on 22.08.2017.
 */
@Path(CommonUrl.LEVEL_EDITOR_PATH)
public interface LevelEditorController extends CrudController<LevelConfig>{
}
