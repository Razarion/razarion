package com.btxtech.server.repository.engine;

import com.btxtech.server.model.engine.GroundConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroundConfigRepository extends JpaRepository<GroundConfigEntity, Integer> {
}
