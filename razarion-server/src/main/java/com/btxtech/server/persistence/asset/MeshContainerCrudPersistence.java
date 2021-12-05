package com.btxtech.server.persistence.asset;

import com.btxtech.server.persistence.AbstractCrudPersistence;
import com.btxtech.server.persistence.Shape3DCrudPersistence;
import com.btxtech.shared.datatypes.asset.AssetConfig;
import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.unityconverter.Converter;
import com.btxtech.unityconverter.unity.asset.AssetReader;
import com.btxtech.unityconverter.unity.asset.UnityAsset;
import com.btxtech.unityconverter.unity.asset.type.Prefab;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Singleton
public class MeshContainerCrudPersistence extends AbstractCrudPersistence<MeshContainer, MeshContainerEntity> {
    @Inject
    private Shape3DCrudPersistence shape3DCrudPersistence;

    public MeshContainerCrudPersistence() {
        super(MeshContainerEntity.class, MeshContainerEntity_.id, MeshContainerEntity_.internalName);
    }

    @Override
    protected MeshContainer toConfig(MeshContainerEntity entity) {
        return entity.toConfig();
    }

    @Override
    protected void fromConfig(MeshContainer config, MeshContainerEntity entity) {
        entity.fromConfig(config, null, shape3DCrudPersistence);
    }
}