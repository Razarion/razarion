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


/**
 * User: beat
 * Date: 27.12.2010
 * Time: 17:25:49
 */
public interface AbstractComparison {
    boolean isFulfilled();

    AbstractConditionProgress getAbstractConditionProgress();

    void setAbstractConditionProgress(AbstractConditionProgress abstractConditionProgress);

    // TODO dave & reload void fillGenericComparisonValues(GenericComparisonValueContainer genericComparisonValueContainer);

    // TODO dave & reload void restoreFromGenericComparisonValue(GenericComparisonValueContainer genericComparisonValueContainer);

    // TODO visualization void fillQuestProgressInfo(QuestProgressInfo questProgressInfo, ConditionService aconditionService);

    void handleDeferredUpdate();
}
