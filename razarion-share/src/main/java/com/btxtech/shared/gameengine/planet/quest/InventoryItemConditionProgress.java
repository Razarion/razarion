package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;

/**
 * User: Razarion contributors
 * Date: 27.12.2010
 * Time: 18:58:14
 */
public class InventoryItemConditionProgress extends AbstractConditionProgress {

    public InventoryItemConditionProgress(ConditionTrigger conditionTrigger, AbstractComparison abstractComparison) {
        super(conditionTrigger, abstractComparison);
    }

    public void onInventoryItem(InventoryItem inventoryItem) {
        ((AbstractInventoryItemComparison) getAbstractComparison()).onInventoryItem(inventoryItem);
        if (getAbstractComparison().isFulfilled()) {
            setFulfilled();
        }
    }
}
