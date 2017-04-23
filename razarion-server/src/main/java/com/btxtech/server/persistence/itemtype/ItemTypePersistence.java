package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.Shape3DPersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
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
import java.util.List;
import java.util.stream.Collectors;

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
    @SecurityCheck
    public BaseItemType createBaseItemType() {
        BaseItemTypeEntity baseItemTypeEntity = new BaseItemTypeEntity();
        entityManager.persist(baseItemTypeEntity);
        return baseItemTypeEntity.toBaseItemType();
    }

    @Transactional
    public List<BaseItemType> readBaseItemTypes() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<BaseItemTypeEntity> userQuery = criteriaBuilder.createQuery(BaseItemTypeEntity.class);
        Root<BaseItemTypeEntity> from = userQuery.from(BaseItemTypeEntity.class);
        CriteriaQuery<BaseItemTypeEntity> userSelect = userQuery.select(from);
        List<BaseItemTypeEntity> itemTypeEntities = entityManager.createQuery(userSelect).getResultList();

        return itemTypeEntities.stream().map(BaseItemTypeEntity::toBaseItemType).collect(Collectors.toList());
    }

    @Transactional
    @SecurityCheck
    public void updateBaseItemType(BaseItemType baseItemType) {
        BaseItemTypeEntity baseItemTypeEntity = entityManager.find(BaseItemTypeEntity.class, baseItemType.getId());
        baseItemTypeEntity.fromBaseItemType(baseItemType);
        baseItemTypeEntity.setShape3DId(shape3DPersistence.getColladaEntity(baseItemType.getShape3DId()));
        baseItemTypeEntity.setSpawnShape3DId(shape3DPersistence.getColladaEntity(baseItemType.getSpawnShape3DId()));
        entityManager.merge(baseItemTypeEntity);
    }

    @Transactional
    @SecurityCheck
    public void deleteBaseItemType(int id) {
        entityManager.remove(entityManager.find(BaseItemTypeEntity.class, id));
    }

    @Transactional
    @SecurityCheck
    public ResourceItemType createResourceItemType() {
        ResourceItemTypeEntity resourceItemTypeEntity = new ResourceItemTypeEntity();
        entityManager.persist(resourceItemTypeEntity);
        return resourceItemTypeEntity.toResourceItemType();
    }

    @Transactional
    public List<ResourceItemType> readResourceItemTypes() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ResourceItemTypeEntity> userQuery = criteriaBuilder.createQuery(ResourceItemTypeEntity.class);
        Root<ResourceItemTypeEntity> from = userQuery.from(ResourceItemTypeEntity.class);
        CriteriaQuery<ResourceItemTypeEntity> userSelect = userQuery.select(from);
        List<ResourceItemTypeEntity> itemTypeEntities = entityManager.createQuery(userSelect).getResultList();

        return itemTypeEntities.stream().map(ResourceItemTypeEntity::toResourceItemType).collect(Collectors.toList());
    }

    @Transactional
    @SecurityCheck
    public void updateResourceItemType(ResourceItemType resourceItemType) {
        ResourceItemTypeEntity resourceItemTypeEntity = entityManager.find(ResourceItemTypeEntity.class, resourceItemType.getId());
        resourceItemTypeEntity.fromBaseItemType(resourceItemType);
        resourceItemTypeEntity.setShape3DId(shape3DPersistence.getColladaEntity(resourceItemType.getShape3DId()));
        entityManager.merge(resourceItemTypeEntity);
    }

    @Transactional
    @SecurityCheck
    public void deleteResourceItemType(int id) {
        entityManager.remove(entityManager.find(ResourceItemTypeEntity.class, id));
    }

    @Transactional
    @SecurityCheck
    public BoxItemType createBoxItemType() {
        BoxItemTypeEntity boxItemTypeEntity = new BoxItemTypeEntity();
        entityManager.persist(boxItemTypeEntity);
        return boxItemTypeEntity.toBoxItemType();
    }

    @Transactional
    public List<BoxItemType> readBoxItemTypes() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<BoxItemTypeEntity> userQuery = criteriaBuilder.createQuery(BoxItemTypeEntity.class);
        Root<BoxItemTypeEntity> from = userQuery.from(BoxItemTypeEntity.class);
        CriteriaQuery<BoxItemTypeEntity> userSelect = userQuery.select(from);
        List<BoxItemTypeEntity> itemTypeEntities = entityManager.createQuery(userSelect).getResultList();

        return itemTypeEntities.stream().map(BoxItemTypeEntity::toBoxItemType).collect(Collectors.toList());
    }

    @Transactional
    @SecurityCheck
    public void deleteBoxItemType(int id) {
        entityManager.remove(entityManager.find(BoxItemTypeEntity.class, id));
    }

    @Transactional
    @SecurityCheck
    public void updateBoxItemType(BoxItemType boxItemType) {
        BoxItemTypeEntity boxItemTypeEntity = entityManager.find(BoxItemTypeEntity.class, boxItemType.getId());
        boxItemTypeEntity.fromBoxItemType(boxItemType);
        boxItemTypeEntity.setShape3DId(shape3DPersistence.getColladaEntity(boxItemType.getShape3DId()));
        entityManager.merge(boxItemTypeEntity);
    }
}
