package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.PlanetPersistence;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelEntity_;
import com.btxtech.server.persistence.level.LevelPersistence;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity_;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.dto.SlavePlanetConfig;
import com.btxtech.shared.dto.StartRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * Created by Beat
 * 09.05.2017.
 */
@Singleton
public class ServerGameEnginePersistence {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private PlanetPersistence planetPersistence;
    @Inject
    private ItemTypePersistence itemTypePersistence;
    @Inject
    private LevelPersistence levelPersistence;
    @Inject
    private Instance<ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, ServerLevelQuestEntity, ServerLevelQuestConfig>> serverLevelQuestCrudInstance;
    @Inject
    private Instance<ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerLevelQuestEntity, QuestConfigEntity, QuestConfig>> serverQuestCrudInstance;

    @Transactional
    public SlavePlanetConfig readSlavePlanetConfig(int levelId) {
        SlavePlanetConfig slavePlanetConfig = new SlavePlanetConfig();
        slavePlanetConfig.setStartRegion(read().findStartRegion(levelPersistence.getLevelNumber4Id(levelId)));
        return slavePlanetConfig;
    }

    @Transactional
    public MasterPlanetConfig readMasterPlanetConfig() {
        return read().getMasterPlanetConfig();
    }

    @Transactional
    public PlanetConfig readPlanetConfig() {
        return read().getPlanetConfig();
    }

    @Transactional
    public Collection<BotConfig> readBotConfigs() {
        return read().getBotConfigs();
    }

    @Transactional
    @SecurityCheck
    public void updatePlanetConfig(Integer planetConfigId) {
        ServerGameEngineConfigEntity serverGameEngineConfigEntity = read();
        if (planetConfigId != null) {
            serverGameEngineConfigEntity.setPlanetEntity(planetPersistence.loadPlanet(planetConfigId));
        } else {
            serverGameEngineConfigEntity.setPlanetEntity(null);
        }
        entityManager.merge(serverGameEngineConfigEntity);
    }

    @Transactional
    @SecurityCheck
    public List<ObjectNameId> readStartRegionObjectNameIds() {
        return read().readStartRegionObjectNameIds();
    }

    @Transactional
    @SecurityCheck
    public StartRegionConfig readStartRegionConfig(int id) {
        return read().readStartRegionConfig(id);
    }

    @Transactional
    @SecurityCheck
    public StartRegionConfig createStartRegionConfig() {
        ServerGameEngineConfigEntity serverGameEngineConfigEntity = read();
        StartRegionLevelConfigEntity startRegionLevelConfigEntity = serverGameEngineConfigEntity.createStartRegionConfig();
        entityManager.persist(serverGameEngineConfigEntity); // Ignores changes on parent but child id is set
        return startRegionLevelConfigEntity.toStartRegionConfig();
    }

    @Transactional
    @SecurityCheck
    public void updateStartRegionConfig(StartRegionConfig startRegionConfig) {
        ServerGameEngineConfigEntity serverGameEngineConfigEntity = read();
        serverGameEngineConfigEntity.updateStartRegionConfig(startRegionConfig, levelPersistence);
        entityManager.merge(serverGameEngineConfigEntity);
    }

    @Transactional
    @SecurityCheck
    public void updateResourceRegionConfigs(List<ResourceRegionConfig> resourceRegionConfigs) {
        ServerGameEngineConfigEntity serverGameEngineConfigEntity = read();
        serverGameEngineConfigEntity.setResourceRegionConfigs(itemTypePersistence, resourceRegionConfigs);
        entityManager.merge(serverGameEngineConfigEntity);
    }

    @Transactional
    @SecurityCheck
    public void deleteStartRegion(int id) {
        ServerGameEngineConfigEntity serverGameEngineConfigEntity = read();
        serverGameEngineConfigEntity.deleteStartRegion(id);
        entityManager.merge(serverGameEngineConfigEntity);
    }

    @Transactional
    @SecurityCheck
    public void updateBotConfigs(List<BotConfig> botConfigs) {
        ServerGameEngineConfigEntity serverGameEngineConfigEntity = read();
        serverGameEngineConfigEntity.setBotConfigs(itemTypePersistence, botConfigs);
        entityManager.merge(serverGameEngineConfigEntity);
    }

    private ServerGameEngineConfigEntity read() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ServerGameEngineConfigEntity> userQuery = criteriaBuilder.createQuery(ServerGameEngineConfigEntity.class);
        Root<ServerGameEngineConfigEntity> from = userQuery.from(ServerGameEngineConfigEntity.class);
        CriteriaQuery<ServerGameEngineConfigEntity> userSelect = userQuery.select(from);
        List<ServerGameEngineConfigEntity> serverGameEngineConfigEntities = entityManager.createQuery(userSelect).getResultList();
        if (serverGameEngineConfigEntities.isEmpty()) {
            throw new IllegalStateException("No ServerGameEngineConfigEntity found");
        }
        if (serverGameEngineConfigEntities.size() > 1) {
            throw new IllegalStateException("More then one ServerGameEngineConfigEntity found: " + serverGameEngineConfigEntities.size());
        }
        return serverGameEngineConfigEntities.get(0);
    }

    public QuestConfigEntity getQuestConfigEntityUser(LevelEntity newLevel, Collection<QuestConfigEntity> completedQuests) {
        return getQuestConfigEntityIntern(newLevel, path -> {
            if (completedQuests == null || completedQuests.isEmpty()) {
                return null;
            } else {
                return path.get(QuestConfigEntity_.id).in(completedQuests);
            }
        });
    }

    public QuestConfigEntity getQuestConfigEntityUnregisteredUser(LevelEntity newLevel, Collection<Integer> completedQuestIds) {
        return getQuestConfigEntityIntern(newLevel, path -> {
            if (completedQuestIds == null || completedQuestIds.isEmpty()) {
                return null;
            } else {
                return path.in(completedQuestIds);
            }
        });
    }

    @Transactional
    public Collection<Integer> readAllQuestIds() {
        // ServerGameEngineConfigEntity is not considered in this query
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> userQuery = criteriaBuilder.createQuery(Integer.class);
        Root<ServerLevelQuestEntity> root = userQuery.from(ServerLevelQuestEntity.class);
        CriteriaQuery<Integer> userSelect = userQuery.select(root.join(ServerLevelQuestEntity_.questConfigs).get(QuestConfigEntity_.id));
        return entityManager.createQuery(userSelect).getResultList();
    }

    private QuestConfigEntity getQuestConfigEntityIntern(LevelEntity newLevel, Function<Path<QuestConfigEntity>, Predicate> inCallback) {
        // Does not work if there are multiple ServerGameEngineConfigEntity with same levels on ServerLevelQuestEntity
        // ServerGameEngineConfigEntity is not considered in this query
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<QuestConfigEntity> criteriaQuery = criteriaBuilder.createQuery(QuestConfigEntity.class);
        Root<ServerLevelQuestEntity> root = criteriaQuery.from(ServerLevelQuestEntity.class);
        CriteriaQuery<QuestConfigEntity> userSelect = criteriaQuery.select(root.join(ServerLevelQuestEntity_.questConfigs));
        Predicate minimalLevelPredicate = criteriaBuilder.lessThanOrEqualTo(root.join(ServerLevelQuestEntity_.minimalLevel).get(LevelEntity_.number), newLevel.getNumber());
        Predicate inPredicate = inCallback.apply(root.join(ServerLevelQuestEntity_.questConfigs));
        if (inPredicate != null) {
            userSelect.where(criteriaBuilder.and(minimalLevelPredicate, criteriaBuilder.not(inPredicate)));
        } else {
            userSelect.where(minimalLevelPredicate);
        }
        criteriaQuery.orderBy(criteriaBuilder.desc(root.join(ServerLevelQuestEntity_.minimalLevel).get(LevelEntity_.number)));
        return entityManager.createQuery(userSelect).setFirstResult(0).setMaxResults(1).getSingleResult();
    }

    public ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, ServerLevelQuestEntity, ServerLevelQuestConfig> getServerLevelQuestCrud() {
        ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, ServerLevelQuestEntity, ServerLevelQuestConfig> crud = serverLevelQuestCrudInstance.get();
        crud.setRootProvider(this::read).setParentProvider(entityManager1 -> read());
        crud.setEntitiesGetter((entityManager) -> read().getServerQuestEntities());
        crud.setEntitiesSetter((entityManager, serverLevelQuestEntities) -> read().setServerQuestEntities(serverLevelQuestEntities));
        crud.setEntityIdProvider(ServerLevelQuestEntity::getId).setConfigIdProvider(ServerLevelQuestConfig::getId);
        crud.setConfigGenerator(ServerLevelQuestEntity::toServerLevelQuestConfig);
        crud.setEntityFactory(ServerLevelQuestEntity::new);
        crud.setEntityFiller((serverLevelQuestEntity, serverLevelQuestConfig) -> {
            serverLevelQuestEntity.setInternalName(serverLevelQuestConfig.getInternalName());
            serverLevelQuestEntity.setMinimalLevel(levelPersistence.getLevel4Id(serverLevelQuestConfig.getMinimalLevelId()));
        });
        return crud;
    }

    public ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerLevelQuestEntity, QuestConfigEntity, QuestConfig> getServerQuestCrud(int serverLevelQuestEntityId, Locale locale) {
        ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerLevelQuestEntity, QuestConfigEntity, QuestConfig> crud = serverQuestCrudInstance.get();
        crud.setRootProvider(this::read);
        crud.setParentProvider(entityManager -> entityManager.find(ServerLevelQuestEntity.class, serverLevelQuestEntityId));
        crud.setEntitiesGetter(ServerLevelQuestEntity::getQuestConfigs).setEntitiesSetter(ServerLevelQuestEntity::setQuestConfigs);
        crud.setEntityIdProvider(QuestConfigEntity::getId).setConfigIdProvider(QuestConfig::getId);
        crud.setConfigGenerator(questConfigEntity -> questConfigEntity.toQuestConfig(locale));
        crud.setEntityFactory(QuestConfigEntity::new);
        crud.setEntityFiller((questConfigEntity, questConfig) -> questConfigEntity.fromQuestConfig(itemTypePersistence, questConfig, locale));
        return crud;
    }
}
