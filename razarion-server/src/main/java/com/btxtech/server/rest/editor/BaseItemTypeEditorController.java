package com.btxtech.server.rest.editor;

import com.btxtech.server.model.engine.BaseItemTypeEntity;
import com.btxtech.server.rest.AbstractConfigController;
import com.btxtech.server.service.engine.AbstractConfigCrudService;
import com.btxtech.server.service.engine.BaseItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/editor/base_item_type")
public class BaseItemTypeEditorController extends AbstractConfigController<BaseItemType> {
    private final BaseItemTypeService baseItemTypeService;

    public BaseItemTypeEditorController(BaseItemTypeService baseItemTypeService) {
        this.baseItemTypeService = baseItemTypeService;
    }

    @Override
    protected AbstractConfigCrudService<BaseItemType, BaseItemTypeEntity> getConfigCrudService() {
        return baseItemTypeService;
    }
}
