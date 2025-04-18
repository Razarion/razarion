package com.btxtech.server.repository.engine;

import com.btxtech.server.model.engine.BoxItemTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoxItemTypeRepository extends JpaRepository<BoxItemTypeEntity, Integer> {
}
