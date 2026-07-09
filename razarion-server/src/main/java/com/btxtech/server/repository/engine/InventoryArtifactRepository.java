package com.btxtech.server.repository.engine;

import com.btxtech.server.model.engine.InventoryArtifactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryArtifactRepository extends JpaRepository<InventoryArtifactEntity, Integer> {
}
