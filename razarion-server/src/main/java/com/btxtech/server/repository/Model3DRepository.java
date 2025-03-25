package com.btxtech.server.repository;

import com.btxtech.server.model.ui.GltfEntity;
import com.btxtech.server.model.ui.Model3DEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Model3DRepository extends JpaRepository<Model3DEntity, Integer> {

    List<Model3DEntity> getModel3DsByGltfEntity(GltfEntity gltfEntity);
}
