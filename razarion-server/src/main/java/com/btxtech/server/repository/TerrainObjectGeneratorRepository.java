package com.btxtech.server.repository;

import com.btxtech.server.model.ui.TerrainObjectGeneratorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TerrainObjectGeneratorRepository extends JpaRepository<TerrainObjectGeneratorEntity, Integer> {
}
