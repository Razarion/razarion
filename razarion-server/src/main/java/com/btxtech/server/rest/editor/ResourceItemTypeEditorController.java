package com.btxtech.server.rest.editor;

import com.btxtech.server.rest.AbstractConfigController;
import com.btxtech.server.service.engine.AbstractConfigCrudPersistence;
import com.btxtech.server.service.engine.ResourceItemTypeCrudPersistence;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/editor/resource_item_type")
public class ResourceItemTypeEditorController extends AbstractConfigController<ResourceItemType> {
    private final ResourceItemTypeCrudPersistence resourceItemTypeCrudPersistence;

    public ResourceItemTypeEditorController(ResourceItemTypeCrudPersistence resourceItemTypeCrudPersistence) {
        this.resourceItemTypeCrudPersistence = resourceItemTypeCrudPersistence;
    }

    @Override
    protected AbstractConfigCrudPersistence<ResourceItemType, ?> getConfigCrudPersistence() {
        return resourceItemTypeCrudPersistence;
    }
}
