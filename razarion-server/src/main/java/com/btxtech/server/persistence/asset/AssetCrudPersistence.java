package com.btxtech.server.persistence.asset;

import com.btxtech.server.persistence.AbstractConfigCrudPersistence;
import com.btxtech.server.persistence.ThreeJsModelCrudPersistence;
import com.btxtech.shared.datatypes.asset.AssetConfig;
import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.unityconverter.UnityAssetConverter;

import javax.inject.Inject;
import javax.inject.Singleton;
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
public class AssetCrudPersistence extends AbstractConfigCrudPersistence<AssetConfig, AssetConfigEntity> {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ThreeJsModelCrudPersistence threeJsModelCrudPersistence;

    public AssetCrudPersistence() {
        super(AssetConfigEntity.class, AssetConfigEntity_.id, AssetConfigEntity_.internalName);
    }

    @Override
    protected AssetConfig toConfig(AssetConfigEntity entity) {
        return entity.toConfig();
    }

    @Override
    protected void fromConfig(AssetConfig config, AssetConfigEntity entity) {
        try {
            if (config.getAssetMetaFileHint() == null) {
                throw new IllegalArgumentException("Asset meta file hint is not set");
            }
            config = UnityAssetConverter.createAssetConfig(config.getAssetMetaFileHint(), new ServerAssetContext(threeJsModelCrudPersistence));

            entity.setAssetMetaFileHint(config.getAssetMetaFileHint());
            entity.setUnityAssetGuid(config.getUnityAssetGuid());
            entity.setInternalName(config.getInternalName());
            entity.setMeshContainers(toMeshContainers(entity.getMeshContainers(), config.getMeshContainers()));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    public List<MeshContainer> readMeshContainers() {
        return read()
                .stream()
                .flatMap(assetConfig -> assetConfig.getMeshContainers().stream())
                .collect(Collectors.toList());
    }

    private List<MeshContainerEntity> toMeshContainers(List<MeshContainerEntity> entityList, List<MeshContainer> configList) {
        if (entityList == null) {
            entityList = new ArrayList<>();
        }
        if (configList == null) {
            entityList.clear();
            return entityList;
        }
        Set<MeshContainerEntity> unusedSet = new HashSet<>(entityList);
        // Update existing and update new
        for (MeshContainer meshContainer : configList) {
            MeshContainerEntity entity = entityList.stream()
                    .filter(e -> e.getGuid().equals(meshContainer.getGuid()))
                    .findFirst()
                    .orElse(null);
            if (entity != null) {
                unusedSet.remove(entity);
            } else {
                entity = new MeshContainerEntity();
                entityList.add(entity);
            }
            entity.fromConfig(meshContainer, null, threeJsModelCrudPersistence);
        }
        // Remove unused
        entityList.removeIf(unusedSet::contains);
        return entityList;
    }
}