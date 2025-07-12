package com.btxtech.server.rest.editor;

import com.btxtech.server.rest.AbstractConfigController;
import com.btxtech.server.service.engine.GroundCrudService;
import com.btxtech.shared.dto.GroundConfig;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/editor/ground")
public class GroundEditorController extends AbstractConfigController<GroundConfig> {
    private final GroundCrudService groundCrudService;

    public GroundEditorController(GroundCrudService groundCrudService) {
        this.groundCrudService = groundCrudService;
    }

    @Override
    protected GroundCrudService getConfigCrudService() {
        return groundCrudService;
    }
}
