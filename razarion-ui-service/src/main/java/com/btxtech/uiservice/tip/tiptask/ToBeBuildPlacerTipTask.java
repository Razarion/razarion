package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.itemplacer.BaseItemPlacer;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerListener;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
import com.btxtech.uiservice.tip.visualization.InGameTipVisualization;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collection;

/**
 * User: beat
 * Date: 22.08.12
 * Time: 13:19
 */
@Dependent
public class ToBeBuildPlacerTipTask extends AbstractTipTask implements BaseItemPlacerListener {
    @Inject
    private SelectionHandler selectionHandler;
    @Inject
    private BaseItemPlacerService baseItemPlacerService;
    private int itemTypeToBePlaced;

    public void init(int itemTypeToBePlaced) {
        this.itemTypeToBePlaced = itemTypeToBePlaced;
        activateFailOnSelectionCleared();
    }

    @Override
    public void internalStart() {
        baseItemPlacerService.addListener(this);
    }

    @Override
    public boolean isFulfilled() {
        Collection<SyncBaseItem> existingItems = findItemsOfType(itemTypeToBePlaced);
        for (SyncBaseItem existingItem : existingItems) {
            if (!existingItem.isBuildup() && selectionHandler.atLeastOneItemTypeAllowed2FinalizeBuild(existingItem)) {
                return true;
            }
        }
        BaseItemPlacer baseItemPlacer = baseItemPlacerService.getBaseItemPlacer();
        return baseItemPlacer != null && baseItemPlacer.getBaseItemType().getId() == itemTypeToBePlaced;
    }

    @Override
    public void internalCleanup() {
        baseItemPlacerService.removeListener(this);
    }

    @Override
    public InGameTipVisualization createInGameTipVisualization() {
        return null;
    }

    @Override
    public void onStateChanged(BaseItemPlacer baseItemPlacer) {
        if (baseItemPlacer.getBaseItemType().getId() == itemTypeToBePlaced) {
            onSucceed();
        }
    }
}
