package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.ServerGameEngineConfig;

import javax.ws.rs.Path;

@Path(CommonUrl.SERVER_GAME_ENGINE_EDITOR_PATH)
public interface ServerGameEngineEditorController extends CrudController<ServerGameEngineConfig> {
}
