package com.btxtech.server.persistence.asset;

import com.btxtech.server.persistence.AbstractCrudPersistence;
import com.btxtech.shared.datatypes.asset.MeshContainer;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Singleton
public class MeshContainerCrudPersistence extends AbstractCrudPersistence<MeshContainer, MeshContainerEntity> {
    public MeshContainerCrudPersistence() {
        super(MeshContainerEntity.class, MeshContainerEntity_.id, MeshContainerEntity_.internalName);
    }

    @Override
    protected MeshContainer toConfig(MeshContainerEntity entity) {
        // Only used for getting the ObjectNames. MeshContainerCrudPersistence is only for select dialog in generic editor
        return entity.toConfig();
    }

    @Override
    protected void fromConfig(MeshContainer config, MeshContainerEntity entity) {
        // Do not use this. MeshContainerCrudPersistence is only for select dialog in generic editor
        throw new UnsupportedOperationException();
    }
}