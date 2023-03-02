package com.btxtech.server.persistence.asset;

import com.btxtech.server.persistence.ThreeJsModelCrudPersistence;
import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;
import com.btxtech.unityconverter.AssetContext;
import com.btxtech.unityconverter.MaterialInfo;
import com.btxtech.unityconverter.unity.asset.type.Fbx;

import java.util.HashMap;
import java.util.Map;

import static com.btxtech.shared.datatypes.shape.ThreeJsModelConfig.Type.GLTF;

public class ServerAssetContext implements AssetContext {
    private final Map<String, Integer> fbxGuid2ThreeJsModelId4 = new HashMap<>();
    private final ThreeJsModelCrudPersistence threeJsModelCrudPersistence;

    public ServerAssetContext(ThreeJsModelCrudPersistence threeJsModelCrudPersistence) {
        this.threeJsModelCrudPersistence = threeJsModelCrudPersistence;
    }

    @Override
    public Integer getThreeJsModelId4Fbx(Fbx fbx, MaterialInfo materialInfo, String assetName) {
        String fbxGuidHint = fbx.getGuid();
        Integer threeJsModelId = fbxGuid2ThreeJsModelId4.get(fbxGuidHint);
        if (threeJsModelId != null) {
            return threeJsModelId;
        }
        threeJsModelId = threeJsModelCrudPersistence.getEntityId4FbxGuidHint(fbxGuidHint);
        if (threeJsModelId != null) {
            fbxGuid2ThreeJsModelId4.put(fbxGuidHint, threeJsModelId);
            return threeJsModelId;
        }
        threeJsModelId = generateThreeJsModel(fbx, assetName);
        fbxGuid2ThreeJsModelId4.put(fbxGuidHint, threeJsModelId);
        return threeJsModelId;
    }

    private Integer generateThreeJsModel(Fbx fbx, String assetName) {
        try {
            ThreeJsModelConfig threeJsModelConfig = threeJsModelCrudPersistence.create()
                    .internalName("[" + assetName + "] " + fbx.getName())
                    .fbxGuidHint(fbx.getGuid())
                    .type(GLTF);
            threeJsModelCrudPersistence.update(threeJsModelConfig);
            return threeJsModelConfig.getId();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
