package com.btxtech.server.service.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.engine.BotConfigEntity;
import com.btxtech.server.model.engine.LevelEntity;
import com.btxtech.server.model.engine.ServerBoxRegionConfigEntity;
import com.btxtech.server.model.engine.ServerGameEngineConfigEntity;
import com.btxtech.server.model.engine.ServerLevelQuestEntity;
import com.btxtech.server.model.engine.ServerResourceRegionConfigEntity;
import com.btxtech.server.model.engine.StartRegionConfigEntity;
import com.btxtech.server.model.engine.quest.QuestConfigEntity;
import com.btxtech.server.repository.engine.ServerGameEngineConfigRepository;
import com.btxtech.server.service.PersistenceUtil;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.dto.ServerGameEngineConfig;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.dto.SlavePlanetConfig;
import com.btxtech.shared.dto.StartRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class ServerGameEngineService extends AbstractConfigCrudService<ServerGameEngineConfig, ServerGameEngineConfigEntity> {
    private final Logger logger = LoggerFactory.getLogger(ServerGameEngineService.class);
    private final LevelCrudService levelCrudPersistence;

    public ServerGameEngineService(ServerGameEngineConfigRepository serverGameEngineConfigRepository,
                                   LevelCrudService levelCrudPersistence) {
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

    @Transactional
    public ServerGameEngineConfig serverGameEngineConfig() {
        return serverGameEngineConfigEntity().toServerGameEngineConfig();
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

    @Transactional
    public void updateResourceRegionConfig(int serverGameEngineConfigId, List<ResourceRegionConfig> resourceRegionConfigs) {
        var serverGameEngineConfig = getBaseEntity(serverGameEngineConfigId);
        serverGameEngineConfig.setResourceRegionConfigs(resourceRegionConfigs.stream()
                .map(c -> new ServerResourceRegionConfigEntity().fromResourceRegionConfig(c))
                .toList());

        updateBaseEntity(serverGameEngineConfig);
    }

    @Transactional
    public void updateStartRegionConfig(int serverGameEngineConfigId, List<StartRegionConfig> startRegionConfigs) {
        var serverGameEngineConfig = getBaseEntity(serverGameEngineConfigId);
        serverGameEngineConfig.setStartRegionConfigs(startRegionConfigs.stream()
                .map(c -> new StartRegionConfigEntity().fromStartRegionConfig(c))
                .toList());
        updateBaseEntity(serverGameEngineConfig);
    }

    @Transactional
    public void updateBotConfig(int serverGameEngineConfigId, List<BotConfig> botConfigs) {
        var serverGameEngineConfig = getBaseEntity(serverGameEngineConfigId);
        serverGameEngineConfig.setBotConfigs(botConfigs.stream()
                .map(c -> new BotConfigEntity().fromBotConfig(c))
                .toList());

        updateBaseEntity(serverGameEngineConfig);
    }

    @Transactional
    public void updateServerLevelQuestConfig(int serverGameEngineConfigId, List<ServerLevelQuestConfig> serverLevelQuestConfigs) {
        var serverGameEngineConfig = getBaseEntity(serverGameEngineConfigId);
        PersistenceUtil.fromConfigsNoClear(serverGameEngineConfig.getServerLevelQuestEntities(),
                serverLevelQuestConfigs,
                ServerLevelQuestEntity::new,
                ServerLevelQuestEntity::fromServerLevelQuestConfig,
                ServerLevelQuestConfig::getId,
                BaseEntity::getId);
        updateBaseEntity(serverGameEngineConfig);
    }

    @Transactional
    public void updateBoxRegionConfig(int serverGameEngineConfigId, List<BoxRegionConfig> boxRegionConfigs) {
        var serverGameEngineConfig = getBaseEntity(serverGameEngineConfigId);
        serverGameEngineConfig.setBoxRegionConfigs(boxRegionConfigs.stream()
                .map(c -> new ServerBoxRegionConfigEntity().fromBoxRegionConfig(c))
                .toList());

        updateBaseEntity(serverGameEngineConfig);
    }
}