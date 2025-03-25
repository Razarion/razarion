package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;

import javax.ws.rs.Path;

@Path(CommonUrl.BOX_ITEM_TYPE_EDITOR_PATH)
public interface BoxItemTypeEditorController extends CrudController<BoxItemType> {
}
