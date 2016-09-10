package com.btxtech.shared.gameengine;

import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 23.07.2016.
 */
@ApplicationScoped
public class LevelService {
    private Map<Integer, LevelConfig> levels = new HashMap<>();

    public void onGameEngineInit(@Observes GameEngineInitEvent engineInitEvent) {
        levels.clear();
        for (LevelConfig levelConfig : engineInitEvent.getGameEngineConfig().getLevelConfigs()) {
            levels.put(levelConfig.getLevelId(), levelConfig);
        }
    }

    public LevelConfig getLevel(int levelId) {
        LevelConfig level = levels.get(levelId);
        if (level == null) {
            throw new IllegalArgumentException("No level for id: " + levelId);
        }
        return level;
    }

    public LevelConfig getLevel(PlayerBase playerBase) {
        return getLevel(playerBase.getUserContext().getLevelId());
    }
}
