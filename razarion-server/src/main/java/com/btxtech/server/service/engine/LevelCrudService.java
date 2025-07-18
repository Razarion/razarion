package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.BaseItemTypeEntity;
import com.btxtech.server.model.engine.LevelEntity;
import com.btxtech.server.model.engine.LevelUnlockEntity;
import com.btxtech.server.repository.engine.LevelRepository;
import com.btxtech.server.service.ui.ImageService;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LevelCrudService extends AbstractConfigCrudService<LevelConfig, LevelEntity> {
    private final BaseItemTypeService baseItemTypeCrudPersistence;
    private final ImageService imageService;

    public LevelCrudService(LevelRepository levelRepository,
                            BaseItemTypeService baseItemTypeCrudPersistence,
                            ImageService imageService) {
        super(LevelEntity.class, levelRepository);
        this.baseItemTypeCrudPersistence = baseItemTypeCrudPersistence;
        this.imageService = imageService;
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
        entity.fromLevelEditConfig(config, itemTypeLimitation, baseItemTypeCrudPersistence, imageService);
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
        return ((LevelRepository) getJpaRepository())
                .getNextLevel(level.getId(), PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public boolean hasAvailableUnlocks(int levelId, Collection<Integer> unlockedEntityIds) {
        return !readAvailableLevelUnlockConfigs(levelId, unlockedEntityIds).isEmpty();
    }

    @Transactional
    public List<LevelUnlockConfig> readAvailableLevelUnlockConfigs(int levelId, Collection<Integer> unlockedEntityIds) {
        LevelEntity levelEntity = getEntity(levelId);
        return ((LevelRepository) getJpaRepository()).findLockedUnlocks(levelEntity.getNumber(), unlockedEntityIds)
                .stream()
                .map(LevelUnlockEntity::toLevelUnlockConfig)
                .collect(Collectors.toList());
    }

    public LevelUnlockEntity readLevelUnlockEntity(int levelUnlockEntityId) {
        return ((LevelRepository) getJpaRepository()).findLevelUnlockEntity(levelUnlockEntityId);
    }

    @Transactional
    public int readLevelUnlockEntityCrystals(int levelUnlockEntityId) {
        return readLevelUnlockEntity(levelUnlockEntityId).getCrystalCost();
    }
}
