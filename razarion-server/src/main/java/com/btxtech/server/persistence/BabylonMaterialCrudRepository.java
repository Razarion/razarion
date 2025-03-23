package com.btxtech.server.persistence;

import com.btxtech.server.persistence.ui.BabylonMaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BabylonMaterialCrudRepository extends JpaRepository<BabylonMaterialEntity, Integer> {
}
