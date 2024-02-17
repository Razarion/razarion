package com.btxtech.server.persistence.level;

import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.mgmt.UnlockedBackendInfo;
import com.btxtech.server.persistence.AbstractCrudPersistence;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.itemtype.BaseItemTypeCrudPersistence;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelEditConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 05.05.2017.
 */
@Singleton
public class LevelCrudPersistence extends AbstractCrudPersistence<LevelEditConfig, LevelEntity> {
    @Inject
    private BaseItemTypeCrudPersistence baseItemTypeCrudPersistence;
    @Inject
    private ImagePersistence imagePersistence;
    @Inject
    private AlarmService alarmService;
    @PersistenceContext
    private EntityManager entityManager;

    public LevelCrudPersistence() {
        super(LevelEntity.class, LevelEntity_.id, null);
    }

    @Transactional
    public List<ObjectNameId> getObjectNameIds() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
        Root<LevelEntity> root = cq.from(LevelEntity.class);
        cq.multiselect(root.get(LevelEntity_.id), root.get(LevelEntity_.number));
        List<Tuple> tupleResult = entityManager.createQuery(cq).getResultList();
        return tupleResult.stream().map(t -> new ObjectNameId(((int) t.get(0)), t.get(1) != null ? t.get(1).toString() : "")).collect(Collectors.toList());
    }

    @Override
    protected LevelEditConfig toConfig(LevelEntity entity) {
        return entity.toLevelEditConfig();
    }

    @Override
    protected void fromConfig(LevelEditConfig config, LevelEntity entity) {
        Map<BaseItemTypeEntity, Integer> itemTypeLimitation = new HashMap<>();
        if (config.getItemTypeLimitation() != null) {
            for (Map.Entry<Integer, Integer> entry : config.getItemTypeLimitation().entrySet()) {
                itemTypeLimitation.put(baseItemTypeCrudPersistence.getEntity(entry.getKey()), entry.getValue());
            }
        }
        entity.fromLevelEditConfig(config, itemTypeLimitation, baseItemTypeCrudPersistence, imagePersistence);
    }

    public LevelEntity getStarterLevel() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LevelEntity> userQuery = criteriaBuilder.createQuery(LevelEntity.class);
        Root<LevelEntity> from = userQuery.from(LevelEntity.class);
        CriteriaQuery<LevelEntity> userSelect = userQuery.select(from);
        userQuery.orderBy(criteriaBuilder.asc(from.get(LevelEntity_.number)));
        LevelEntity startLevel = entityManager.createQuery(userSelect).setFirstResult(0).setMaxResults(1).getResultList().stream().findFirst().orElse(null);
        if (startLevel == null) {
            alarmService.riseAlarm(Alarm.Type.NO_LEVELS);
        }
        return startLevel;
    }

    @Transactional
    public List<LevelConfig> readLevelConfigs() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LevelEntity> userQuery = criteriaBuilder.createQuery(LevelEntity.class);
        Root<LevelEntity> root = userQuery.from(LevelEntity.class);
        CriteriaQuery<LevelEntity> userSelect = userQuery.select(root);
        Collection<LevelEntity> slopeConfigEntities = entityManager.createQuery(userSelect).getResultList();
        return slopeConfigEntities.stream().map(LevelEntity::toLevelConfig).collect(Collectors.toList());
    }

    @Transactional
    public Integer getStarterLevelId() {
        LevelEntity starterLevel = getStarterLevel();
        if (starterLevel != null) {
            return starterLevel.getId();
        } else {
            return null;
        }
    }

    @Transactional
    public List<LevelUnlockEntity> getStartUnlockedItemLimit() {
        return new ArrayList<>();
    }

    public LevelEntity getLevel4Number(int levelNumber) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LevelEntity> userQuery = criteriaBuilder.createQuery(LevelEntity.class);
        Root<LevelEntity> from = userQuery.from(LevelEntity.class);
        CriteriaQuery<LevelEntity> userSelect = userQuery.select(from);
        userSelect.where(criteriaBuilder.equal(from.get(LevelEntity_.number), levelNumber));
        return entityManager.createQuery(userSelect).getSingleResult();
    }

    @Transactional
    public int getLevelNumber4Id(int levelId) {
        return getEntity(levelId).getNumber();
    }

    public LevelEntity getNextLevel(LevelEntity level) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LevelEntity> userQuery = criteriaBuilder.createQuery(LevelEntity.class);
        Root<LevelEntity> from = userQuery.from(LevelEntity.class);
        CriteriaQuery<LevelEntity> userSelect = userQuery.select(from);
        userSelect.where(criteriaBuilder.greaterThan(from.get(LevelEntity_.number), level.getNumber()));
        userSelect.orderBy(criteriaBuilder.asc(from.get(LevelEntity_.number)));
        try {
            return entityManager.createQuery(userSelect).setMaxResults(1).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Transactional
    public int readLevelUnlockEntityCrystals(int levelUnlockEntityId) {
        return readLevelUnlockEntity(levelUnlockEntityId).getCrystalCost();
    }

    public LevelUnlockEntity readLevelUnlockEntity(int levelUnlockEntityId) {
        return entityManager.find(LevelUnlockEntity.class, levelUnlockEntityId);
    }

    @Transactional
    public Map<Integer, Integer> setupUnlockedItemLimit(Collection<Integer> levelUnlockEntityIds) {
        if (levelUnlockEntityIds == null) {
            return Collections.emptyMap();
        }
        Collection<LevelUnlockEntity> levelUnlockEntities = levelUnlockEntityIds.stream().map(levelUnlockEntityId -> entityManager.find(LevelUnlockEntity.class, levelUnlockEntityId)).collect(Collectors.toList());
        return ServerUnlockService.convertUnlockedItemLimit(levelUnlockEntities);
    }

    @Transactional
    public boolean hasAvailableUnlocks(int levelId, Collection<Integer> unlockedEntityIds) {
        return !readAvailableLevelUnlockConfigs(levelId, unlockedEntityIds).isEmpty();
    }

    @Transactional
    public List<LevelUnlockConfig> readAvailableLevelUnlockConfigs(int levelId, Collection<Integer> unlockedEntityIds) {
        LevelEntity levelEntity = getEntity(levelId);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LevelUnlockEntity> criteriaQuery = criteriaBuilder.createQuery(LevelUnlockEntity.class);
        Root<LevelEntity> root = criteriaQuery.from(LevelEntity.class);
        ListJoin<LevelEntity, LevelUnlockEntity> listJoin = root.join(LevelEntity_.levelUnlockEntities);
        CriteriaQuery<LevelUnlockEntity> userSelect = criteriaQuery.select(listJoin);
        Predicate levelNumber = criteriaBuilder.lessThanOrEqualTo(root.get(LevelEntity_.number), levelEntity.getNumber());
        if (unlockedEntityIds != null && !unlockedEntityIds.isEmpty()) {
            Predicate notIn = criteriaBuilder.not(listJoin.get(LevelUnlockEntity_.id).in(unlockedEntityIds));
            userSelect.where(criteriaBuilder.and(levelNumber, notIn));
        } else {
            userSelect.where(levelNumber);
        }
        return entityManager.createQuery(userSelect)
                .getResultList()
                .stream()
                .map(LevelUnlockEntity::toLevelUnlockConfig)
                .collect(Collectors.toList());
    }

    @Transactional
    public UnlockedBackendInfo findUnlockedBackendInfo(int levelUnlockId) {
        LevelUnlockEntity levelUnlockEntity = entityManager.find(LevelUnlockEntity.class, levelUnlockId);
        return new UnlockedBackendInfo().setId(levelUnlockEntity.getId()).setInternalName(levelUnlockEntity.getInternalName());
    }
}
