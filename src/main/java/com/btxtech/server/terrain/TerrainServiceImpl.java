package com.btxtech.server.terrain;

import com.btxtech.server.ExceptionHandler;
import com.btxtech.shared.SlopeSkeletonEntity;
import com.btxtech.shared.TerrainService;
import com.btxtech.shared.dto.GroundSkeleton;
import com.google.gson.Gson;
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
            Collection<SlopeSkeletonEntity> groundSkeletonEntitys = entityManager.createQuery(userSelect).getResultList();

            Gson gson = new Gson();
            for (SlopeSkeletonEntity groundSkeletonEntity : groundSkeletonEntitys) {
                System.out.println("--------------------------------------------------------");
                String json = gson.toJson(groundSkeletonEntity);
                System.out.println(json);
                System.out.println("--------------------------------------------------------");
            }

            return groundSkeletonEntitys;
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public GroundSkeleton loadGroundSkeleton() {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            // Query for total row count in invitations
            CriteriaQuery<GroundConfigEntity> userQuery = criteriaBuilder.createQuery(GroundConfigEntity.class);
            Root<GroundConfigEntity> from = userQuery.from(GroundConfigEntity.class);
            CriteriaQuery<GroundConfigEntity> userSelect = userQuery.select(from);
            GroundConfigEntity groundSkeletonEntity = entityManager.createQuery(userSelect).getSingleResult();
            return groundSkeletonEntity.generateGroundSkeleton();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
