package com.btxtech.server.service.engine;

import com.btxtech.server.RazarionServerEventListener;
import com.btxtech.server.model.engine.ServerGameEngineConfigEntity;
import com.btxtech.server.repository.engine.ServerGameEngineConfigRepository;
import com.btxtech.shared.dto.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Created by Beat
 * 09.05.2017.
 */
@Service
public class ServerGameEngineCrudPersistence extends AbstractConfigCrudPersistence<ServerGameEngineConfig, ServerGameEngineConfigEntity> {
    private final Logger logger = LoggerFactory.getLogger(ServerGameEngineCrudPersistence.class);
    private final LevelCrudPersistence levelCrudPersistence;

    public ServerGameEngineCrudPersistence(ServerGameEngineConfigRepository serverGameEngineConfigRepository, LevelCrudPersistence levelCrudPersistence) {
        super(ServerGameEngineConfigEntity.class, serverGameEngineConfigRepository);
        this.levelCrudPersistence = levelCrudPersistence;
    }

    @Override
    protected ServerGameEngineConfig toConfig(ServerGameEngineConfigEntity entity) {
        return entity.toServerGameEngineConfig();
    }

    @Override
    protected void fromConfig(ServerGameEngineConfig config, ServerGameEngineConfigEntity entity) {
//        entity.fromServerGameEngineConfig(config,
//                planetCrudPersistence,
//                resourceItemTypeCrudPersistence,
//                levelCrudPersistence,
//                baseItemTypeCrudPersistence,
//                botConfigEntityPersistence,
//                babylonMaterialCrudPersistence);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Transactional
    public SlavePlanetConfig readSlavePlanetConfig(int levelId) {
        return serverGameEngineConfigEntity().findSlavePlanetConfig4Level(levelCrudPersistence.getLevelNumber4Id(levelId));
    }

    private ServerGameEngineConfigEntity serverGameEngineConfigEntity() {
        try {
            return getEntities().stream().findFirst().orElseThrow((Supplier<Throwable>) () -> new IllegalStateException("No ServerGameEngineConfigEntity in DB"));
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Transactional
    public MasterPlanetConfig readMasterPlanetConfig() {
        try {
            return serverGameEngineConfigEntity().getMasterPlanetConfig();
        } catch (Throwable t) {
            logger.warn("Using fallback. Error reading MasterPlanetConfig: " + t.getMessage(), t);
            return FallbackConfig.setupMasterPlanetConfig();
        }
    }

    @Transactional
    public Collection<BoxRegionConfig> readBoxRegionConfigs() {
        return serverGameEngineConfigEntity().getBoxRegionConfigs();
    }
}
