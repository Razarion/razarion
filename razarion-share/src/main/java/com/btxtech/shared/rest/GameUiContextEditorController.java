package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.GameUiContextConfig;

import javax.ws.rs.Path;

@Path(CommonUrl.GAME_UI_CONTEXT_EDITOR_PATH)
public interface GameUiContextEditorController extends CrudController<GameUiContextConfig> {
}
