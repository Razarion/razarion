package com.btxtech.server.repository;

import com.btxtech.server.model.ui.BabylonMaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BabylonMaterialRepository extends JpaRepository<BabylonMaterialEntity, Integer> {
}
