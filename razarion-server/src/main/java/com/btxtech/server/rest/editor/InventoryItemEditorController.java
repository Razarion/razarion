package com.btxtech.server.rest.editor;

import com.btxtech.server.rest.AbstractConfigController;
import com.btxtech.server.service.engine.AbstractConfigCrudService;
import com.btxtech.server.service.engine.InventoryItemService;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/editor/inventory-item")
public class InventoryItemEditorController extends AbstractConfigController<InventoryItem> {
    private final InventoryItemService inventoryItemService;

    public InventoryItemEditorController(InventoryItemService inventoryItemService) {
        this.inventoryItemService = inventoryItemService;
    }

    @Override
    protected AbstractConfigCrudService<InventoryItem, ?> getConfigCrudService() {
        return inventoryItemService;
    }
}
