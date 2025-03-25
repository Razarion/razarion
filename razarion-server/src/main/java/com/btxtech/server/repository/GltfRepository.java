package com.btxtech.server.repository;

import com.btxtech.server.model.ui.GltfEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GltfRepository extends JpaRepository<GltfEntity, Integer> {
}
