package com.btxtech.server.persistence.level;

import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.mgmt.UnlockedBackendInfo;
import com.btxtech.server.mgmt.UserBackendInfo;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.tracker.I18nBundleEntity;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelEditConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
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
public class LevelPersistence {
    @Inject
    private ItemTypePersistence itemTypePersistence;
    @Inject
    private ImagePersistence imagePersistence;
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
    public LevelEditConfig readLevelConfig(int id) {
        return read(id).toLevelEditConfig();
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
    public LevelEditConfig create() {
        LevelEntity levelEntity = new LevelEntity();
        entityManager.persist(levelEntity);
        return levelEntity.toLevelEditConfig();
    }

    @Transactional
    @SecurityCheck
    public LevelConfig update(LevelEditConfig levelEditConfig) {
        Map<BaseItemTypeEntity, Integer> itemTypeLimitation = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : levelEditConfig.getItemTypeLimitation().entrySet()) {
            itemTypeLimitation.put(itemTypePersistence.readBaseItemTypeEntity(entry.getKey()), entry.getValue());
        }
        LevelEntity levelEntity = read(levelEditConfig.getLevelId());
        Collection<LevelUnlockEntity> levelUnlockEntities = new ArrayList<>();
        if (levelEditConfig.getLevelUnlockConfigs() != null) {
            levelEditConfig.getLevelUnlockConfigs().forEach(levelUnlockConfig -> {
                LevelUnlockEntity levelUnlockEntity = new LevelUnlockEntity();
                levelUnlockEntity.setId(levelUnlockConfig.getId());
                levelUnlockEntity.setInternalName(levelUnlockConfig.getInternalName());
                levelUnlockEntity.setCrystalCost(levelUnlockConfig.getCrystalCost());
                levelUnlockEntity.setBaseItemType(itemTypePersistence.readBaseItemTypeEntity(levelUnlockConfig.getBaseItemType()));
                levelUnlockEntity.setBaseItemTypeCount(levelUnlockConfig.getBaseItemTypeCount());
                levelUnlockEntity.setThumbnail(imagePersistence.getImageLibraryEntity(levelUnlockConfig.getThumbnail()));
                levelUnlockEntity.setI18nName(I18nBundleEntity.fromI18nStringSafe(levelUnlockConfig.getI18nName(), levelUnlockEntity.getI18nName()));
                levelUnlockEntity.setI18nDescription(I18nBundleEntity.fromI18nStringSafe(levelUnlockConfig.getI18nDescription(), levelUnlockEntity.getI18nDescription()));
                levelUnlockEntities.add(levelUnlockEntity);
            });
        }
        levelEntity.fromLevelEditConfig(levelEditConfig, itemTypeLimitation, levelUnlockEntities);
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

    @Transactional
    public List<LevelUnlockEntity> getStartUnlockedItemLimit() {
        return new ArrayList<>();
    }

    public LevelEntity getLevel4Id(Integer levelId) {
        if (levelId == null) {
            return null;
        }
        LevelEntity levelEntity = entityManager.find(LevelEntity.class, levelId);
        if (levelEntity == null) {
            throw new IllegalArgumentException("No level for id: " + levelId);
        }
        return levelEntity;
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
        return getLevel4Id(levelId).getNumber();
    }

    @Transactional
    public List<ObjectNameId> readObjectNameIds() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
        Root<LevelEntity> root = cq.from(LevelEntity.class);
        cq.multiselect(root.get(LevelEntity_.id), root.get(LevelEntity_.number));
        List<Tuple> tupleResult = entityManager.createQuery(cq).getResultList();
        return tupleResult.stream().map(t -> new ObjectNameId((int) t.get(0), Integer.toString((int) t.get(1)))).collect(Collectors.toList());
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
    public int readLevelUnlockEntityCrystals(int levelId, int levelUnlockEntityId) {
        return getLevel4Id(levelId).getLevelUnlockEntity(levelUnlockEntityId).getCrystalCost();
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
    public List<LevelUnlockConfig> readUnlocks(int levelId, Collection<Integer> unlockedEntityIds) {
        LevelEntity levelEntity = getLevel4Id(levelId);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LevelUnlockEntity> criteriaQuery = criteriaBuilder.createQuery(LevelUnlockEntity.class);
        Root<LevelEntity> root = criteriaQuery.from(LevelEntity.class);
        CollectionJoin<LevelEntity, LevelUnlockEntity> collectionJoin = root.join(LevelEntity_.levelUnlockEntities);
        CriteriaQuery<LevelUnlockEntity> userSelect = criteriaQuery.select(collectionJoin);
        Predicate levelNumber = criteriaBuilder.lessThanOrEqualTo(root.get(LevelEntity_.number), levelEntity.getNumber());
        if (unlockedEntityIds != null && !unlockedEntityIds.isEmpty()) {
            Predicate notIn = criteriaBuilder.not(collectionJoin.get(LevelUnlockEntity_.id).in(unlockedEntityIds));
            userSelect.where(criteriaBuilder.and(levelNumber, notIn));
        } else {
            userSelect.where(levelNumber);
        }
        return entityManager.createQuery(userSelect).getResultList().stream().map(LevelUnlockEntity::toLevelUnlockConfig).collect(Collectors.toList());
    }

    @Transactional
    public UnlockedBackendInfo findUnlockedBackendInfo(int levelUnlockId) {
        LevelUnlockEntity levelUnlockEntity =  entityManager.find(LevelUnlockEntity.class,levelUnlockId);
        return new UnlockedBackendInfo().setId(levelUnlockEntity.getId()).setInternalName(levelUnlockEntity.getInternalName());
    }
}
