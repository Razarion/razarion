package com.btxtech.server.service.ui;

import com.btxtech.server.model.ui.BrushConfigEntity;
import com.btxtech.server.repository.ui.BrushConfigRepository;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class BrushConfigService extends AbstractBaseEntityCrudService<BrushConfigEntity> {
    @Autowired
    private BrushConfigRepository brushConfigRepository;

    public BrushConfigService() {
        super(BrushConfigEntity.class);
    }

    @Override
    protected JpaRepository<BrushConfigEntity, Integer> getJpaRepository() {
        return brushConfigRepository;
    }
}
