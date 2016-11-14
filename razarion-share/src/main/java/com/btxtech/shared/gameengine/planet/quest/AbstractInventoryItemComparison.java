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


import com.btxtech.shared.gameengine.datatypes.InventoryItem;

/**
 * User: beat Date: 12.01.2011 Time: 12:05:40
 */
public abstract class AbstractInventoryItemComparison extends AbstractUpdatingComparison {
    private AbstractConditionProgress abstractConditionTrigger;

    protected abstract void privateOnInventoryItem(InventoryItem inventoryItem);

    public final void onInventoryItem(InventoryItem inventoryItem) {
        privateOnInventoryItem(inventoryItem);
    }

    @Override
    public AbstractConditionProgress getAbstractConditionProgress() {
        return abstractConditionTrigger;
    }

    @Override
    public void setAbstractConditionProgress(AbstractConditionProgress abstractConditionProgress) {
        this.abstractConditionTrigger = abstractConditionProgress;
    }
}
