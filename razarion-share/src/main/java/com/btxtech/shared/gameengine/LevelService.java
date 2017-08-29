package com.btxtech.shared.gameengine;

import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.ArrayList;
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

    public void onGameEngineInit(@Observes StaticGameInitEvent engineInitEvent) {
        init(engineInitEvent.getStaticGameConfig());
    }

    public void init(StaticGameConfig staticGameConfig) {
        levels.clear();
        orderedLevels.clear();
        for (LevelConfig levelConfig : staticGameConfig.getLevelConfigs()) {
            levels.put(levelConfig.getLevelId(), levelConfig);
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
