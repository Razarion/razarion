package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;

import javax.ws.rs.Path;

@Path(CommonUrl.RESOURCE_ITEM_TYPE_EDITOR_PATH)
public interface ResourceItemTypeEditorController extends CrudController<ResourceItemType> {
}
