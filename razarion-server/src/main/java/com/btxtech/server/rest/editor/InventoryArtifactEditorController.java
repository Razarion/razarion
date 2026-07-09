package com.btxtech.server.rest.editor;

import com.btxtech.server.rest.AbstractConfigController;
import com.btxtech.server.service.engine.AbstractConfigCrudService;
import com.btxtech.server.service.engine.InventoryArtifactService;
import com.btxtech.shared.gameengine.datatypes.InventoryArtifact;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/editor/inventory-artifact")
public class InventoryArtifactEditorController extends AbstractConfigController<InventoryArtifact> {
    private final InventoryArtifactService inventoryArtifactService;

    public InventoryArtifactEditorController(InventoryArtifactService inventoryArtifactService) {
        this.inventoryArtifactService = inventoryArtifactService;
    }

    @Override
    protected AbstractConfigCrudService<InventoryArtifact, ?> getConfigCrudService() {
        return inventoryArtifactService;
    }
}
