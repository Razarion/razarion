package com.btxtech.server.persistence;

import com.btxtech.shared.dto.ClipConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 15.10.2016.
 */
@Singleton
public class ClipPersistence {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private Shape3DPersistence shape3DPersistence;

    @Transactional
    public List<ClipConfig> readClipConfigs() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // Query for total row count in invitations
        CriteriaQuery<ClipEntity> userQuery = criteriaBuilder.createQuery(ClipEntity.class);
        Root<ClipEntity> from = userQuery.from(ClipEntity.class);
        CriteriaQuery<ClipEntity> userSelect = userQuery.select(from);
        return entityManager.createQuery(userSelect).getResultList().stream().map(ClipEntity::toClipConfig).collect(Collectors.toList());
    }

    @Transactional
    public ClipConfig create() {
        ClipEntity clipEntity = new ClipEntity();
        entityManager.persist(clipEntity);
        return clipEntity.toClipConfig();
    }

    @Transactional
    public void update(ClipConfig clipConfig) {
        ClipEntity clipEntity = entityManager.find(ClipEntity.class, (long) clipConfig.getId());
        clipEntity.fromClipConfig(clipConfig);
        clipEntity.setShape3D(shape3DPersistence.getColladaEntity(clipConfig.getShape3DId()));
        entityManager.merge(clipEntity);
    }

    @Transactional
    public void delete(int id) {
        entityManager.remove(entityManager.find(ClipEntity.class, (long) id));
    }
}
