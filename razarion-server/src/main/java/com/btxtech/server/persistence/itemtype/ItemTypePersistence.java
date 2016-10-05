package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.Shape3DPersistence;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.system.ExceptionHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 15.08.2015.
 */
@ApplicationScoped
public class ItemTypePersistence {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private Shape3DPersistence shape3DPersistence;

    @Transactional
    public BaseItemType createBaseItemType() {
        BaseItemTypeEntity baseItemTypeEntity = new BaseItemTypeEntity();
        entityManager.persist(baseItemTypeEntity);
        return baseItemTypeEntity.toBaseItemType();
    }

    @Transactional
    public List<BaseItemType> readBaseItemType() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<BaseItemTypeEntity> userQuery = criteriaBuilder.createQuery(BaseItemTypeEntity.class);
        Root<BaseItemTypeEntity> from = userQuery.from(BaseItemTypeEntity.class);
        CriteriaQuery<BaseItemTypeEntity> userSelect = userQuery.select(from);
        List<BaseItemTypeEntity> itemTypeEntities = entityManager.createQuery(userSelect).getResultList();

        List<BaseItemType> itemTypes = new ArrayList<>();
        for (BaseItemTypeEntity baseItemTypeEntity : itemTypeEntities) {
            itemTypes.add(baseItemTypeEntity.toBaseItemType());
        }
        return itemTypes;
    }

    @Transactional
    public void updateBaseItemType(BaseItemType baseItemType) {
        BaseItemTypeEntity baseItemTypeEntity = entityManager.find(BaseItemTypeEntity.class, (long) baseItemType.getId());
        baseItemTypeEntity.fromBaseItemType(baseItemType);
        baseItemTypeEntity.setShape3DId(shape3DPersistence.getColladaEntity(baseItemType.getShape3DId()));
        baseItemTypeEntity.setSpawnShape3DId(shape3DPersistence.getColladaEntity(baseItemType.getSpawnShape3DId()));
        entityManager.merge(baseItemTypeEntity);
    }

    @Transactional
    public void deleteBaseItemType(int id) {
        entityManager.remove(entityManager.find(BaseItemTypeEntity.class, (long) id));
    }

    @Transactional
    public ResourceItemType createResourceItemType() {
        ResourceItemTypeEntity resourceItemTypeEntity = new ResourceItemTypeEntity();
        entityManager.persist(resourceItemTypeEntity);
        return resourceItemTypeEntity.toResourceItemType();
    }

    @Transactional
    public List<ResourceItemType> readResourceItemType() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ResourceItemTypeEntity> userQuery = criteriaBuilder.createQuery(ResourceItemTypeEntity.class);
        Root<ResourceItemTypeEntity> from = userQuery.from(ResourceItemTypeEntity.class);
        CriteriaQuery<ResourceItemTypeEntity> userSelect = userQuery.select(from);
        List<ResourceItemTypeEntity> itemTypeEntities = entityManager.createQuery(userSelect).getResultList();

        List<ResourceItemType> itemTypes = new ArrayList<>();
        for (ResourceItemTypeEntity resourceItemTypeEntity : itemTypeEntities) {
            itemTypes.add(resourceItemTypeEntity.toResourceItemType());
        }
        return itemTypes;
    }

    @Transactional
    public void updateResourceItemType(ResourceItemType resourceItemType) {
        ResourceItemTypeEntity resourceItemTypeEntity = entityManager.find(ResourceItemTypeEntity.class, (long) resourceItemType.getId());
        resourceItemTypeEntity.fromBaseItemType(resourceItemType);
        resourceItemTypeEntity.setShape3DId(shape3DPersistence.getColladaEntity(resourceItemType.getShape3DId()));
        entityManager.merge(resourceItemTypeEntity);
    }

    @Transactional
    public void deleteResourceItemType(int id) {
        entityManager.remove(entityManager.find(ResourceItemTypeEntity.class, (long) id));
    }
}
