package com.btxtech.server.persistence;

import com.btxtech.server.persistence.ui.Model3DEntity;
import com.btxtech.server.persistence.ui.Model3DEntity_;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
public class Model3DCrudPersistence extends AbstractEntityCrudPersistence<Model3DEntity> {
    @PersistenceContext
    private EntityManager entityManager;

    public Model3DCrudPersistence() {
        super(Model3DEntity.class);
    }

    public List<Model3DEntity> getModel3DsByGltf(int gltfId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Model3DEntity> userQuery = criteriaBuilder.createQuery(Model3DEntity.class);
        Root<Model3DEntity> from = userQuery.from(Model3DEntity.class);
        userQuery.where(criteriaBuilder.equal(from.get(Model3DEntity_.gltfEntity), gltfId));
        CriteriaQuery<Model3DEntity> userSelect = userQuery.select(from);
        return entityManager.createQuery(userSelect).getResultList();
    }
}
