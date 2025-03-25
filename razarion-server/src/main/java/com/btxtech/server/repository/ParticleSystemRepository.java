package com.btxtech.server.repository;

import com.btxtech.server.model.ui.ParticleSystemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticleSystemRepository extends JpaRepository<ParticleSystemEntity, Integer> {
}
