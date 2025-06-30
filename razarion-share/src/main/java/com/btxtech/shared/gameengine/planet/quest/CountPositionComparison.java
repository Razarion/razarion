/*
 * Copyright (c) 2011.
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

package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

public class CountPositionComparison extends AbstractTickComparison {
    private static final int MIN_TIME_TICK_DELAY = PlanetService.TICKS_PER_SECONDS * 5;

    private final BaseItemService baseItemService;
    private final Collection<SyncBaseItem> fulfilledItems = new HashSet<>();
    private AbstractConditionProgress abstractConditionProgress;
    private int count;
    private PlaceConfig placeConfig;
    private Integer timeSeconds;
    private String userId;
    private boolean isFulfilled;
    private boolean isItemFulfilled;
    private Integer fulfilledTickCount;

    @Inject
    public CountPositionComparison(GameLogicService gameLogicService, BaseItemService baseItemService) {
        super(gameLogicService);
        this.baseItemService = baseItemService;
    }

    public void init(int count, PlaceConfig placeConfig, Integer timeSeconds, String userId) {
        this.count = count;
        this.placeConfig = placeConfig;
        this.timeSeconds = timeSeconds;
        this.userId = userId;
    }

    @Override
    public void tick() {
        Collection<SyncBaseItem> fulfilledItems = setupFulfilled();
        if (compareWithFulfilled(fulfilledItems)) {
            if (isItemFulfilled && timeSeconds != null) {
                tickTimer();
            }
            return;
        }
        synchronized (this.fulfilledItems) {
            this.fulfilledItems.clear();
            this.fulfilledItems.addAll(fulfilledItems);
        }
        isItemFulfilled = fulfilledItems.size() >= this.count;
        if (isItemFulfilled) {
            if (timeSeconds != null) {
                tickTimer();
            } else {
                onProgressChanged();
                isFulfilled = true;
            }
        } else {
            isFulfilled = false;
            if (timeSeconds != null) {
                fulfilledTickCount = null;
            }
            onProgressChanged();
        }
    }

    private void tickTimer() {
        int timeSecondsTicks = toTicks(timeSeconds);
        if (fulfilledTickCount != null) {
            fulfilledTickCount++;
            if (fulfilledTickCount != 0 && fulfilledTickCount < timeSecondsTicks && fulfilledTickCount % MIN_TIME_TICK_DELAY == 0) {
                onProgressChanged();
            }
        } else {
            fulfilledTickCount = 0;
            onProgressChanged();
        }
        isFulfilled = fulfilledTickCount >= timeSecondsTicks;
    }

    private boolean compareWithFulfilled(Collection<SyncBaseItem> fulfilledItems) {
        synchronized (this.fulfilledItems) {
            return fulfilledItems.size() == this.fulfilledItems.size() && this.fulfilledItems.containsAll(fulfilledItems);
        }
    }

    private Collection<SyncBaseItem> setupFulfilled() {
        PlayerBaseFull playerBase = baseItemService.getPlayerBaseFull4UserId(userId);
        if (playerBase == null) {
            return Collections.emptyList();
        }

        Collection<SyncBaseItem> items;
        if (placeConfig != null) {
            items = playerBase.findItemsInPlace(placeConfig);
        } else {
            items = playerBase.getItems();
        }
        return items.stream().filter(syncBaseItem -> syncBaseItem.isAlive() && syncBaseItem.isBuildup()).collect(Collectors.toList());
    }

    @Override
    public boolean isFulfilled() {
        return isFulfilled;
    }

    @Override
    public AbstractConditionProgress getAbstractConditionProgress() {
        return abstractConditionProgress;
    }

    @Override
    public void setAbstractConditionProgress(AbstractConditionProgress abstractConditionProgress) {
        this.abstractConditionProgress = abstractConditionProgress;
    }

    @Override
    public void fillGenericComparisonValues(BackupComparisionInfo backupComparisionInfo) {
        if (fulfilledTickCount != null) {
            backupComparisionInfo.setPassedSeconds(toSeconds(fulfilledTickCount));
        }
    }

    @Override
    public void restoreFromGenericComparisonValue(BackupComparisionInfo backupComparisionInfo) {
        synchronized (fulfilledItems) {
            fulfilledItems.clear();
        }

        if (backupComparisionInfo.hasPassedSeconds()) {
            fulfilledTickCount = toTicks(backupComparisionInfo.getPassedSeconds());
        }
    }

    @Override
    public QuestProgressInfo generateQuestProgressInfo() {
        QuestProgressInfo questProgressInfo = new QuestProgressInfo();

        // Add time
        if (fulfilledTickCount != null) {
            questProgressInfo.setSecondsRemaining(timeSeconds - toSeconds(fulfilledTickCount));
        }
        questProgressInfo.setCount(fulfilledItems.size());
        return questProgressInfo;
    }

    private int toSeconds(int ticks) {
        return Math.max(0, ticks / PlanetService.TICKS_PER_SECONDS);

    }

    private int toTicks(int seconds) {
        return Math.max(0, seconds * PlanetService.TICKS_PER_SECONDS);
    }
}