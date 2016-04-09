package com.btxtech.server.terrain;

import com.btxtech.shared.SlopeConfigEntity;
import com.btxtech.shared.TerrainEditorService;
import com.btxtech.server.ExceptionHandler;
import com.btxtech.shared.TerrainMeshVertex;
import org.jboss.errai.bus.server.annotations.Service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 20.11.2015.
 */
@Service
@ApplicationScoped
public class TerrainEditorServiceImpl implements TerrainEditorService {
    @Inject
    private Logger logger;
    @Inject
    private ExceptionHandler exceptionHandler;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public SlopeConfigEntity read() {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            // Query for total row count in invitations
            CriteriaQuery<SlopeConfigEntity> userQuery = criteriaBuilder.createQuery(SlopeConfigEntity.class);
            Root<SlopeConfigEntity> from = userQuery.from(SlopeConfigEntity.class);
            CriteriaQuery<SlopeConfigEntity> userSelect = userQuery.select(from);
            SlopeConfigEntity plateauConfigEntity =  entityManager.createQuery(userSelect).getSingleResult();
            logger.severe("plateauConfigEntity: " + plateauConfigEntity.getShape());
            return plateauConfigEntity;
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void save(SlopeConfigEntity plateauConfigEntity) {
        try {
            entityManager.merge(plateauConfigEntity);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public Collection<TerrainMeshVertex> readTerrainMeshVertices() {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            // Query for total row count in invitations
            CriteriaQuery<TerrainMeshVertex> userQuery = criteriaBuilder.createQuery(TerrainMeshVertex.class);
            Root<TerrainMeshVertex> from = userQuery.from(TerrainMeshVertex.class);
            CriteriaQuery<TerrainMeshVertex> userSelect = userQuery.select(from);
            return entityManager.createQuery(userSelect).getResultList();
        } catch (RuntimeException e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void saveTerrainMeshVertices(Collection<TerrainMeshVertex> terrainMeshVertexes) {
        try {
            for (TerrainMeshVertex terrainMeshVertex : terrainMeshVertexes) {
                entityManager.merge(terrainMeshVertex);
            }
        } catch (RuntimeException e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

}
