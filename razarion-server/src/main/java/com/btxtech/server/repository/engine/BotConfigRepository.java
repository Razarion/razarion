package com.btxtech.server.repository.engine;

import com.btxtech.server.model.engine.BotConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BotConfigRepository extends JpaRepository<BotConfigEntity, Integer> {
}
