package com.btxtech.shared.gameengine;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.LevelServiceListener;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 23.07.2016.
 */
@ApplicationScoped
public class LevelService {
    private Map<Integer, LevelConfig> levels = new HashMap<>();
    private List<LevelConfig> orderedLevels = new ArrayList<>();
    private Collection<LevelServiceListener> levelServiceListeners = new ArrayList<>();

    public void onGameEngineInit(@Observes GameEngineInitEvent engineInitEvent) {
        levels.clear();
        orderedLevels.clear();
        for (LevelConfig levelConfig : engineInitEvent.getGameEngineConfig().getLevelConfigs()) {
            levels.put(levelConfig.getLevelId(), levelConfig);
            orderedLevels.add(levelConfig);
        }
        Collections.sort(orderedLevels, (o1, o2) -> Integer.compare(o1.getNumber(), o2.getNumber()));
    }

    public LevelConfig getLevel(int levelId) {
        LevelConfig level = levels.get(levelId);
        if (level == null) {
            throw new IllegalArgumentException("No level for id: " + levelId);
        }
        return level;
    }

    public LevelConfig getLevel(PlayerBase playerBase) {
        return getLevel(playerBase.getUserContext());
    }

    public LevelConfig getLevel(UserContext userContext) {
        return getLevel(userContext.getLevelId());
    }

    public void increaseXp(UserContext userContext, int deltaXp) {
        int xp = userContext.getXp() + deltaXp;
        LevelConfig levelConfig = getLevel(userContext);
        if (xp >= levelConfig.getXp2LevelUp()) {
            LevelConfig newLevelConfig = getNextLevel(levelConfig);
            userContext.setLevelId(newLevelConfig.getLevelId());
            userContext.setXp(0);
            for (LevelServiceListener levelServiceListener : levelServiceListeners) {
                levelServiceListener.onLevelPassed(userContext, levelConfig, newLevelConfig);
            }
        } else {
            userContext.setXp(xp);
        }
    }

    public void addLevelServiceListener(LevelServiceListener levelServiceListener) {
        levelServiceListeners.add(levelServiceListener);
    }

    public void removeLevelServiceListener(LevelServiceListener levelServiceListener) {
        levelServiceListeners.remove(levelServiceListener);
    }

    private LevelConfig getNextLevel(LevelConfig level) {
        int newIndex = orderedLevels.indexOf(level) + 1;
        return orderedLevels.get(newIndex);
    }
}
