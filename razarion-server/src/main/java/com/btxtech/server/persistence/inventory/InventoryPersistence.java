package com.btxtech.server.persistence.inventory;

import com.btxtech.shared.gameengine.datatypes.InventoryItem;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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

    public List<InventoryItem> readInventoryItems() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<InventoryItemEntity> userQuery = criteriaBuilder.createQuery(InventoryItemEntity.class);
        Root<InventoryItemEntity> from = userQuery.from(InventoryItemEntity.class);
        CriteriaQuery<InventoryItemEntity> userSelect = userQuery.select(from);
        List<InventoryItemEntity> itemTypeEntities = entityManager.createQuery(userSelect).getResultList();

        return itemTypeEntities.stream().map(InventoryItemEntity::toInventoryItem).collect(Collectors.toList());
    }
}
