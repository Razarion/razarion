package com.btxtech.server.repository.engine;

import com.btxtech.server.model.engine.ServerGameEngineConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerGameEngineConfigRepository extends JpaRepository<ServerGameEngineConfigEntity, Integer> {
}
