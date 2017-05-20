package com.btxtech.server.persistence.inventory;

import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;

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
 * 13.05.2017.
 */
@Singleton
public class InventoryPersistence {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private ImagePersistence imagePersistence;
    @Inject
    private ItemTypePersistence itemTypePersistence;

    @Transactional
    @SecurityCheck
    public InventoryItem createInventoryItem() {
        InventoryItemEntity inventoryItemEntity = new InventoryItemEntity();
        entityManager.persist(inventoryItemEntity);
        return inventoryItemEntity.toInventoryItem();
    }

    @Transactional
    @SecurityCheck
    public void updateInventoryItem(InventoryItem inventoryItem) {
        InventoryItemEntity inventoryItemEntity = readInventoryItemEntity(inventoryItem.getId());
        inventoryItemEntity.fromInventoryItem(inventoryItem);
        inventoryItemEntity.setBaseItemType(itemTypePersistence.readBaseItemTypeEntity(inventoryItem.getBaseItemTypeId()));
        inventoryItemEntity.setImage(imagePersistence.getImageLibraryEntity(inventoryItem.getImageId()));
        entityManager.merge(inventoryItemEntity);
    }

    @Transactional
    @SecurityCheck
    public void deleteInventoryItem(int id) {
        entityManager.remove(readInventoryItemEntity(id));
    }

    @Transactional
    public List<InventoryItem> readInventoryItems() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<InventoryItemEntity> userQuery = criteriaBuilder.createQuery(InventoryItemEntity.class);
        Root<InventoryItemEntity> from = userQuery.from(InventoryItemEntity.class);
        CriteriaQuery<InventoryItemEntity> userSelect = userQuery.select(from);
        List<InventoryItemEntity> itemTypeEntities = entityManager.createQuery(userSelect).getResultList();

        return itemTypeEntities.stream().map(InventoryItemEntity::toInventoryItem).collect(Collectors.toList());
    }

    @Transactional
    public InventoryItemEntity readInventoryItemEntity(int id) {
        InventoryItemEntity inventoryItemEntity = entityManager.find(InventoryItemEntity.class, id);
        if (inventoryItemEntity == null) {
            throw new IllegalArgumentException("No InventoryItemEntity for id: " + id);
        }
        return inventoryItemEntity;
    }
}
