package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;

import javax.ws.rs.Path;

@Path(CommonUrl.BASE_ITEM_TYPE_EDITOR_PATH)
public interface BaseItemTypeEditorController extends CrudController<BaseItemType> {
}
