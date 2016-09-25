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

package com.btxtech.shared.gameengine.planet.condition;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * User: beat
 * Date: 28.12.2010
 * Time: 13:09:19
 */
@ApplicationScoped
public class ConditionService {
    @Inject
    private ItemTypeService itemTypeService;
    private final Map<UserContext, AbstractConditionProgress> progressMap = new HashMap<>();

    public void activateCondition(UserContext examinee, ConditionConfig conditionConfig, Consumer<UserContext> conditionPassedListener) {
        AbstractComparison abstractComparison = null;
        if (conditionConfig.getConditionTrigger().isComparisonNeeded()) {
            abstractComparison = createAbstractComparison(conditionConfig.getComparisonConfig());
//            if (abstractComparison instanceof AbstractUpdatingComparison) {
//                ((AbstractUpdatingComparison) abstractComparison).setGlobalServices(getGlobalServices());
//            }
//   TODO         if (abstractComparison instanceof TimeAware && ((TimeAware) abstractComparison).isTimerNeeded()) {
//   TODO             timeAwareList.add((TimeAware) abstractComparison);
//   TODO             if (timeAwareList.size() == 1) {
//   TODO                 startTimer();
//   TODO             }
//   TODO         }
        }
        AbstractConditionProgress conditionProgress = conditionConfig.getConditionTrigger().createConditionProgress(abstractComparison);
        conditionProgress.setExaminee(examinee);
        conditionProgress.setConditionPassedListener(conditionPassedListener);
        synchronized (progressMap) {
            progressMap.put(examinee, conditionProgress);
        }
        if (conditionProgress.isFulfilled()) {
            conditionPassed(conditionProgress);
        }
    }

    public void deactivateActorCondition(UserContext examinee) {
        synchronized (progressMap) {
            progressMap.remove(examinee);
        }
        // TODO if (abstractConditionTrigger != null) {
        // TODO handleTimerRemoval(abstractConditionTrigger);
        // TODO }
    }

    public void onSyncItemBuilt(SyncBaseItem syncBaseItem) {
        UserContext examinee = syncBaseItem.getBase().getUserContext();
        if (examinee == null) {
            return;
        }
        triggerSyncItem(examinee, ConditionTrigger.SYNC_ITEM_CREATED, syncBaseItem);
        triggerSyncItem(examinee, ConditionTrigger.SYNC_ITEM_POSITION, syncBaseItem);
    }

    private AbstractComparison createAbstractComparison(ComparisonConfig comparisonConfig) {
        if (comparisonConfig.getCount() != null) {
            return new CountComparison(comparisonConfig.getCount());
        } else if (comparisonConfig.getBaseItemTypeCount() != null) {
            Map<BaseItemType, Integer> baseItemType = new HashMap<>();
            for (Map.Entry<Integer, Integer> entry : comparisonConfig.getBaseItemTypeCount().entrySet()) {
                baseItemType.put(itemTypeService.getBaseItemType(entry.getKey()), entry.getValue());
            }
            return new SyncItemTypeComparison(baseItemType);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void triggerSyncItem(UserContext examinee, ConditionTrigger conditionTrigger, SyncBaseItem syncBaseItem) {
        AbstractConditionProgress abstractConditionProgress;
        synchronized (progressMap) {
            abstractConditionProgress = progressMap.get(examinee);
        }
        if (abstractConditionProgress == null) {
            return;
        }
        SyncBaseItemConditionProgress syncBaseItemConditionProgress = (SyncBaseItemConditionProgress) abstractConditionProgress;
        syncBaseItemConditionProgress.onItem(syncBaseItem);
        if (syncBaseItemConditionProgress.isFulfilled()) {
            conditionPassed(syncBaseItemConditionProgress);
        }
    }

    private void conditionPassed(AbstractConditionProgress abstractConditionProgress) {
        deactivateActorCondition(abstractConditionProgress.getExaminee());
        if (abstractConditionProgress.getConditionPassedListener() != null) {
            abstractConditionProgress.getConditionPassedListener().accept(abstractConditionProgress.getExaminee());
        }
    }
}
