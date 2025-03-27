package com.btxtech.server.service.ui;

import com.btxtech.server.model.ui.BrushConfigEntity;
import com.btxtech.server.repository.ui.BrushConfigRepository;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class BrushConfigService extends AbstractBaseEntityCrudService<BrushConfigEntity> {
    private final BrushConfigRepository brushConfigRepository;

    public BrushConfigService(BrushConfigRepository brushConfigRepository) {
        super(BrushConfigEntity.class);
        this.brushConfigRepository = brushConfigRepository;
    }

    @Override
    protected JpaRepository<BrushConfigEntity, Integer> getJpaRepository() {
        return brushConfigRepository;
    }
}
