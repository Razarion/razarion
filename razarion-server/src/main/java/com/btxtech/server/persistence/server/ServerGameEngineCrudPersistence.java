package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.AbstractConfigCrudPersistence;
import com.btxtech.server.persistence.BabylonMaterialCrudPersistence;
import com.btxtech.server.persistence.BoxItemTypeCrudPersistence;
import com.btxtech.server.persistence.PlanetCrudPersistence;
import com.btxtech.server.persistence.bot.BotConfigEntity;
import com.btxtech.server.persistence.itemtype.BaseItemTypeCrudPersistence;
import com.btxtech.server.persistence.itemtype.BotConfigEntityPersistence;
import com.btxtech.server.persistence.itemtype.ResourceItemTypeCrudPersistence;
import com.btxtech.server.persistence.level.LevelCrudPersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelEntity_;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity_;
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

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 09.05.2017.
 */
@Singleton
public class ServerGameEngineCrudPersistence extends AbstractConfigCrudPersistence<ServerGameEngineConfig, ServerGameEngineConfigEntity> {
    private final Logger logger = Logger.getLogger(ServerGameEngineCrudPersistence.class.getName());
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private PlanetCrudPersistence planetCrudPersistence;
    @Inject
    private BaseItemTypeCrudPersistence baseItemTypeCrudPersistence;
    @Inject
    private ResourceItemTypeCrudPersistence resourceItemTypeCrudPersistence;
    @Inject
    private BotConfigEntityPersistence botConfigEntityPersistence;
    @Inject
    private LevelCrudPersistence levelCrudPersistence;
    @Inject
    private BoxItemTypeCrudPersistence boxItemTypeCrudPersistence;
    @Inject
    private BabylonMaterialCrudPersistence babylonMaterialCrudPersistence;

    public ServerGameEngineCrudPersistence() {
        super(ServerGameEngineConfigEntity.class, ServerGameEngineConfigEntity_.id, ServerGameEngineConfigEntity_.internalName);
    }

    @Override
    protected ServerGameEngineConfig toConfig(ServerGameEngineConfigEntity entity) {
        return entity.toServerGameEngineConfig();
    }

    @Override
    protected void fromConfig(ServerGameEngineConfig config, ServerGameEngineConfigEntity entity) {
        entity.fromServerGameEngineConfig(config,
                planetCrudPersistence,
                resourceItemTypeCrudPersistence,
                levelCrudPersistence,
                baseItemTypeCrudPersistence,
                botConfigEntityPersistence,
                babylonMaterialCrudPersistence);
    }

    @Transactional
    public SlavePlanetConfig readSlavePlanetConfig(int levelId) {
        return serverGameEngineConfigEntity().findSlavePlanetConfig4Level(levelCrudPersistence.getLevelNumber4Id(levelId));
    }

    @Transactional
    public MasterPlanetConfig readMasterPlanetConfig() {
        try {
            return serverGameEngineConfigEntity().getMasterPlanetConfig();
        } catch (Throwable t) {
            logger.severe("Using fallback. Error reading MasterPlanetConfig: " + t.getMessage());
            return FallbackConfig.setupMasterPlanetConfig();
        }
    }

    @Transactional
    public Collection<BotConfig> readBotConfigs() {
        return serverGameEngineConfigEntity().getBotConfigs();
    }

    @Transactional
    public Collection<BoxRegionConfig> readBoxRegionConfigs() {
        return serverGameEngineConfigEntity().getBoxRegionConfigs();
    }

    @Transactional
    public Collection<Integer> readAllQuestIds() {
        // ServerGameEngineConfigEntity is not considered in this query
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> userQuery = criteriaBuilder.createQuery(Integer.class);
        Root<ServerLevelQuestEntity> root = userQuery.from(ServerLevelQuestEntity.class);
        CriteriaQuery<Integer> userSelect = userQuery.select(root.join(ServerLevelQuestEntity_.serverLevelQuestEntryEntities).join(ServerLevelQuestEntryEntity_.quest).get(QuestConfigEntity_.id));
        return entityManager.createQuery(userSelect).getResultList();
    }

    public QuestConfigEntity getQuest4LevelAndCompleted(LevelEntity level, Collection<Integer> completedQuests) {
        List<QuestConfigEntity> questConfigEntities = getQuests4Level(level, completedQuests);
        if (questConfigEntities.isEmpty()) {
            return null;
        }
        return questConfigEntities.get(0);
    }

    @Transactional
    public List<QuestConfig> getQuests4Dialog(LevelEntity level, Collection<Integer> ignoreQuests) {
        return getQuests4Level(level, ignoreQuests)
                .stream()
                .map(QuestConfigEntity::toQuestConfig)
                .collect(Collectors.toList());
    }

    private List<QuestConfigEntity> getQuests4Level(LevelEntity level, Collection<Integer> ignoreQuests) {
        // Does not work if there are multiple ServerGameEngineConfigEntity with same levels on ServerLevelQuestEntity
        // ServerGameEngineConfigEntity is not considered in this query
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<QuestConfigEntity> criteriaQuery = criteriaBuilder.createQuery(QuestConfigEntity.class);
        Root<ServerLevelQuestEntity> root = criteriaQuery.from(ServerLevelQuestEntity.class);

        Join<ServerLevelQuestEntity, ServerLevelQuestEntryEntity> serverLevelQuestEntryEntityJoin = root.join(ServerLevelQuestEntity_.serverLevelQuestEntryEntities);
        Join<ServerLevelQuestEntryEntity, QuestConfigEntity> questConfigEntityJoin = serverLevelQuestEntryEntityJoin.join(ServerLevelQuestEntryEntity_.quest);
        Path<Integer> levelNumberPath = root.join(ServerLevelQuestEntity_.minimalLevel).get(LevelEntity_.number);

        criteriaQuery.select(questConfigEntityJoin);

        if (ignoreQuests != null && !ignoreQuests.isEmpty()) {
            criteriaQuery.where(
                    criteriaBuilder.and(
                            criteriaBuilder.lessThanOrEqualTo(levelNumberPath, level.getNumber()),
                            criteriaBuilder.not(questConfigEntityJoin.get(QuestConfigEntity_.id).in(ignoreQuests)))
            );
        } else {
            criteriaQuery.where(
                    criteriaBuilder.lessThanOrEqualTo(levelNumberPath, level.getNumber())
            );
        }

        criteriaQuery.orderBy(
                criteriaBuilder.asc(levelNumberPath),
                criteriaBuilder.asc(serverLevelQuestEntryEntityJoin.get(ServerLevelQuestEntryEntity_.orderColumn))
        );
        criteriaQuery.distinct(true);

        TypedQuery<QuestConfigEntity> typedQuery = entityManager.createQuery(criteriaQuery);

        return typedQuery.getResultList();
    }

    @Transactional
    public QuestConfig getAndVerifyQuest(int levelId, int questId) {
        // Does not work if there are multiple ServerGameEngineConfigEntity with same levels on ServerLevelQuestEntity
        // ServerGameEngineConfigEntity is not considered in this query
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LevelEntity> userQuery = criteriaBuilder.createQuery(LevelEntity.class);
        Root<ServerLevelQuestEntity> root = userQuery.from(ServerLevelQuestEntity.class);
        CriteriaQuery<LevelEntity> userSelect = userQuery.select(root.join(ServerLevelQuestEntity_.minimalLevel));
        userSelect.where(criteriaBuilder.equal(root.join(ServerLevelQuestEntity_.serverLevelQuestEntryEntities).join(ServerLevelQuestEntryEntity_.quest).get(QuestConfigEntity_.id), questId));
        LevelEntity questLevelEntity = entityManager.createQuery(userSelect).getSingleResult();
        LevelEntity userLevelEntity = levelCrudPersistence.getEntity(levelId);
        if (userLevelEntity.getNumber() < questLevelEntity.getNumber()) {
            throw new IllegalArgumentException("The user is not allowed to activate a quest due to wrong level. questLevelEntity: " + questLevelEntity + " userLevelEntity: " + userLevelEntity);
        }
        return entityManager.find(QuestConfigEntity.class, questId).toQuestConfig();
    }

    private ServerGameEngineConfigEntity serverGameEngineConfigEntity() {
        try {
            return getEntities().stream().findFirst().orElseThrow((Supplier<Throwable>) () -> new IllegalStateException("No ServerGameEngineConfigEntity in DB"));
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Transactional
    public void updateResourceRegionConfig(int serverGameEngineConfigId, List<ResourceRegionConfig> resourceRegionConfigs) {
        updateChildren(serverGameEngineConfigId,
                resourceRegionConfigs,
                ServerGameEngineConfigEntity::getResourceRegionConfigs,
                ResourceRegionConfig::getId,
                (resourceRegionConfig, serverResourceRegionConfigEntity) -> serverResourceRegionConfigEntity.fromResourceRegionConfig(resourceItemTypeCrudPersistence, resourceRegionConfig),
                ServerResourceRegionConfigEntity::new,
                ServerResourceRegionConfigEntity::getId);
    }

    @Transactional
    public void updateStartRegionConfig(int serverGameEngineConfigId, List<StartRegionConfig> startRegionConfigs) {
        updateChildren(serverGameEngineConfigId,
                startRegionConfigs,
                ServerGameEngineConfigEntity::getStartRegionConfigs,
                StartRegionConfig::getId,
                (startRegionConfig, startRegionConfigEntity) -> startRegionConfigEntity.fromStartRegionConfig(startRegionConfig, levelCrudPersistence),
                StartRegionConfigEntity::new,
                StartRegionConfigEntity::getId);
    }

    @Transactional
    public void updateBotConfig(int serverGameEngineConfigId, List<BotConfig> botConfigs) {
        updateChildren(serverGameEngineConfigId,
                botConfigs,
                ServerGameEngineConfigEntity::getBotConfigEntities,
                BotConfig::getId,
                (botConfig, botConfigEntity) -> botConfigEntity.fromBotConfig(baseItemTypeCrudPersistence, babylonMaterialCrudPersistence, botConfig),
                BotConfigEntity::new,
                BotConfigEntity::getId);
    }

    @Transactional
    public void updateServerLevelQuestConfig(int serverGameEngineConfigId, List<ServerLevelQuestConfig> serverLevelQuestConfigs) {
        updateChildren(serverGameEngineConfigId,
                serverLevelQuestConfigs,
                ServerGameEngineConfigEntity::getServerLevelQuestEntities,
                ServerLevelQuestConfig::getId,
                (serverLevelQuestConfig, serverLevelQuestEntity) -> serverLevelQuestEntity.fromServerLevelQuestConfig(botConfigEntityPersistence, baseItemTypeCrudPersistence, serverLevelQuestConfig, levelCrudPersistence),
                ServerLevelQuestEntity::new,
                ServerLevelQuestEntity::getId);
    }

    @Transactional
    public void updateBoxRegionConfig(int serverGameEngineConfigId, List<BoxRegionConfig> boxRegionConfigs) {
        updateChildren(serverGameEngineConfigId,
                boxRegionConfigs,
                ServerGameEngineConfigEntity::getServerBoxRegionConfigEntities,
                BoxRegionConfig::getId,
                (boxRegionConfig, boxRegionConfigEntity) -> boxRegionConfigEntity.fromBoxRegionConfig(boxItemTypeCrudPersistence, boxRegionConfig),
                ServerBoxRegionConfigEntity::new,
                ServerBoxRegionConfigEntity::getId);
    }

    private <C, E> void updateChildren(int serverGameEngineConfigId,
                                       List<C> childConfigs,
                                       Function<ServerGameEngineConfigEntity, List<E>> getChildren,
                                       Function<C, Integer> getChildId,
                                       BiConsumer<C, E> fromConfig,
                                       Supplier<E> entityGenerator,
                                       Function<E, Integer> getEntityId) {
        ServerGameEngineConfigEntity serverGameEngineConfigEntity = getEntity(serverGameEngineConfigId);
        Map<Integer, E> dbMap = getChildren.apply(serverGameEngineConfigEntity).stream()
                .collect(Collectors.toMap(getEntityId, Function.identity()));

        Set<Integer> updated = new HashSet<>();
        Collection<E> created = new ArrayList<>();
        childConfigs.forEach(childConfig -> {
            if (getChildId.apply(childConfig) != null && dbMap.containsKey(getChildId.apply(childConfig))) {

                fromConfig.accept(childConfig, dbMap.get(getChildId.apply(childConfig)));
                updated.add(getChildId.apply(childConfig));
            } else {
                E entity = entityGenerator.get();
                fromConfig.accept(childConfig, entity);
                created.add(entity);
            }
        });

        getChildren.apply(serverGameEngineConfigEntity).removeIf(e -> !updated.contains(getEntityId.apply(e)));
        getChildren.apply(serverGameEngineConfigEntity).addAll(created);

        entityManager.merge(serverGameEngineConfigEntity);
    }
}
