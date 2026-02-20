package com.btxtech.server.repository.ui;

import com.btxtech.server.model.ui.BabylonMaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BabylonMaterialRepository extends JpaRepository<BabylonMaterialEntity, Integer> {

    @Query(value = "SELECT id, internalName, COALESCE(OCTET_LENGTH(data), 0) FROM BABYLON_MATERIAL", nativeQuery = true)
    List<Object[]> findAllMaterialSizes();
}
