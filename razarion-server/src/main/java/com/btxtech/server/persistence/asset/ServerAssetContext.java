package com.btxtech.server.persistence.asset;

import com.btxtech.server.collada.ColladaConverter;
import com.btxtech.server.collada.ColladaConverterMapper;
import com.btxtech.server.persistence.Shape3DCrudPersistence;
import com.btxtech.shared.datatypes.shape.AnimationTrigger;
import com.btxtech.shared.datatypes.shape.VertexContainerMaterial;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;
import com.btxtech.unityconverter.AssetContext;
import com.btxtech.unityconverter.unity.asset.type.Fbx;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ServerAssetContext implements AssetContext {
    private final Map<String, Integer> fbxGuid2Shape3DIds = new HashMap<>();
    private final Shape3DCrudPersistence shape3DCrudPersistence;

    public ServerAssetContext(Shape3DCrudPersistence shape3DCrudPersistence) {
        this.shape3DCrudPersistence = shape3DCrudPersistence;
    }

    @Override
    public Integer getShape3DId4Fbx(Fbx fbx) {
        String fbxGuidHint = fbx.getGuid();
        Integer shape3DId = fbxGuid2Shape3DIds.get(fbxGuidHint);
        if (shape3DId != null) {
            return shape3DId;
        }
        shape3DId = shape3DCrudPersistence.getColladaEntityId4InternalName(fbxGuidHint);
        if (shape3DId != null) {
            fbxGuid2Shape3DIds.put(fbxGuidHint, shape3DId);
            return shape3DId;
        }
        shape3DId = generateShape3D(fbx);
        fbxGuid2Shape3DIds.put(fbxGuidHint, shape3DId);
        return shape3DId;
    }

    private Integer generateShape3D(Fbx fbx) {
        try {
            StringBuilder contentBuilder = new StringBuilder();

            try (Stream<String> stream = Files.lines(Paths.get(fbx.getColladaFile().toURI()), StandardCharsets.UTF_8)) {
                stream.forEach(s -> contentBuilder.append(s).append("\n"));
            } catch (IOException e) {
                throw new RuntimeException("Error generate Shape3D for FBX: " + fbx.getAssetFile(), e);
            }
            String colladaText = contentBuilder.toString();
            Shape3DConfig shape3DConfig = shape3DCrudPersistence.create();
            shape3DConfig.setColladaString(colladaText);
            shape3DConfig.internalName(fbx.getGuid());
            shape3DConfig.setShape3DElementConfigs(ColladaConverter.createShape3DBuilder(
                    colladaText,
                    new AssetColladaConverterMapper(), null).createShape3DConfig(shape3DConfig.getId()).getShape3DElementConfigs());
            shape3DCrudPersistence.update(shape3DConfig);
            return shape3DConfig.getId();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static class AssetColladaConverterMapper implements ColladaConverterMapper {
        @Override
        public VertexContainerMaterial toVertexContainerMaterial(String materialId) {
            return null;
        }

        @Override
        public AnimationTrigger getAnimationTrigger(String animationId) {
            return null;
        }
    }

}