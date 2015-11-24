package com.btxtech.server.terrain;

import com.btxtech.client.TerrainEditorService;
import com.btxtech.server.ExceptionHandler;
import com.btxtech.shared.PlateauConfigEntity;
import org.jboss.errai.bus.server.annotations.Service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
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
    public PlateauConfigEntity read() {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            // Query for total row count in invitations
            CriteriaQuery<PlateauConfigEntity> userQuery = criteriaBuilder.createQuery(PlateauConfigEntity.class);
            Root<PlateauConfigEntity> from = userQuery.from(PlateauConfigEntity.class);
            CriteriaQuery<PlateauConfigEntity> userSelect = userQuery.select(from);
            return entityManager.createQuery(userSelect).getSingleResult();
        } catch (RuntimeException e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void save(PlateauConfigEntity plateauConfigEntity) {
        try {
            entityManager.merge(plateauConfigEntity);
        } catch (RuntimeException e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
