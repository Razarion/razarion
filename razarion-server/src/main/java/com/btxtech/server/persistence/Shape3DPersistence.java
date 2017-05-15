package com.btxtech.server.persistence;

import com.btxtech.server.collada.ColladaConverter;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.datatypes.shape.AnimationTrigger;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.Shape3DConfig;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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
public class Shape3DPersistence {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private ImagePersistence imagePersistence;

    @Transactional
    public List<ColladaEntity> readColladaEntities() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ColladaEntity> userQuery = criteriaBuilder.createQuery(ColladaEntity.class);
        Root<ColladaEntity> root = userQuery.from(ColladaEntity.class);
        CriteriaQuery<ColladaEntity> userSelect = userQuery.select(root);
        return entityManager.createQuery(userSelect).getResultList();
    }

    @Transactional
    public List<Shape3D> getShape3Ds() throws ParserConfigurationException, SAXException, IOException {
        List<Shape3D> shape3Ds = new ArrayList<>();
        for (ColladaEntity colladaEntity : readColladaEntities()) {
            shape3Ds.add(ColladaConverter.createShape3DBuilder(colladaEntity.getColladaString(), colladaEntity).createShape3D(colladaEntity.getId()));
        }
        return shape3Ds;
    }

    @Transactional
    public List<VertexContainerBuffer> getVertexContainerBuffers() throws ParserConfigurationException, SAXException, IOException {
        List<VertexContainerBuffer> vertexContainerBuffers = new ArrayList<>();
        for (ColladaEntity colladaEntity : readColladaEntities()) {
            vertexContainerBuffers.addAll(ColladaConverter.createShape3DBuilder(colladaEntity.getColladaString(), colladaEntity).createVertexContainerBuffer(colladaEntity.getId()));
        }
        return vertexContainerBuffers;
    }

    @Transactional
    @SecurityCheck
    public Shape3D create() throws ParserConfigurationException, SAXException, IOException {
        ColladaEntity colladaEntity = new ColladaEntity();
        entityManager.persist(colladaEntity);
        return new Shape3D().setDbId(colladaEntity.getId());
    }

    @Transactional
    public ColladaEntity getColladaEntity(Integer colladaId) {
        if (colladaId != null) {
            return entityManager.find(ColladaEntity.class, colladaId);
        } else {
            return null;
        }
    }

    @Transactional
    @SecurityCheck
    public void save(Shape3DConfig shape3DConfig) throws ParserConfigurationException, SAXException, IOException {
        ColladaEntity colladaEntity = entityManager.find(ColladaEntity.class, shape3DConfig.getDbId());
        if (shape3DConfig.getColladaString() != null) {
            ColladaConverter.createShape3DBuilder(shape3DConfig.getColladaString(), null); // Verification
            colladaEntity.setColladaString(shape3DConfig.getColladaString());
        }
        if (shape3DConfig.getTextures() != null) {
            Map<String, ImageLibraryEntity> imageLibraryEntityMap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : shape3DConfig.getTextures().entrySet()) {
                imageLibraryEntityMap.put(entry.getKey(), imagePersistence.getImageLibraryEntity(entry.getValue()));
            }
            colladaEntity.setTextures(imageLibraryEntityMap);
        }
        if (shape3DConfig.getAnimations() != null) {
            Map<String, AnimationTrigger> animations = new HashMap<>();
            for (Map.Entry<String, AnimationTrigger> entry : shape3DConfig.getAnimations().entrySet()) {
                animations.put(entry.getKey(), entry.getValue());
            }
            colladaEntity.setAnimations(animations);
        }
        entityManager.merge(colladaEntity);
    }

    @Transactional
    @SecurityCheck
    public void delete(int id) {
        entityManager.remove(entityManager.find(ColladaEntity.class, id));
    }
}
