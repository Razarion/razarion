package com.btxtech.server.service.ui;

import com.btxtech.server.model.ui.ScatterBrushEntity;
import com.btxtech.server.repository.ui.ScatterBrushRepository;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import org.springframework.stereotype.Service;

@Service
public class ScatterBrushService extends AbstractBaseEntityCrudService<ScatterBrushEntity> {

    public ScatterBrushService(ScatterBrushRepository scatterBrushRepository) {
        super(ScatterBrushEntity.class, scatterBrushRepository);
    }
}
