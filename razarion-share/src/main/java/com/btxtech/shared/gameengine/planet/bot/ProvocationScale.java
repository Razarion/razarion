package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConflictConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 20.03.2018.
 */
public class ProvocationScale {
    private BotSceneConflictConfig nextBotSceneConflictConfig;
    private BotSceneConflictConfig currentBotSceneConflictConfig;
    private BotSceneConflict currentBotSceneConflict;
    private final List<Long> killTimeStamps = new ArrayList<>();
    private int userId;
    private Long lastKillTimeStamp;

    public ProvocationScale(int userId, BotSceneConflictConfig nextBotSceneConflictConfig) {
        this.userId = userId;
        this.nextBotSceneConflictConfig = nextBotSceneConflictConfig;
        reset();
    }

    public void onRise(BotSceneConflictConfig nextBotSceneConflictConfig) {
        currentBotSceneConflictConfig = this.nextBotSceneConflictConfig;
        this.nextBotSceneConflictConfig = nextBotSceneConflictConfig;
        reset();
    }

    public void onFall(BotSceneConflictConfig currentBotSceneConflictConfig) {
        nextBotSceneConflictConfig = this.currentBotSceneConflictConfig;
        this.currentBotSceneConflictConfig = currentBotSceneConflictConfig;
        reset();
    }

    private void reset() {
        lastKillTimeStamp = System.currentTimeMillis();
        synchronized (killTimeStamps) {
            killTimeStamps.clear();
        }
    }

    public int getUserId() {
        return userId;
    }

    public void onKill() {
        if (nextBotSceneConflictConfig != null) {
            synchronized (killTimeStamps) {
                killTimeStamps.add(System.currentTimeMillis());
            }
        }
        if (currentBotSceneConflictConfig != null) {
            lastKillTimeStamp = System.currentTimeMillis();
        }

    }

    public boolean isRiseReached() {
        if (nextBotSceneConflictConfig == null) {
            return false;
        }
        long minTimeStamp = System.currentTimeMillis() - (long) nextBotSceneConflictConfig.getEnterDuration();
        synchronized (killTimeStamps) {
            killTimeStamps.removeIf(timeStamp -> timeStamp < minTimeStamp);
        }
        return killTimeStamps.size() >= nextBotSceneConflictConfig.getEnterKills();
    }

    public boolean isFallReached() {
        return currentBotSceneConflictConfig != null && System.currentTimeMillis() > lastKillTimeStamp + (long) currentBotSceneConflictConfig.getLeaveNoKillDuration();
    }

    public boolean isCleanupReached() {
        return currentBotSceneConflictConfig == null && killTimeStamps.isEmpty();
    }

    public BotSceneConflictConfig getCurrentBotSceneConflictConfig() {
        return currentBotSceneConflictConfig;
    }

    public BotSceneConflictConfig getNextBotSceneConflictConfig() {
        return nextBotSceneConflictConfig;
    }

    public BotSceneConflict getCurrentBotSceneConflict() {
        return currentBotSceneConflict;
    }

    public void setCurrentBotSceneConflict(BotSceneConflict currentBotSceneConflict) {
        this.currentBotSceneConflict = currentBotSceneConflict;
    }

}
