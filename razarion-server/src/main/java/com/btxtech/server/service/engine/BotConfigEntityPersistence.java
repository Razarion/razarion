package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.BotConfigEntity;
import com.btxtech.server.repository.engine.BotConfigRepository;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import org.springframework.stereotype.Service;

@Service
public class BotConfigEntityPersistence extends AbstractConfigCrudPersistence<BotConfig, BotConfigEntity> {
    public BotConfigEntityPersistence(BotConfigRepository botConfigRepository) {
        super(BotConfigEntity.class, botConfigRepository);
    }

    @Override
    protected BotConfig toConfig(BotConfigEntity entity) {
        throw new UnsupportedOperationException("...TODO...");
    }

    @Override
    protected void fromConfig(BotConfig config, BotConfigEntity entity) {
        throw new UnsupportedOperationException("...TODO...");
    }
}
