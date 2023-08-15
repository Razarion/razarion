package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.AbstractCrudPersistence;
import com.btxtech.server.persistence.PlanetCrudPersistence;
import com.btxtech.server.persistence.bot.BotConfigEntity;
import com.btxtech.server.persistence.bot.BotConfigEntity_;
import com.btxtech.server.persistence.bot.BotSceneConfigEntity;
import com.btxtech.server.persistence.itemtype.BaseItemTypeCrudPersistence;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.itemtype.ResourceItemTypeCrudPersistence;
import com.btxtech.server.persistence.level.LevelCrudPersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelEntity_;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity_;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.ServerGameEngineConfig;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.dto.SlavePlanetConfig;
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
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 09.05.2017.
 */
@Singleton
public class ServerGameEngineCrudPersistence extends AbstractCrudPersistence<ServerGameEngineConfig, ServerGameEngineConfigEntity> {
    private Logger logger = Logger.getLogger(ServerGameEngineCrudPersistence.class.getName());
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private PlanetCrudPersistence planetCrudPersistence;
    @Inject
    private BaseItemTypeCrudPersistence baseItemTypeCrudPersistence;
    @Inject
    private ResourceItemTypeCrudPersistence resourceItemTypeCrudPersistence;
    @Inject
    private ItemTypePersistence itemTypePersistence;
    @Inject
    private LevelCrudPersistence levelCrudPersistence;
    @Inject
    private Instance<ServerChildCrudPersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, ServerLevelQuestEntity, ServerLevelQuestConfig>> serverLevelQuestCrudInstance;
    @Inject
    private Instance<ServerChildCrudPersistence<ServerGameEngineConfigEntity, ServerLevelQuestEntity, QuestConfigEntity, QuestConfig>> serverQuestCrudInstance;
    @Inject
    private Instance<ServerChildCrudPersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, BotSceneConfigEntity, BotSceneConfig>> botSceneConfigCrud;
    @Inject
    private Instance<ServerChildCrudPersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, ServerBoxRegionConfigEntity, BoxRegionConfig>> boxRegionCrud;

    public ServerGameEngineCrudPersistence() {
        super(ServerGameEngineConfigEntity.class, ServerGameEngineConfigEntity_.id, ServerGameEngineConfigEntity_.internalName);
    }

    @Override
    protected ServerGameEngineConfig toConfig(ServerGameEngineConfigEntity entity) {
        return entity.toServerGameEngineConfig(Locale.GERMAN);
    }

    @Override
    protected void fromConfig(ServerGameEngineConfig config, ServerGameEngineConfigEntity entity) {
        entity.fromServerGameEngineConfig(config, planetCrudPersistence, resourceItemTypeCrudPersistence, levelCrudPersistence, baseItemTypeCrudPersistence, itemTypePersistence, Locale.GERMAN);
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
    @SecurityCheck
    public Map<Integer, String> getAllBotName2Id() {
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
        return serverGameEngineConfigEntity().getBotSceneConfigs();
    }

    @Transactional
    public Collection<BoxRegionConfig> readBoxRegionConfigs() {
        return serverGameEngineConfigEntity().getBoxRegionConfigs();
    }

    @Transactional
    @SecurityCheck
    public void updatePlanetConfig(Integer planetConfigId) {
        ServerGameEngineConfigEntity serverGameEngineConfigEntity = serverGameEngineConfigEntity();
        if (planetConfigId != null) {
            serverGameEngineConfigEntity.setPlanetEntity(planetCrudPersistence.getEntity(planetConfigId));
        } else {
            serverGameEngineConfigEntity.setPlanetEntity(null);
        }
        entityManager.merge(serverGameEngineConfigEntity);
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
        LevelEntity userLevelEntity = levelCrudPersistence.getEntity(levelId);
        if (userLevelEntity.getNumber() < questLevelEntity.getNumber()) {
            throw new IllegalArgumentException("The user is not allowed to activate a quest due to wrong level. questLevelEntity: " + questLevelEntity + " userLevelEntity: " + userLevelEntity);
        }
        return entityManager.find(QuestConfigEntity.class, questId).toQuestConfig(locale);
    }

    private ServerGameEngineConfigEntity serverGameEngineConfigEntity() {
        try {
            return getEntities().stream().findFirst().orElseThrow((Supplier<Throwable>) () -> new IllegalStateException("No ServerGameEngineConfigEntity in DB"));
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
