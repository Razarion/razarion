package com.btxtech.server.persistence.level;

import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 05.05.2017.
 */
@Singleton
public class LevelPersistence {
    @Inject
    private ItemTypePersistence itemTypePersistence;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public LevelEntity read(int id) {
        LevelEntity levelEntity = entityManager.find(LevelEntity.class, id);
        if (levelEntity == null) {
            throw new IllegalArgumentException("No Level for id: " + id);
        }
        return levelEntity;
    }

    @Transactional
    public List<LevelConfig> read() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LevelEntity> userQuery = criteriaBuilder.createQuery(LevelEntity.class);
        Root<LevelEntity> from = userQuery.from(LevelEntity.class);
        CriteriaQuery<LevelEntity> userSelect = userQuery.select(from);
        List<LevelEntity> itemTypeEntities = entityManager.createQuery(userSelect).getResultList();

        return itemTypeEntities.stream().map(LevelEntity::toLevelConfig).collect(Collectors.toList());
    }

    @Transactional
    @SecurityCheck
    public LevelConfig create() {
        LevelEntity levelEntity = new LevelEntity();
        entityManager.persist(levelEntity);
        return levelEntity.toLevelConfig();
    }

    @Transactional
    @SecurityCheck
    public LevelConfig update(LevelConfig levelConfig) {
        Map<BaseItemTypeEntity, Integer> itemTypeLimitation = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : levelConfig.getItemTypeLimitation().entrySet()) {
            itemTypeLimitation.put(itemTypePersistence.readBaseItemTypeEntity(entry.getKey()), entry.getValue());
        }
        LevelEntity levelEntity = read(levelConfig.getLevelId());
        levelEntity.fromLevelConfig(levelConfig, itemTypeLimitation);
        entityManager.merge(levelEntity);
        return levelEntity.toLevelConfig();
    }

    @Transactional
    @SecurityCheck
    public void delete(int id) {
        entityManager.remove(read(id));
    }

    @Transactional
    public LevelEntity getStarterLevel() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LevelEntity> userQuery = criteriaBuilder.createQuery(LevelEntity.class);
        Root<LevelEntity> from = userQuery.from(LevelEntity.class);
        CriteriaQuery<LevelEntity> userSelect = userQuery.select(from);
        userQuery.orderBy(criteriaBuilder.asc(from.get(LevelEntity_.number)));
        return entityManager.createQuery(userSelect).setFirstResult(0).setMaxResults(1).getSingleResult();
    }

    public LevelEntity getLevel4Id(int levelId) {
        LevelEntity levelEntity = entityManager.find(LevelEntity.class, levelId);
        if (levelEntity == null) {
            throw new IllegalArgumentException("No level for id: " + levelId);
        }
        return levelEntity;
    }

    @Transactional
    public int getLevelNumber4Id(int levelId) {
        return getLevel4Id(levelId).getNumber();
    }
}
