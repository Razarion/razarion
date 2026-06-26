package com.btxtech.server.rest.editor;

import com.btxtech.server.model.ui.ScatterBrushEntity;
import com.btxtech.server.rest.AbstractBaseController;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.ui.ScatterBrushService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/editor/scatter-brush")
public class ScatterBrushController extends AbstractBaseController<ScatterBrushEntity> {
    private final ScatterBrushService scatterBrushService;

    public ScatterBrushController(ScatterBrushService scatterBrushService) {
        this.scatterBrushService = scatterBrushService;
    }

    @Override
    protected AbstractBaseEntityCrudService<ScatterBrushEntity> getBaseEntityCrudService() {
        return scatterBrushService;
    }
}
