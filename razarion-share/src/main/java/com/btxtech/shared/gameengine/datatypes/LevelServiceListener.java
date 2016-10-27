package com.btxtech.shared.gameengine.datatypes;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;

/**
 * Created by Beat
 * 26.10.2016.
 */
public interface LevelServiceListener {
    void onLevelPassed(UserContext userContext, LevelConfig oldLvele, LevelConfig newLevel);
}
