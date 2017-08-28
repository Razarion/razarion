/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 21.05.2010
 * Time: 21:51:58
 */
@Dependent
public class BotRunner {
    private enum IntervalState {
        INACTIVE,
        ACTIVE
    }

    private Logger log = Logger.getLogger(BotRunner.class.getName());
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private Instance<BotEnragementState> enragementStateInstance;
    @Inject
    private Instance<IntruderHandler> intruderHandlerInstance;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    private BotConfig botConfig;
    private PlayerBaseFull base;
    private BotEnragementState botEnragementState;
    private IntruderHandler intruderHandler;
    private final Object syncObject = new Object();
    private IntervalState intervalState;
    private SimpleScheduledFuture botTimerFuture;
    private SimpleScheduledFuture botTickerFuture;

    private class BotTicker implements Runnable {
        @Override
        public void run() {
            // System.out.println("--------BotTicker.run()--------");
            try {
                synchronized (syncObject) {
                    if (botEnragementState == null) {
                        return;
                    }
                    createBaseIfNeeded();
                    botEnragementState.work(base);
                    if (intruderHandler != null) {
                        intruderHandler.handleIntruders(base);
                    }
                }
            } catch (Throwable t) {
                log.log(Level.SEVERE, "Exception in BotRunner (BotTicker): " + botConfig.getName(), t);
            }
        }
    }

    private class BotTimer implements Runnable {
        @Override
        public void run() {
            try {
                runBotTimer();
            } catch (Throwable t) {
                log.log(Level.SEVERE, "", t);
            }
        }
    }

    public void start(BotConfig botConfig) {
        this.botConfig = botConfig;
        if (botConfig.intervalBot()) {
            if (botConfig.intervalValid()) {
                intervalState = IntervalState.INACTIVE;
                scheduleTimer(botConfig.getMinInactiveMs(), botConfig.getMaxInactiveMs());
            } else {
                log.warning("Bot has invalid interval configuration: " + botConfig.getName());
            }
        } else {
            startBot();
        }
    }

    BotConfig getBotConfig() {
        return botConfig;
    }

    private void killBotThread() {
        if (botTickerFuture != null) {
            botTickerFuture.cancel();
            botTickerFuture = null;
        }
    }

    private void createBaseIfNeeded() {
        if (base == null || !baseItemService.isAlive(base)) {
            base = baseItemService.createBotBase(botConfig);
        }
    }

    private BotEnragementState.Listener getEnragementStateListener() {
        return null;
    }

    void kill() {
        killTimer();
        killBot();
    }

    private void killTimer() {
        if (botTimerFuture != null) {
            botTimerFuture.cancel();
            botTimerFuture = null;
        }
    }


    public PlayerBaseFull getBase() {
        return base;
    }

    boolean isInRealm(DecimalPosition position) {
        return botConfig.getRealm().checkInside(position);
    }

    void enrageOnKill(SyncBaseItem syncBaseItem, PlayerBase actor) {
        if (botEnragementState != null) {
            // Timer bot is may inactive
            botEnragementState.enrageOnKill(syncBaseItem, actor);
        }
    }

    public boolean onSyncBaseItemCreated(SyncBaseItem syncBaseItem, SyncBaseItem createdBy) {
        if (botEnragementState != null) {
            if (base != null && base.equals(syncBaseItem.getBase())) {
                botEnragementState.onSyncBaseItemCreated(syncBaseItem, createdBy);
                return true;
            }
        }
        return false;
    }

    public void executeCommand(AbstractBotCommandConfig botCommandConfig) {
        botEnragementState.executeCommand(botCommandConfig, base);
    }


    private void killBot() {
        synchronized (syncObject) {
            killBotThread();
            if (botEnragementState != null) {
                botEnragementState.killAllItems(base);
            }
            botEnragementState = null;
            intruderHandler = null;
        }
    }

    private void startBot() {
        synchronized (syncObject) {
            botEnragementState = enragementStateInstance.get();
            botEnragementState.init(botConfig.getBotEnragementStateConfigs(), botConfig.getRealm(), botConfig.getName(), getEnragementStateListener());
            if (botConfig.isAutoAttack()) {
                intruderHandler = intruderHandlerInstance.get();
                intruderHandler.init(botEnragementState, botConfig.getRealm());
            }
        }
        killBotThread();
        BotTicker botTicker = new BotTicker();
        botTicker.run();
        botTickerFuture = simpleExecutorService.scheduleAtFixedRate(botConfig.getActionDelay(), true, botTicker, SimpleExecutorService.Type.BOT);
    }

    private void runBotTimer() {
        try {
            switch (intervalState) {
                case INACTIVE:
                    startBot();
                    intervalState = IntervalState.ACTIVE;
                    scheduleTimer(botConfig.getMinActiveMs(), botConfig.getMaxActiveMs());
                    break;
                case ACTIVE:
                    killBot();
                    intervalState = IntervalState.INACTIVE;
                    scheduleTimer(botConfig.getMinInactiveMs(), botConfig.getMaxInactiveMs());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown intervalState: " + intervalState);
            }
        } catch (Throwable t) {
            log.log(Level.SEVERE, "Exception in BotRunner (runBotTimer): " + botConfig.getName(), t);
        }
    }

    private void scheduleTimer(long min, long max) {
        Random random = new Random();
        long delay = min + (long) (random.nextDouble() * (double) (max - min));
        killTimer();
        botTimerFuture = simpleExecutorService.schedule(delay, new BotTimer(), SimpleExecutorService.Type.UNSPECIFIED);
    }
}
