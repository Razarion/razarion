package com.btxtech.server.repository.engine;

import com.btxtech.server.model.engine.ResourceItemTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceItemTypeRepository extends JpaRepository<ResourceItemTypeEntity, Integer> {
}
