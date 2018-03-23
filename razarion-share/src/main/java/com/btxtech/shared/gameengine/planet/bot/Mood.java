package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConfig;

/**
 * Created by Beat
 * on 20.03.2018.
 */
public class Mood {
    private int kills;
    private HumanPlayerId humanPlayerId;

    public Mood(HumanPlayerId humanPlayerId) {
        this.humanPlayerId = humanPlayerId;
    }

    public HumanPlayerId getHumanPlayerId() {
        return humanPlayerId;
    }

    public int getKills() {
        return kills;
    }

    public void increaseKills() {
        kills++;
    }

    public boolean checkThreshold(BotSceneConfig botSceneConfig) {
        return kills >= botSceneConfig.getKillThreshold();
    }
}
