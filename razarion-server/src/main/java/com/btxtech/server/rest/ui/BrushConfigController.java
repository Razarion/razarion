package com.btxtech.server.rest.ui;

import com.btxtech.server.model.ui.BrushConfigEntity;
import com.btxtech.server.rest.AbstractBaseController;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.ui.BrushConfigService;
import com.btxtech.shared.CommonUrl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(CommonUrl.BRUSH_EDITOR_PATH)
public class BrushConfigController extends AbstractBaseController<BrushConfigEntity> {
    private final BrushConfigService persistenceService;

    public BrushConfigController(BrushConfigService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @Override
    protected AbstractBaseEntityCrudService<BrushConfigEntity> getEntityCrudPersistence() {
        return persistenceService;
    }
}
