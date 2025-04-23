package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.LevelEntity;
import com.btxtech.server.model.engine.ServerGameEngineConfigEntity;
import com.btxtech.server.model.engine.quest.QuestConfigEntity;
import com.btxtech.server.repository.engine.ServerGameEngineConfigRepository;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.ServerGameEngineConfig;
import com.btxtech.shared.dto.SlavePlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class ServerGameEngineCrudPersistence extends AbstractConfigCrudPersistence<ServerGameEngineConfig, ServerGameEngineConfigEntity> {
    private final Logger logger = LoggerFactory.getLogger(ServerGameEngineCrudPersistence.class);
    private final LevelCrudPersistence levelCrudPersistence;

    public ServerGameEngineCrudPersistence(ServerGameEngineConfigRepository serverGameEngineConfigRepository,
                                           LevelCrudPersistence levelCrudPersistence) {
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

    @Transactional
    public List<QuestConfig> getQuests4Dialog(LevelEntity level, Collection<Integer> ignoreQuests) {
        return getQuests4Level(level, ignoreQuests)
                .stream()
                .map(QuestConfigEntity::toQuestConfig)
                .collect(Collectors.toList());
    }

    private List<QuestConfigEntity> getQuests4Level(LevelEntity level, Collection<Integer> ignoreQuests) {
        var serverGameEngineConfigEntityId = serverGameEngineConfigEntity().getId();
        return ((ServerGameEngineConfigRepository) getJpaRepository()).getQuests4Level(level.getNumber(),
                serverGameEngineConfigEntityId,
                ignoreQuests);
    }

    @Transactional
    public QuestConfig getAndVerifyQuest(int levelId, int questId) {
        var levelEntity = levelCrudPersistence.getEntity(levelId);
        return getQuests4Level(levelEntity, null)
                .stream()
                .filter(questConfigEntity -> questConfigEntity.getId() == questId)
                .findFirst()
                .orElseThrow()
                .toQuestConfig();
    }

    public QuestConfigEntity getQuest4LevelAndIgnoreCompleted(LevelEntity level, Collection<Integer> completedQuests) {
        List<QuestConfigEntity> questConfigEntities = getQuests4Level(level, completedQuests);
        if (questConfigEntities.isEmpty()) {
            return null;
        }
        return questConfigEntities.get(0);
    }

}