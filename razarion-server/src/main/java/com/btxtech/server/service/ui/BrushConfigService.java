package com.btxtech.server.service.ui;

import com.btxtech.server.model.ui.BrushConfigEntity;
import com.btxtech.server.repository.ui.BrushConfigRepository;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import org.springframework.stereotype.Service;

@Service
public class BrushConfigService extends AbstractBaseEntityCrudService<BrushConfigEntity> {

    public BrushConfigService(BrushConfigRepository brushConfigRepository) {
        super(BrushConfigEntity.class, brushConfigRepository);
    }
}
