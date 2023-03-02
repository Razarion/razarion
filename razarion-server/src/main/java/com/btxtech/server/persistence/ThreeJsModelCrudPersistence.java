package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.List;

@Singleton
public class ThreeJsModelCrudPersistence extends AbstractCrudPersistence<ThreeJsModelConfig, ThreeJsModelConfigEntity> {
    @PersistenceContext
    private EntityManager entityManager;

    public ThreeJsModelCrudPersistence() {
        super(ThreeJsModelConfigEntity.class, ThreeJsModelConfigEntity_.id, ThreeJsModelConfigEntity_.internalName);
    }

    @Override
    protected ThreeJsModelConfig toConfig(ThreeJsModelConfigEntity entity) {
        return entity.toConfig();
    }

    @Override
    protected void fromConfig(ThreeJsModelConfig config, ThreeJsModelConfigEntity entity) {
        entity.from(config);
    }

    @Transactional
    public void saveData(int id, byte[] bytes) {
        ThreeJsModelConfigEntity threeJsModelConfig = getEntity(id);
        threeJsModelConfig.setData(bytes);
        entityManager.merge(threeJsModelConfig);
    }

    @Transactional
    public byte[] getThreeJsModel(int id) {
        return getEntity(id).getData();
    }

    @Transactional
    public Integer getEntityId4FbxGuidHint(String fbxGuidHint) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> userQuery = criteriaBuilder.createTupleQuery();
        Root<ThreeJsModelConfigEntity> from = userQuery.from(ThreeJsModelConfigEntity.class);
        userQuery.multiselect(from.get(ThreeJsModelConfigEntity_.id));
        userQuery.where(criteriaBuilder.equal(from.get(ThreeJsModelConfigEntity_.fbxGuidHint), fbxGuidHint));

        List<Tuple> tuples = entityManager.createQuery(userQuery).getResultList();
        if (!tuples.isEmpty()) {
            return (Integer) tuples.get(0).get(0);
        } else {
            return null;
        }
    }

}
