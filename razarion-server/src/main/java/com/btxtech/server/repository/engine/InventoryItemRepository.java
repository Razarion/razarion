package com.btxtech.server.repository.engine;

import com.btxtech.server.model.engine.InventoryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItemEntity, Integer> {
}
