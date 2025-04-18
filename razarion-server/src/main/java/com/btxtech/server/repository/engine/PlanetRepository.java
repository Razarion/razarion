package com.btxtech.server.repository.engine;

import com.btxtech.server.model.engine.PlanetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanetRepository extends JpaRepository<PlanetEntity, Integer> {
}
