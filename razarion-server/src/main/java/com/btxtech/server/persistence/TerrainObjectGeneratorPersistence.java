package com.btxtech.server.persistence;

import com.btxtech.server.persistence.ui.TerrainObjectGeneratorEntity;

import javax.inject.Singleton;

@Singleton
public class TerrainObjectGeneratorPersistence extends AbstractEntityCrudPersistence<TerrainObjectGeneratorEntity> {
    public TerrainObjectGeneratorPersistence() {
        super(TerrainObjectGeneratorEntity.class);
    }
}
