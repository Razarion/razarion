package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConflictConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneIndicationInfo;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Beat
 * on 19.03.2018.
 */
@Dependent
public class BotScene {
    @Inject
    private Instance<BotSceneConflict> conflictInstance;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private GameLogicService gameLogicService;
    private BotSceneConfig botSceneConfig;
    private Set<Integer> botsToWatch;
    private final HashMap<Integer, ProvocationScale> provocationScales = new HashMap<>();
    private SimpleScheduledFuture future;

    public void start(BotSceneConfig botSceneConfig) {
        botsToWatch = new HashSet<>(botSceneConfig.getBotIdsToWatch());
        this.botSceneConfig = botSceneConfig;
        future = simpleExecutorService.scheduleAtFixedRate(botSceneConfig.getScheduleTimeMillis(), true, this::onTimer, SimpleExecutorService.Type.BOT_SCENE_TICKER);
    }

    public void onKillBotItem(int botId, PlayerBase actor) {
        if (botsToWatch.contains(botId)) {
            synchronized (provocationScales) {
                ProvocationScale provocationScale = provocationScales.computeIfAbsent(actor.getUserId(), humanPlayerId -> new ProvocationScale(actor.getUserId(), findFirstBotSceneConflict()));
                provocationScale.onKill();
            }
        }
    }

    public void stop() {
        try {
            future.cancel();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public boolean onHumanKill(int userId, BotRunner botRunner) {
        ProvocationScale provocationScale = provocationScales.get(userId);
        return provocationScale != null
                && provocationScale.getCurrentBotSceneConflict() != null
                && provocationScale.getCurrentBotSceneConflict().onHumanKill(botRunner);
    }

    public BotSceneIndicationInfo getBotSceneIndicationInfo(int userId) {
        ProvocationScale provocationScale = provocationScales.get(userId);
        if (provocationScale == null) {
            return null;
        }
        BotSceneConflictConfig botSceneConflictConfig = provocationScale.getCurrentBotSceneConflictConfig();
        if (botSceneConflictConfig == null) {
            return null;
        }
        int index = findBotSceneConflictConfigIndex(botSceneConflictConfig);
        return new BotSceneIndicationInfo().setBotSceneId(botSceneConfig.getId()).setConflictStep(index + 1).setConflictStepCount(botSceneConfig.getBotSceneConflictConfigs().size());
    }


    private void onTimer() {
        Collection<Integer> expiredHumanPlayerIdConflicts = new ArrayList<>();
        synchronized (provocationScales) {
            provocationScales.values().forEach(provocationScale -> {
                try {
                    if (provocationScale.isRiseReached()) {
                        BotSceneConflictConfig oldNextBotSceneConflictConfig = provocationScale.getNextBotSceneConflictConfig();
                        if (oldNextBotSceneConflictConfig == null) {
                            return;
                        }
                        BotSceneConflictConfig newNextBotSceneConflictConfig = findNextBotSceneConflict(oldNextBotSceneConflictConfig);
                        BotSceneConflictConfig oldBotSceneConflictConfig = provocationScale.getCurrentBotSceneConflictConfig();
                        provocationScale.onRise(newNextBotSceneConflictConfig);

                        BotSceneConflict currentBotSceneConflict = provocationScale.getCurrentBotSceneConflict();
                        if (currentBotSceneConflict == null) {
                            currentBotSceneConflict = conflictInstance.get();
                            currentBotSceneConflict.setUserId(provocationScale.getUserId());
                            provocationScale.setCurrentBotSceneConflict(currentBotSceneConflict);
                        }
                        currentBotSceneConflict.start(oldNextBotSceneConflictConfig);
                        gameLogicService.onBotSceneConflictChanged(provocationScale.getUserId(), true, oldNextBotSceneConflictConfig, oldBotSceneConflictConfig, getBotSceneIndicationInfo(provocationScale.getUserId()));
                    } else if (provocationScale.isFallReached()) {
                        BotSceneConflictConfig oldBotSceneConflictConfig = provocationScale.getCurrentBotSceneConflictConfig();
                        BotSceneConflictConfig newCurrentBotSceneConflictConfig = findPreviousBotSceneConflict(oldBotSceneConflictConfig);
                        provocationScale.onFall(newCurrentBotSceneConflictConfig);
                        if (newCurrentBotSceneConflictConfig != null) {
                            provocationScale.getCurrentBotSceneConflict().start(newCurrentBotSceneConflictConfig);
                        } else {
                            provocationScale.getCurrentBotSceneConflict().stop();
                            provocationScale.setCurrentBotSceneConflict(null);
                        }
                        gameLogicService.onBotSceneConflictChanged(provocationScale.getUserId(), false, newCurrentBotSceneConflictConfig, oldBotSceneConflictConfig, getBotSceneIndicationInfo(provocationScale.getUserId()));
                    } else if (provocationScale.isCleanupReached()) {
                        expiredHumanPlayerIdConflicts.add(provocationScale.getUserId());
                    } else if (provocationScale.getCurrentBotSceneConflict() != null) {
                        provocationScale.getCurrentBotSceneConflict().tick();
                    }
                } catch (Throwable t) {
                    exceptionHandler.handleException(t);
                }
            });
            expiredHumanPlayerIdConflicts.forEach(provocationScales::remove);
        }
    }

    private BotSceneConflictConfig findFirstBotSceneConflict() {
        if (botSceneConfig.getBotSceneConflictConfigs() == null || botSceneConfig.getBotSceneConflictConfigs().isEmpty()) {
            throw new IllegalStateException("No BotSceneConflictConfig configured for: " + botSceneConfig);
        }
        return botSceneConfig.getBotSceneConflictConfigs().get(0);
    }

    private BotSceneConflictConfig findNextBotSceneConflict(BotSceneConflictConfig botSceneConflictConfig) {
        int index = findBotSceneConflictConfigIndex(botSceneConflictConfig);
        index++;
        if (index < botSceneConfig.getBotSceneConflictConfigs().size()) {
            return botSceneConfig.getBotSceneConflictConfigs().get(index);
        } else {
            return null;
        }
    }

    private BotSceneConflictConfig findPreviousBotSceneConflict(BotSceneConflictConfig botSceneConflictConfig) {
        int index = findBotSceneConflictConfigIndex(botSceneConflictConfig);
        index--;
        if (index >= 0) {
            return botSceneConfig.getBotSceneConflictConfigs().get(index);
        } else {
            return null;
        }
    }

    private int findBotSceneConflictConfigIndex(BotSceneConflictConfig botSceneConflictConfig) {
        int index = botSceneConfig.getBotSceneConflictConfigs().indexOf(botSceneConflictConfig);
        if (index < 0) {
            throw new IllegalArgumentException("index = " + index + " for: " + botSceneConflictConfig);
        }
        return index;
    }

    public Collection<Integer> allActiveConflicts() {
        synchronized (provocationScales) {
            return new ArrayList<>(provocationScales.keySet());
        }
    }
}
