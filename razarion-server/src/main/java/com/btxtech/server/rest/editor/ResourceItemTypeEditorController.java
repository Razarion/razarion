package com.btxtech.server.rest.editor;

import com.btxtech.server.rest.AbstractConfigController;
import com.btxtech.server.service.engine.AbstractConfigCrudService;
import com.btxtech.server.service.engine.ResourceItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/editor/resource_item_type")
public class ResourceItemTypeEditorController extends AbstractConfigController<ResourceItemType> {
    private final ResourceItemTypeService resourceItemTypeService;

    public ResourceItemTypeEditorController(ResourceItemTypeService resourceItemTypeService) {
        this.resourceItemTypeService = resourceItemTypeService;
    }

    @Override
    protected AbstractConfigCrudService<ResourceItemType, ?> getConfigCrudService() {
        return resourceItemTypeService;
    }
}
