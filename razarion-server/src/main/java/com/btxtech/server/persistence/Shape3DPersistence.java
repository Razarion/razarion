package com.btxtech.server.persistence;

import com.btxtech.servercommon.collada.ColladaConverter;
import com.btxtech.shared.datatypes.shape.Shape3D;
import org.xml.sax.SAXException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 17.08.2016.
 */
public class Shape3DPersistence {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public List<Shape3D> getShape3Ds() throws ParserConfigurationException, SAXException, IOException {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ColladaEntity> userQuery = criteriaBuilder.createQuery(ColladaEntity.class);
        Root<ColladaEntity> root = userQuery.from(ColladaEntity.class);
        CriteriaQuery<ColladaEntity> userSelect = userQuery.select(root);

        List<Shape3D> shape3Ds = new ArrayList<>();
        for (ColladaEntity slopeConfigEntity : entityManager.createQuery(userSelect).getResultList()) {
            shape3Ds.add(ColladaConverter.convertShape3D(slopeConfigEntity.getColladaString(), null).setDbId(slopeConfigEntity.getId().intValue()));
        }

        return shape3Ds;
    }

    @Transactional
    public void create(String colladaString) throws ParserConfigurationException, SAXException, IOException {
        ColladaConverter.convertShape3D(colladaString, null); // Verification
        ColladaEntity colladaEntity = new ColladaEntity();
        colladaEntity.setColladaString(colladaString);
        entityManager.persist(colladaEntity);
    }

    @Transactional
    public ColladaEntity getColladaEntity(Integer colladaId) {
        if (colladaId != null) {
            return entityManager.find(ColladaEntity.class, colladaId.longValue());
        } else {
            return null;
        }
    }
}
