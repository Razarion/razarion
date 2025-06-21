package com.btxtech.server.rest.editor;

import com.btxtech.server.rest.AbstractConfigController;
import com.btxtech.server.service.engine.GroundCrudPersistence;
import com.btxtech.shared.dto.GroundConfig;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/editor/ground")
public class GroundEditorController extends AbstractConfigController<GroundConfig> {
    private final GroundCrudPersistence groundCrudPersistence;

    public GroundEditorController(GroundCrudPersistence groundCrudPersistence) {
        this.groundCrudPersistence = groundCrudPersistence;
    }

    @Override
    protected GroundCrudPersistence getConfigCrudPersistence() {
        return groundCrudPersistence;
    }
}
