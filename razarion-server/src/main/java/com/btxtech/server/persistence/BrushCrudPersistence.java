package com.btxtech.server.persistence;

import com.btxtech.server.persistence.ui.BrushConfigEntity;

import javax.inject.Singleton;

@Singleton
public class BrushCrudPersistence extends AbstractEntityCrudPersistence<BrushConfigEntity> {
    public BrushCrudPersistence() {
        super(BrushConfigEntity.class);
    }
}
