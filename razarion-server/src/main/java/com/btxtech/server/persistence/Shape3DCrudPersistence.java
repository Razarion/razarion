package com.btxtech.server.persistence;

import com.btxtech.server.collada.ColladaConverter;
import com.btxtech.shared.datatypes.shape.AnimationTrigger;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;
import com.btxtech.shared.system.ExceptionHandler;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
public class Shape3DCrudPersistence extends AbstractConfigCrudPersistence<Shape3DConfig, ColladaEntity> {
    @Inject
    private ImagePersistence imagePersistence;
    @Inject
    private ExceptionHandler exceptionHandler;
    @PersistenceContext
    private EntityManager entityManager;

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
                ColladaConverter.createShape3DBuilder(config.getColladaString(), entity, null); // Verification
                entity.setColladaString(config.getColladaString());
            }
            Map<String, AnimationTrigger> animations = new HashMap<>();
            List<ColladaMaterialEntity> colladaMaterialEntities = new ArrayList<>();
            if (config.getShape3DElementConfigs() != null) {
                config.getShape3DElementConfigs().forEach(shape3DElementConfig -> {
                    shape3DElementConfig.getVertexContainerMaterialConfigs().forEach(vertexContainerMaterialConfig -> {
                        colladaMaterialEntities.add(new ColladaMaterialEntity().from(vertexContainerMaterialConfig, imagePersistence));
                    });
                    if (shape3DElementConfig.getShape3DAnimationTriggerConfigs() != null) {
                        shape3DElementConfig.getShape3DAnimationTriggerConfigs().forEach(shape3DAnimationTriggerConfig -> {
                            animations.put(shape3DAnimationTriggerConfig.getDescription(), shape3DAnimationTriggerConfig.getAnimationTrigger());
                        });
                    }
                });
            }
            entity.setColladaMaterials(colladaMaterialEntities);
            entity.setAnimations(animations);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Transactional
    public List<VertexContainerBuffer> getVertexContainerBuffers() throws ParserConfigurationException, SAXException, IOException {
        List<VertexContainerBuffer> vertexContainerBuffers = new ArrayList<>();
        for (ColladaEntity colladaEntity : getEntities()) {
            vertexContainerBuffers.addAll(ColladaConverter.createShape3DBuilder(colladaEntity.getColladaString(), colladaEntity, null).createVertexContainerBuffer(colladaEntity.getId()));
        }
        return vertexContainerBuffers;
    }
}
