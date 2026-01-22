package com.btxtech.shared.gameengine;

import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.*;

/**
 * Created by Beat
 * 23.07.2016.
 */
@Singleton
public class LevelService {
    private final Map<Integer, LevelConfig> levels = new HashMap<>();
    private final List<LevelConfig> orderedLevels = new ArrayList<>();

    @Inject
    public LevelService(InitializeService initializeService) {
        initializeService.receiveStaticGameConfig(this::init);
    }

    public void init(StaticGameConfig staticGameConfig) {
        levels.clear();
        orderedLevels.clear();
        for (LevelConfig levelConfig : staticGameConfig.getLevelConfigs()) {
            levels.put(levelConfig.getId(), levelConfig);
            orderedLevels.add(levelConfig);
        }
        orderedLevels.sort(Comparator.comparingInt(LevelConfig::getNumber));
    }

    public LevelConfig getLevel(int levelId) {
        LevelConfig level = levels.get(levelId);
        if (level == null) {
            throw new IllegalArgumentException("No level for id: " + levelId);
        }
        return level;
    }

    public LevelConfig getNextLevel(LevelConfig level) {
        int newIndex = orderedLevels.indexOf(level) + 1;
        return orderedLevels.get(newIndex);
    }

    public List<LevelConfig> getOrderedLevels() {
        return orderedLevels;
    }
}
