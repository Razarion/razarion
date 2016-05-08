package com.btxtech.server.terrain;

import com.btxtech.server.ExceptionHandler;
import com.btxtech.shared.TerrainService;
import com.btxtech.shared.dto.GroundSkeleton;
import com.btxtech.shared.dto.SlopeSkeleton;
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
import java.util.ArrayList;
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
    public Collection<SlopeSkeleton> loadSlopeSkeletons() {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<SlopeConfigEntity> userQuery = criteriaBuilder.createQuery(SlopeConfigEntity.class);
            Root<SlopeConfigEntity> root = userQuery.from(SlopeConfigEntity.class);
            CriteriaQuery<SlopeConfigEntity> userSelect = userQuery.select(root);
            Collection<SlopeConfigEntity> slopeConfigEntities = entityManager.createQuery(userSelect).getResultList();

            Gson gson = new Gson();
            Collection<SlopeSkeleton> slopeSkeletons = new ArrayList<>();
            for (SlopeConfigEntity slopeConfigEntity : slopeConfigEntities) {
                SlopeSkeleton slopeSkeleton = slopeConfigEntity.toSlopeSkeleton();
                slopeSkeletons.add(slopeSkeleton);
                System.out.println("--------------------------------------------------------");
                String json = gson.toJson(slopeSkeleton);
                System.out.println(json);
                System.out.println("--------------------------------------------------------");
            }

            return slopeSkeletons;
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
