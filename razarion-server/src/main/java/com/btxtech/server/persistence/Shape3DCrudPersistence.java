package com.btxtech.server.persistence;

import com.btxtech.server.collada.ColladaConverter;
import com.btxtech.shared.datatypes.shape.AnimationTrigger;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.Shape3DConfig;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import com.btxtech.shared.system.ExceptionHandler;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 17.08.2016.
 */
@Singleton
public class Shape3DCrudPersistence extends AbstractCrudPersistence<Shape3DConfig, ColladaEntity> {
    @Inject
    private ImagePersistence imagePersistence;
    @Inject
    private ExceptionHandler exceptionHandler;

    public Shape3DCrudPersistence() {
        super(ColladaEntity.class, ColladaEntity_.id, ColladaEntity_.internalName);
    }

    @Override
    protected Shape3DConfig toConfig(ColladaEntity entity) {
        return entity.toShape3DConfig();
    }

    @Override
    protected void fromConfig(Shape3DConfig config, ColladaEntity entity) {
        try {
            entity.setInternalName(config.getInternalName());
            if (config.getColladaString() != null) {
                ColladaConverter.createShape3DBuilder(config.getColladaString(), entity); // Verification
                entity.setColladaString(config.getColladaString());
            }
            Map<String, ImageLibraryEntity> textures = new HashMap<>();
            Map<String, ImageLibraryEntity> bumpMaps = new HashMap<>();
            Map<String, Double> bumpMapDepts = new HashMap<>();
            Map<String, Boolean> characterRepresentings = new HashMap<>();
            Map<String, Double> alphaToCoverages = new HashMap<>();
            if (config.getShape3DMaterialConfigs() != null) {
                config.getShape3DMaterialConfigs().forEach(shape3DMaterialConfig -> {
                    textures.put(shape3DMaterialConfig.getMaterialId(), imagePersistence.getImageLibraryEntity(shape3DMaterialConfig.getPhongMaterialConfig().getTextureId()));
                    bumpMaps.put(shape3DMaterialConfig.getMaterialId(), imagePersistence.getImageLibraryEntity(shape3DMaterialConfig.getPhongMaterialConfig().getBumpMapId()));
                    bumpMapDepts.put(shape3DMaterialConfig.getMaterialId(), shape3DMaterialConfig.getPhongMaterialConfig().getBumpMapDepth());
                    characterRepresentings.put(shape3DMaterialConfig.getMaterialId(), shape3DMaterialConfig.isCharacterRepresenting());
                    alphaToCoverages.put(shape3DMaterialConfig.getMaterialId(), shape3DMaterialConfig.getAlphaToCoverage());
                });
            }
            entity.setTextures(textures);
            entity.setBumpMaps(bumpMaps);
            entity.setBumpMapDepts(bumpMapDepts);
            entity.setCharacterRepresentings(characterRepresentings);
            entity.setAlphaToCoverages(alphaToCoverages);
            if (config.getAnimations() != null) {
                Map<String, AnimationTrigger> animations = new HashMap<>();
                for (Map.Entry<String, AnimationTrigger> entry : config.getAnimations().entrySet()) {
                    animations.put(entry.getKey(), entry.getValue());
                }
                entity.setAnimations(animations);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Transactional
    public List<Shape3D> getShape3Ds() {
        List<Shape3D> shape3Ds = new ArrayList<>();
        for (ColladaEntity colladaEntity : getEntities()) {
            try {
                shape3Ds.add(ColladaConverter.createShape3DBuilder(colladaEntity.getColladaString(), colladaEntity).createShape3D(colladaEntity.getId()));
            } catch (Exception e) {
                exceptionHandler.handleException(e);
            }
        }
        return shape3Ds;
    }

    @Transactional
    public List<VertexContainerBuffer> getVertexContainerBuffers() throws ParserConfigurationException, SAXException, IOException {
        List<VertexContainerBuffer> vertexContainerBuffers = new ArrayList<>();
        for (ColladaEntity colladaEntity : getEntities()) {
            vertexContainerBuffers.addAll(ColladaConverter.createShape3DBuilder(colladaEntity.getColladaString(), colladaEntity).createVertexContainerBuffer(colladaEntity.getId()));
        }
        return vertexContainerBuffers;
    }
}
