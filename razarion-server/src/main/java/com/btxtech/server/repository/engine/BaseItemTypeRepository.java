package com.btxtech.server.repository.engine;

import com.btxtech.server.model.engine.BaseItemTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BaseItemTypeRepository extends JpaRepository<BaseItemTypeEntity, Integer> {
}
