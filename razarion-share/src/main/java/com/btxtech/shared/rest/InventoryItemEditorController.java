package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;

import javax.ws.rs.Path;

/**
 * Created by Beat
 * on 19.09.2017.
 */
@Path(CommonUrl.INVENTORY_ITEM_EDITOR_PATH)
public interface InventoryItemEditorController extends CrudController<InventoryItem> {
}
