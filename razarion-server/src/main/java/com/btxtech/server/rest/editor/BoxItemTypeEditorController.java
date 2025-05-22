package com.btxtech.server.rest.editor;

import com.btxtech.server.rest.AbstractConfigController;
import com.btxtech.server.service.engine.AbstractConfigCrudPersistence;
import com.btxtech.server.service.engine.BoxItemTypeCrudPersistence;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/editor/box_item_type")
public class BoxItemTypeEditorController extends AbstractConfigController<BoxItemType> {
    private final BoxItemTypeCrudPersistence boxItemTypeCrudPersistence;

    public BoxItemTypeEditorController(BoxItemTypeCrudPersistence boxItemTypeCrudPersistence) {
        this.boxItemTypeCrudPersistence = boxItemTypeCrudPersistence;
    }

    @Override
    protected AbstractConfigCrudPersistence<BoxItemType, ?> getConfigCrudPersistence() {
        return boxItemTypeCrudPersistence;
    }
}
