package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.BaseItemTypeEntity;
import com.btxtech.server.model.engine.LevelEntity;
import com.btxtech.server.repository.engine.LevelRepository;
import com.btxtech.server.service.ui.ImagePersistence;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LevelCrudPersistence extends AbstractConfigCrudPersistence<LevelConfig, LevelEntity> {
    private final BaseItemTypeCrudPersistence baseItemTypeCrudPersistence;
    private final ImagePersistence imagePersistence;

    public LevelCrudPersistence(LevelRepository levelRepository,
                                BaseItemTypeCrudPersistence baseItemTypeCrudPersistence,
                                ImagePersistence imagePersistence) {
        super(LevelEntity.class, levelRepository);
        this.baseItemTypeCrudPersistence = baseItemTypeCrudPersistence;
        this.imagePersistence = imagePersistence;
    }

    @Override
    protected LevelConfig toConfig(LevelEntity entity) {
        return entity.toLevelEditConfig();
    }

    @Override
    protected void fromConfig(LevelConfig config, LevelEntity entity) {
        Map<BaseItemTypeEntity, Integer> itemTypeLimitation = new HashMap<>();
        if (config.getItemTypeLimitation() != null) {
            for (Map.Entry<Integer, Integer> entry : config.getItemTypeLimitation().entrySet()) {
                itemTypeLimitation.put(baseItemTypeCrudPersistence.getEntity(entry.getKey()), entry.getValue());
            }
        }
        entity.fromLevelEditConfig(config, itemTypeLimitation, baseItemTypeCrudPersistence, imagePersistence);
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

    public LevelEntity getStarterLevel() {
        return ((LevelRepository) getJpaRepository()).findTopByOrderByNumberAsc();
    }

    public int getLevelNumber4Id(int levelId) {
        return ((LevelRepository) getJpaRepository()).getLevelNumberByLevelId(levelId);
    }

    public LevelEntity getNextLevel(LevelEntity level) {
//        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//        CriteriaQuery<LevelEntity> userQuery = criteriaBuilder.createQuery(LevelEntity.class);
//        Root<LevelEntity> from = userQuery.from(LevelEntity.class);
//        CriteriaQuery<LevelEntity> userSelect = userQuery.select(from);
//        userSelect.where(criteriaBuilder.greaterThan(from.get(LevelEntity_.number), level.getNumber()));
//        userSelect.orderBy(criteriaBuilder.asc(from.get(LevelEntity_.number)));
//        try {
//            return entityManager.createQuery(userSelect).setMaxResults(1).getSingleResult();
//        } catch (NoResultException e) {
//            return null;
//        }
        throw new UnsupportedOperationException("... TODO ...");
    }

    @Transactional
    public boolean hasAvailableUnlocks(int levelId, Collection<Integer> unlockedEntityIds) {
        return !readAvailableLevelUnlockConfigs(levelId, unlockedEntityIds).isEmpty();
    }

    @Transactional
    public List<LevelUnlockConfig> readAvailableLevelUnlockConfigs(int levelId, Collection<Integer> unlockedEntityIds) {
//        LevelEntity levelEntity = getEntity(levelId);
//        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//        CriteriaQuery<LevelUnlockEntity> criteriaQuery = criteriaBuilder.createQuery(LevelUnlockEntity.class);
//        Root<LevelEntity> root = criteriaQuery.from(LevelEntity.class);
//        ListJoin<LevelEntity, LevelUnlockEntity> listJoin = root.join(LevelEntity_.levelUnlockEntities);
//        CriteriaQuery<LevelUnlockEntity> userSelect = criteriaQuery.select(listJoin);
//        Predicate levelNumber = criteriaBuilder.lessThanOrEqualTo(root.get(LevelEntity_.number), levelEntity.getNumber());
//        if (unlockedEntityIds != null && !unlockedEntityIds.isEmpty()) {
//            Predicate notIn = criteriaBuilder.not(listJoin.get(LevelUnlockEntity_.id).in(unlockedEntityIds));
//            userSelect.where(criteriaBuilder.and(levelNumber, notIn));
//        } else {
//            userSelect.where(levelNumber);
//        }
//        return entityManager.createQuery(userSelect)
//                .getResultList()
//                .stream()
//                .map(LevelUnlockEntity::toLevelUnlockConfig)
//                .collect(Collectors.toList());
        throw new UnsupportedOperationException("... TODO ...");
    }

}
