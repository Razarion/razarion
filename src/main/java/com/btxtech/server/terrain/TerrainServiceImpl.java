package com.btxtech.server.terrain;

import com.btxtech.server.ExceptionHandler;
import com.btxtech.shared.GroundSkeletonEntity;
import com.btxtech.shared.SlopeSkeletonEntity;
import com.btxtech.shared.TerrainService;
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

/**
 * Created by Beat
 * 24.04.2016.
 */
@Service
@ApplicationScoped
public class TerrainServiceImpl implements TerrainService {
    @Inject
    private ExceptionHandler exceptionHandler;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Collection<SlopeSkeletonEntity> loadSlopeSkeleton() {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<SlopeSkeletonEntity> userQuery = criteriaBuilder.createQuery(SlopeSkeletonEntity.class);
            Root<SlopeSkeletonEntity> root = userQuery.from(SlopeSkeletonEntity.class);
            CriteriaQuery<SlopeSkeletonEntity> userSelect = userQuery.select(root);
            return entityManager.createQuery(userSelect).getResultList();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public GroundSkeletonEntity loadGroundSkeleton() {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            // Query for total row count in invitations
            CriteriaQuery<GroundSkeletonEntity> userQuery = criteriaBuilder.createQuery(GroundSkeletonEntity.class);
            Root<GroundSkeletonEntity> from = userQuery.from(GroundSkeletonEntity.class);
            CriteriaQuery<GroundSkeletonEntity> userSelect = userQuery.select(from);
            return entityManager.createQuery(userSelect).getSingleResult();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
