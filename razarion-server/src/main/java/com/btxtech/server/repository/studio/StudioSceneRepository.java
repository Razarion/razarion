package com.btxtech.server.repository.studio;

import com.btxtech.server.model.studio.StudioSceneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudioSceneRepository extends JpaRepository<StudioSceneEntity, Integer> {
}
