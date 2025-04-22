package com.btxtech.server.rest.editor;

import com.btxtech.server.model.engine.BaseItemTypeEntity;
import com.btxtech.server.rest.AbstractConfigController;
import com.btxtech.server.service.engine.AbstractConfigCrudPersistence;
import com.btxtech.server.service.engine.BaseItemTypeCrudPersistence;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/editor/base_item_type")
public class BaseItemTypeEditorController extends AbstractConfigController<BaseItemType> {
    private final BaseItemTypeCrudPersistence persistenceService;

    public BaseItemTypeEditorController(BaseItemTypeCrudPersistence persistenceService) {
        this.persistenceService = persistenceService;
    }

    @Override
    protected AbstractConfigCrudPersistence<BaseItemType, BaseItemTypeEntity> getConfigCrudPersistence() {
        return persistenceService;
    }
}
