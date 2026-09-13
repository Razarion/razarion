package com.btxtech.shared.gameengine.planet.quest;


import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.planet.GameLogicService;

/**
 * User: Razarion contributors Date: 12.01.2011 Time: 12:05:40
 */
public abstract class AbstractInventoryItemComparison extends AbstractUpdatingComparison {
    private AbstractConditionProgress abstractConditionTrigger;

    public AbstractInventoryItemComparison(GameLogicService gameLogicService) {
        super(gameLogicService);
    }

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
