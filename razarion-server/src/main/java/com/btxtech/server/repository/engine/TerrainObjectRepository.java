package com.btxtech.server.repository.engine;

import com.btxtech.server.model.engine.TerrainObjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TerrainObjectRepository extends JpaRepository<TerrainObjectEntity, Integer> {
}
