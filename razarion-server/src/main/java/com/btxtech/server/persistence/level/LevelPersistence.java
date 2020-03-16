package com.btxtech.server.persistence.level;

import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.mgmt.UnlockedBackendInfo;
import com.btxtech.server.persistence.CrudPersistence;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
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
public class LevelPersistence extends CrudPersistence<LevelConfig, LevelEntity> {
    @Inject
    private ItemTypePersistence itemTypePersistence;
    @Inject
    private ImagePersistence imagePersistence;
    @PersistenceContext
    private EntityManager entityManager;

    public LevelPersistence() {
        super(LevelEntity.class, LevelEntity_.id, LevelEntity_.internalName);
    }


    @Override
    protected LevelConfig toConfig(LevelEntity entity) {
        return entity.toLevelConfig();
    }

    @Override
    protected void fromConfig(LevelConfig config, LevelEntity entity) {
        Map<BaseItemTypeEntity, Integer> itemTypeLimitation = new HashMap<>();
        if (config.getItemTypeLimitation() != null) {
            for (Map.Entry<Integer, Integer> entry : config.getItemTypeLimitation().entrySet()) {
                itemTypeLimitation.put(itemTypePersistence.readBaseItemTypeEntity(entry.getKey()), entry.getValue());
            }
        }
        Collection<LevelUnlockEntity> levelUnlockEntities = new ArrayList<>();
//   TODO     LevelEntity levelEntity = read(levelEditConfig.getLevelId());
//        if (levelEditConfig.getLevelUnlockConfigs() != null) {
//            levelEditConfig.getLevelUnlockConfigs().forEach(levelUnlockConfig -> {
//                LevelUnlockEntity levelUnlockEntity = new LevelUnlockEntity();
//                levelUnlockEntity.setId(levelUnlockConfig.getId());
//                levelUnlockEntity.setInternalName(levelUnlockConfig.getInternalName());
//                levelUnlockEntity.setCrystalCost(levelUnlockConfig.getCrystalCost());
//                levelUnlockEntity.setBaseItemType(itemTypePersistence.readBaseItemTypeEntity(levelUnlockConfig.getBaseItemType()));
//                levelUnlockEntity.setBaseItemTypeCount(levelUnlockConfig.getBaseItemTypeCount());
//                levelUnlockEntity.setThumbnail(imagePersistence.getImageLibraryEntity(levelUnlockConfig.getThumbnail()));
//                levelUnlockEntity.setI18nName(I18nBundleEntity.fromI18nStringSafe(levelUnlockConfig.getI18nName(), levelUnlockEntity.getI18nName()));
//                levelUnlockEntity.setI18nDescription(I18nBundleEntity.fromI18nStringSafe(levelUnlockConfig.getI18nDescription(), levelUnlockEntity.getI18nDescription()));
//                levelUnlockEntities.add(levelUnlockEntity);
//            });
//        }
        entity.fromLevelConfig(config, itemTypeLimitation, levelUnlockEntities);
    }

    public LevelEntity getStarterLevel() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LevelEntity> userQuery = criteriaBuilder.createQuery(LevelEntity.class);
        Root<LevelEntity> from = userQuery.from(LevelEntity.class);
        CriteriaQuery<LevelEntity> userSelect = userQuery.select(from);
        userQuery.orderBy(criteriaBuilder.asc(from.get(LevelEntity_.number)));
        return entityManager.createQuery(userSelect).setFirstResult(0).setMaxResults(1).getResultList().stream().findFirst().orElse(null);
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
    public List<LevelUnlockConfig> readUnlocks(int levelId, Collection<Integer> unlockedEntityIds) {
        LevelEntity levelEntity = getEntity(levelId);
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
        LevelUnlockEntity levelUnlockEntity = entityManager.find(LevelUnlockEntity.class, levelUnlockId);
        return new UnlockedBackendInfo().setId(levelUnlockEntity.getId()).setInternalName(levelUnlockEntity.getInternalName());
    }
}
