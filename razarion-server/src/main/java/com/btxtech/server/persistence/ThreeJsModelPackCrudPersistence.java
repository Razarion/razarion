package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.shape.ThreeJsModelPackConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class ThreeJsModelPackCrudPersistence extends AbstractConfigCrudPersistence<ThreeJsModelPackConfig, ThreeJsModelPackConfigEntity> {
    @Inject
    private ThreeJsModelCrudPersistence threeJsModelCrudPersistence;
    @PersistenceContext
    private EntityManager entityManager;

    public ThreeJsModelPackCrudPersistence() {
        super(ThreeJsModelPackConfigEntity.class, ThreeJsModelPackConfigEntity_.id, ThreeJsModelPackConfigEntity_.internalName);
    }

    @Override
    protected ThreeJsModelPackConfig toConfig(ThreeJsModelPackConfigEntity entity) {
        return entity.toConfig();
    }

    @Override
    protected void fromConfig(ThreeJsModelPackConfig config, ThreeJsModelPackConfigEntity entity) {
        entity.from(config, threeJsModelCrudPersistence);
    }

    @Transactional
    public List<ThreeJsModelPackConfig> findByThreeJsModelId(int threeJsModelId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ThreeJsModelPackConfigEntity> userQuery = criteriaBuilder.createQuery(ThreeJsModelPackConfigEntity.class);
        Root<ThreeJsModelPackConfigEntity> root = userQuery.from(ThreeJsModelPackConfigEntity.class);
        userQuery.where(criteriaBuilder.equal(root.get(ThreeJsModelPackConfigEntity_.threeJsModelConfig), threeJsModelId));
        Collection<ThreeJsModelPackConfigEntity> threeJsModelPackConfigEntities = entityManager.createQuery(userQuery).getResultList();
        return threeJsModelPackConfigEntities.stream().map(this::toConfig).collect(Collectors.toList());
    }
}
