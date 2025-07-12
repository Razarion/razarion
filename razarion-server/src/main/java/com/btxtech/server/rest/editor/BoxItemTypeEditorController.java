package com.btxtech.server.rest.editor;

import com.btxtech.server.rest.AbstractConfigController;
import com.btxtech.server.service.engine.AbstractConfigCrudService;
import com.btxtech.server.service.engine.BoxItemTypeCrudService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/editor/box_item_type")
public class BoxItemTypeEditorController extends AbstractConfigController<BoxItemType> {
    private final BoxItemTypeCrudService boxItemTypeCrudService;

    public BoxItemTypeEditorController(BoxItemTypeCrudService boxItemTypeCrudService) {
        this.boxItemTypeCrudService = boxItemTypeCrudService;
    }

    @Override
    protected AbstractConfigCrudService<BoxItemType, ?> getConfigCrudService() {
        return boxItemTypeCrudService;
    }
}
