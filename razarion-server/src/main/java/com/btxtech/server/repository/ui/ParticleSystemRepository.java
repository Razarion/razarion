package com.btxtech.server.repository.ui;

import com.btxtech.server.model.ui.ParticleSystemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticleSystemRepository extends JpaRepository<ParticleSystemEntity, Integer> {

    @Query(value = "SELECT id, internalName, COALESCE(OCTET_LENGTH(data), 0) FROM PARTICLE_SYSTEM", nativeQuery = true)
    List<Object[]> findAllParticleSizes();
}
