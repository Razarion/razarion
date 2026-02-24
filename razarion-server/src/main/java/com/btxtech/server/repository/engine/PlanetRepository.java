package com.btxtech.server.repository.engine;

import com.btxtech.server.model.engine.PlanetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanetRepository extends JpaRepository<PlanetEntity, Integer> {

    @Query(value = "SELECT COALESCE(OCTET_LENGTH(compressedHeightMap), 0) FROM PLANET WHERE id = :planetId", nativeQuery = true)
    int findHeightmapSize(@Param("planetId") int planetId);
}
