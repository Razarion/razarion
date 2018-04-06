package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.PlanetPersistence;
import com.btxtech.server.persistence.bot.BotConfigEntity;
import com.btxtech.server.persistence.bot.BotConfigEntity_;
import com.btxtech.server.persistence.bot.BotSceneConfigEntity;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelEntity_;
import com.btxtech.server.persistence.level.LevelPersistence;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity_;
import com.btxtech.server.user.HumanPlayerIdEntity_;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.server.user.UserEntity;
import com.btxtech.server.user.UserEntity_;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.dto.SlavePlanetConfig;
import com.btxtech.shared.dto.StartRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConfig;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 09.05.2017.
 */
@Singleton
public class ServerGameEnginePersistence {
    // private Logger logger = Logger.getLogger(ServerGameEnginePersistence.class.getName());
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
    @Inject
    private Instance<ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, ServerResourceRegionConfigEntity, ResourceRegionConfig>> resourceRegionCrud;
    @Inject
    private Instance<ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, BotConfigEntity, BotConfig>> botConfigCrud;
    @Inject
    private Instance<ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, BotSceneConfigEntity, BotSceneConfig>> botSceneConfigCrud;
    @Inject
    private Instance<ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, ServerBoxRegionConfigEntity, BoxRegionConfig>> boxRegionCrud;

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
    public Map<Integer,String> getAllBotName2Id() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
        Root<BotConfigEntity> root = cq.from(BotConfigEntity.class);
        cq.multiselect(root.get(BotConfigEntity_.id), root.get(BotConfigEntity_.internalName));
        Map<Integer, String> botId2Names = new HashMap<>();
        entityManager.createQuery(cq).getResultList().forEach(tuple -> {
            String name = tuple.get(1) != null ? tuple.get(1).toString() : null;
            if (name != null) {
                botId2Names.put((int) tuple.get(0), name);
            }
        });
        return botId2Names;
    }

    @Transactional
    public Collection<BotSceneConfig> readBotSceneConfigs() {
        return read().getBotSceneConfigs();
    }

    @Transactional
    public Collection<BoxRegionConfig> readBoxRegionConfigs() {
        return read().getBoxRegionConfigs();
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

    @Transactional
    public Collection<Integer> readAllQuestIds() {
        // ServerGameEngineConfigEntity is not considered in this query
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> userQuery = criteriaBuilder.createQuery(Integer.class);
        Root<ServerLevelQuestEntity> root = userQuery.from(ServerLevelQuestEntity.class);
        CriteriaQuery<Integer> userSelect = userQuery.select(root.join(ServerLevelQuestEntity_.questConfigs).get(QuestConfigEntity_.id));
        return entityManager.createQuery(userSelect).getResultList();
    }

    public QuestConfigEntity getQuest4LevelAndCompleted(LevelEntity level, Collection<Integer> completedQuests) {
        // Does not work if there are multiple ServerGameEngineConfigEntity with same levels on ServerLevelQuestEntity
        // ServerGameEngineConfigEntity is not considered in this query
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> criteriaQuery = criteriaBuilder.createQuery(Integer.class);
        Root<ServerLevelQuestEntity> root = criteriaQuery.from(ServerLevelQuestEntity.class);
        ListJoin<ServerLevelQuestEntity, QuestConfigEntity> listJoin = root.join(ServerLevelQuestEntity_.questConfigs);
        CriteriaQuery<Integer> userSelect = criteriaQuery.select(listJoin.get(QuestConfigEntity_.id));
        userSelect.where(criteriaBuilder.lessThanOrEqualTo(root.join(ServerLevelQuestEntity_.minimalLevel).get(LevelEntity_.number), level.getNumber()));
        criteriaQuery.orderBy(criteriaBuilder.asc(listJoin.index()));
        List<Integer> questIds = entityManager.createQuery(userSelect).getResultList();
        if (questIds.isEmpty()) {
            return null;
        }
        if (completedQuests != null) {
            questIds.removeAll(completedQuests);
        }
        if (questIds.isEmpty()) {
            return null;
        }
        return entityManager.find(QuestConfigEntity.class, questIds.get(0));
    }

    @Transactional
    public List<QuestConfig> getQuests4Dialog(LevelEntity level, Collection<Integer> ignoreQuests, Locale locale) {
        // Does not work if there are multiple ServerGameEngineConfigEntity with same levels on ServerLevelQuestEntity
        // ServerGameEngineConfigEntity is not considered in this query
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> criteriaQuery = criteriaBuilder.createQuery(Integer.class);
        Root<ServerLevelQuestEntity> root = criteriaQuery.from(ServerLevelQuestEntity.class);
        ListJoin<ServerLevelQuestEntity, QuestConfigEntity> listJoin = root.join(ServerLevelQuestEntity_.questConfigs);
        CriteriaQuery<Integer> userSelect = criteriaQuery.select(listJoin.get(QuestConfigEntity_.id));
        userSelect.where(criteriaBuilder.lessThanOrEqualTo(root.join(ServerLevelQuestEntity_.minimalLevel).get(LevelEntity_.number), level.getNumber()));
        criteriaQuery.orderBy(criteriaBuilder.asc(listJoin.index()));
        List<Integer> questIds = entityManager.createQuery(userSelect).getResultList();
        if (questIds.isEmpty()) {
            return Collections.emptyList();
        }
        if (ignoreQuests != null) {
            questIds.removeAll(ignoreQuests);
        }
        if (questIds.isEmpty()) {
            return Collections.emptyList();
        }
        return questIds.stream().map(questId -> entityManager.find(QuestConfigEntity.class, questId).toQuestConfig(locale)).collect(Collectors.toList());
    }

    @Transactional
    public QuestConfig getAndVerifyQuest(int levelId, int questId, Locale locale) {
        // Does not work if there are multiple ServerGameEngineConfigEntity with same levels on ServerLevelQuestEntity
        // ServerGameEngineConfigEntity is not considered in this query
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LevelEntity> userQuery = criteriaBuilder.createQuery(LevelEntity.class);
        Root<ServerLevelQuestEntity> root = userQuery.from(ServerLevelQuestEntity.class);
        CriteriaQuery<LevelEntity> userSelect = userQuery.select(root.join(ServerLevelQuestEntity_.minimalLevel));
        userSelect.where(criteriaBuilder.equal(root.join(ServerLevelQuestEntity_.questConfigs).get(QuestConfigEntity_.id), questId));
        LevelEntity questLevelEntity = entityManager.createQuery(userSelect).getSingleResult();
        LevelEntity userLevelEntity = levelPersistence.read(levelId);
        if (userLevelEntity.getNumber() < questLevelEntity.getNumber()) {
            throw new IllegalArgumentException("The user is not allowed to activate a quest due to wrong level. questLevelEntity: " + questLevelEntity + " userLevelEntity: " + userLevelEntity);
        }
        return entityManager.find(QuestConfigEntity.class, questId).toQuestConfig(locale);
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
        crud.setAdditionalDelete((entityManager, integer) -> entityManager.remove(entityManager.find(QuestConfigEntity.class, integer)));
        return crud;
    }

    public ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, ServerResourceRegionConfigEntity, ResourceRegionConfig> getResourceRegionConfigCrud() {
        ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, ServerResourceRegionConfigEntity, ResourceRegionConfig> crud = resourceRegionCrud.get();
        crud.setRootProvider(this::read).setParentProvider(entityManager -> read());
        crud.setEntitiesGetter((entityManager) -> read().getResourceRegionConfigs());
        crud.setEntitiesSetter((entityManager, resourceRegionConfigs) -> read().setResourceRegionConfigs(resourceRegionConfigs));
        crud.setEntityIdProvider(ServerResourceRegionConfigEntity::getId).setConfigIdProvider(ResourceRegionConfig::getId);
        crud.setConfigGenerator(ServerResourceRegionConfigEntity::toResourceRegionConfig);
        crud.setEntityFactory(ServerResourceRegionConfigEntity::new);
        crud.setEntityFiller((serverResourceRegionConfigEntity, resourceRegionConfig) -> {
            serverResourceRegionConfigEntity.fromResourceRegionConfig(itemTypePersistence, resourceRegionConfig);
        });
        return crud;
    }

    public ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, BotConfigEntity, BotConfig> getBotConfigCrud() {
        ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, BotConfigEntity, BotConfig> crud = botConfigCrud.get();
        crud.setRootProvider(this::read).setParentProvider(entityManager -> read());
        crud.setEntitiesGetter((entityManager) -> read().getBotConfigEntities());
        crud.setEntitiesSetter((entityManager, botConfigs) -> read().setBotConfigEntities(botConfigs));
        crud.setEntityIdProvider(BotConfigEntity::getId).setConfigIdProvider(BotConfig::getId);
        crud.setConfigGenerator(BotConfigEntity::toBotConfig);
        crud.setEntityFactory(() -> new BotConfigEntity().setAutoAttack(true));
        crud.setEntityFiller((botConfigEntity, botConfig) -> {
            botConfigEntity.fromBotConfig(itemTypePersistence, botConfig);
        });
        return crud;
    }

    public ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, BotSceneConfigEntity, BotSceneConfig> getBotSceneConfigCrud() {
        ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, BotSceneConfigEntity, BotSceneConfig> crud = botSceneConfigCrud.get();
        crud.setRootProvider(this::read).setParentProvider(entityManager -> read());
        crud.setEntitiesGetter((entityManager) -> read().getBotSceneConfigEntities());
        crud.setEntitiesSetter((entityManager, botConfigs) -> read().setBotSceneConfigEntities(botConfigs));
        crud.setEntityIdProvider(BotSceneConfigEntity::getId).setConfigIdProvider(BotSceneConfig::getId);
        crud.setConfigGenerator(BotSceneConfigEntity::toBotSceneConfig);
        crud.setEntityFactory(BotSceneConfigEntity::new);
        crud.setEntityFiller((botSceneConfigEntity, botSceneConfig) -> {
            botSceneConfigEntity.fromBotConfig(itemTypePersistence, entityManager, botSceneConfig);
        });
        return crud;
    }

    public ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, ServerBoxRegionConfigEntity, BoxRegionConfig> getBoxRegionConfigCrud() {
        ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, ServerBoxRegionConfigEntity, BoxRegionConfig> crud = boxRegionCrud.get();
        crud.setRootProvider(this::read).setParentProvider(entityManager -> read());
        crud.setEntitiesGetter((entityManager) -> read().getServerBoxRegionConfigEntities());
        crud.setEntitiesSetter((entityManager, boxRegionConfigEntities) -> read().setServerBoxRegionConfigEntities(boxRegionConfigEntities));
        crud.setEntityIdProvider(ServerBoxRegionConfigEntity::getId).setConfigIdProvider(BoxRegionConfig::getId);
        crud.setConfigGenerator(ServerBoxRegionConfigEntity::toBoxRegionConfig);
        crud.setEntityFactory(ServerBoxRegionConfigEntity::new);
        crud.setEntityFiller((serverBoxRegionConfigEntity, boxRegionConfig) -> {
            serverBoxRegionConfigEntity.fromBoxRegionConfig(itemTypePersistence, boxRegionConfig);
        });
        return crud;
    }
}
