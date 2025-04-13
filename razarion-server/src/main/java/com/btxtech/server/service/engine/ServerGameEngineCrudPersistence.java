package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.LevelEntity;
import com.btxtech.server.model.engine.ServerGameEngineConfigEntity;
import com.btxtech.server.model.engine.quest.QuestConfigEntity;
import com.btxtech.server.repository.engine.ServerGameEngineConfigRepository;
import com.btxtech.shared.dto.*;
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
//        // Does not work if there are multiple ServerGameEngineConfigEntity with same levels on ServerLevelQuestEntity
//        // ServerGameEngineConfigEntity is not considered in this query
//        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//        CriteriaQuery<QuestConfigEntity> criteriaQuery = criteriaBuilder.createQuery(QuestConfigEntity.class);
//        Root<ServerLevelQuestEntity> root = criteriaQuery.from(ServerLevelQuestEntity.class);
//
//        Join<ServerLevelQuestEntity, ServerLevelQuestEntryEntity> serverLevelQuestEntryEntityJoin = root.join(ServerLevelQuestEntity_.serverLevelQuestEntryEntities);
//        Join<ServerLevelQuestEntryEntity, QuestConfigEntity> questConfigEntityJoin = serverLevelQuestEntryEntityJoin.join(ServerLevelQuestEntryEntity_.quest);
//        Path<Integer> levelNumberPath = root.join(ServerLevelQuestEntity_.minimalLevel).get(LevelEntity_.number);
//
//        criteriaQuery.select(questConfigEntityJoin);
//
//        if (ignoreQuests != null && !ignoreQuests.isEmpty()) {
//            criteriaQuery.where(
//                    criteriaBuilder.and(
//                            criteriaBuilder.lessThanOrEqualTo(levelNumberPath, level.getNumber()),
//                            criteriaBuilder.not(questConfigEntityJoin.get(QuestConfigEntity_.id).in(ignoreQuests)))
//            );
//        } else {
//            criteriaQuery.where(
//                    criteriaBuilder.lessThanOrEqualTo(levelNumberPath, level.getNumber())
//            );
//        }
//        criteriaQuery.orderBy(
//                criteriaBuilder.asc(levelNumberPath),
//                criteriaBuilder.asc(serverLevelQuestEntryEntityJoin.get(ServerLevelQuestEntryEntity_.orderColumn))
//        );
//        criteriaQuery.distinct(true);
//
//        TypedQuery<QuestConfigEntity> typedQuery = entityManager.createQuery(criteriaQuery);
//
//        return typedQuery.getResultList();
        throw new UnsupportedOperationException("... TODO ...");
    }

    @Transactional
    public QuestConfig getAndVerifyQuest(int levelId, int questId) {
//        var levelNumber = levelCrudPersistence.getLevelNumber4Id(levelId);
//
//        getJpaRepository().
//
//                // Does not work if there are multiple ServerGameEngineConfigEntity with same levels on ServerLevelQuestEntity
//                // ServerGameEngineConfigEntity is not considered in this query
//                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//        CriteriaQuery<LevelEntity> userQuery = criteriaBuilder.createQuery(LevelEntity.class);
//        Root<ServerLevelQuestEntity> root = userQuery.from(ServerLevelQuestEntity.class);
//        CriteriaQuery<LevelEntity> userSelect = userQuery.select(root.join(ServerLevelQuestEntity_.minimalLevel));
//        userSelect.where(criteriaBuilder.equal(root.join(ServerLevelQuestEntity_.serverLevelQuestEntryEntities).join(ServerLevelQuestEntryEntity_.quest).get(QuestConfigEntity_.id), questId));
//        LevelEntity questLevelEntity = entityManager.createQuery(userSelect).getSingleResult();
//        LevelEntity userLevelEntity = levelCrudPersistence.getEntity(levelId);
//        if (userLevelEntity.getNumber() < questLevelEntity.getNumber()) {
//            throw new IllegalArgumentException("The user is not allowed to activate a quest due to wrong level. questLevelEntity: " + questLevelEntity + " userLevelEntity: " + userLevelEntity);
//        }
//        return entityManager.find(QuestConfigEntity.class, questId).toQuestConfig();
        throw new UnsupportedOperationException("... TODO ...");
    }

    public QuestConfigEntity getQuest4LevelAndCompleted(LevelEntity level, Collection<Integer> completedQuests) {
        List<QuestConfigEntity> questConfigEntities = getQuests4Level(level, completedQuests);
        if (questConfigEntities.isEmpty()) {
            return null;
        }
        return questConfigEntities.get(0);
    }

}