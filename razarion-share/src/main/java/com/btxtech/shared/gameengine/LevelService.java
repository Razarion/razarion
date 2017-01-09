package com.btxtech.shared.gameengine;

import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 23.07.2016.
 */
@ApplicationScoped
public class LevelService {
    private Map<Integer, LevelConfig> levels = new HashMap<>();
    private List<LevelConfig> orderedLevels = new ArrayList<>();

    public void onGameEngineInit(@Observes GameEngineInitEvent engineInitEvent) {
        init(engineInitEvent.getGameEngineConfig());
    }

    public void init(GameEngineConfig gameEngineConfig) {
        levels.clear();
        orderedLevels.clear();
        for (LevelConfig levelConfig : gameEngineConfig.getLevelConfigs()) {
            levels.put(levelConfig.getLevelId(), levelConfig);
            orderedLevels.add(levelConfig);
        }
        Collections.sort(orderedLevels, Comparator.comparingInt(LevelConfig::getNumber));
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
}
