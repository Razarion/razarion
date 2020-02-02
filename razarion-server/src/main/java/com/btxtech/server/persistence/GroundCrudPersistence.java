package com.btxtech.server.persistence;

import com.btxtech.server.persistence.surface.GroundConfigEntity;
import com.btxtech.server.persistence.surface.GroundConfigEntity_;
import com.btxtech.shared.dto.GroundConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

@Singleton
public class GroundCrudPersistence extends CrudPersistence<GroundConfig, GroundConfigEntity> {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private ImagePersistence imagePersistence;

    public GroundCrudPersistence() {
        super(GroundConfigEntity.class, GroundConfigEntity_.id, GroundConfigEntity_.internalName, GroundConfig::getId);
    }

    @Override
    protected GroundConfig toConfig(GroundConfigEntity entity) {
        return entity.toGroundConfig();
    }

    @Override
    protected void fromConfig(GroundConfig config, GroundConfigEntity entity) {
        entity.fromGroundConfig(config, imagePersistence);
    }

    @Transactional
    public GroundConfig getDefaultGround() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GroundConfigEntity> userQuery = criteriaBuilder.createQuery(GroundConfigEntity.class);
        Root<GroundConfigEntity> from = userQuery.from(GroundConfigEntity.class);
        CriteriaQuery<GroundConfigEntity> userSelect = userQuery.select(from);
        return toConfig(entityManager.createQuery(userSelect).getSingleResult());
    }
}
