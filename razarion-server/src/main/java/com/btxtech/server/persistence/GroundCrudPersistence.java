package com.btxtech.server.persistence;

import com.btxtech.server.persistence.surface.GroundConfigEntity;
import com.btxtech.server.persistence.surface.GroundConfigEntity_;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.GroundSkeletonConfig;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

@Singleton
public class GroundCrudPersistence extends CrudPersistence<GroundSkeletonConfig, GroundConfigEntity> {
    @PersistenceContext
    private EntityManager entityManager;

    public GroundCrudPersistence() {
        super(GroundConfigEntity.class, GroundConfigEntity_.id, GroundConfigEntity_.internalName, GroundSkeletonConfig::getId);
    }

    @Override
    protected GroundSkeletonConfig toConfig(GroundConfigEntity entity) {
        return entity.generateGroundSkeleton();
    }

    @Override
    protected void fromConfig(GroundSkeletonConfig config, GroundConfigEntity entity) {
        entity.fromGroundConfig(new GroundConfig(), null);
    }

    @Transactional
    public GroundSkeletonConfig getDefaultGround() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GroundConfigEntity> userQuery = criteriaBuilder.createQuery(GroundConfigEntity.class);
        Root<GroundConfigEntity> from = userQuery.from(GroundConfigEntity.class);
        CriteriaQuery<GroundConfigEntity> userSelect = userQuery.select(from);
       return toConfig(entityManager.createQuery(userSelect).getSingleResult());
    }
}
