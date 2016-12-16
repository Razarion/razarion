package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.itemplacer.BaseItemPlacer;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerListener;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
import com.btxtech.uiservice.tip.visualization.AbstractGuiTipVisualization;
import com.btxtech.uiservice.tip.visualization.GuiPointingTipVisualization;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 22.08.12
 * Time: 13:19
 */
@Dependent
public class ToBeBuildPlacerTipTask extends AbstractTipTask implements BaseItemPlacerListener {
    private Logger logger = Logger.getLogger(ToBeBuildPlacerTipTask.class.getName());
    @Inject
    private ItemCockpitService itemCockpitService;
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
    public void onStateChanged(BaseItemPlacer baseItemPlacer) {
        if (baseItemPlacer.getBaseItemType().getId() == itemTypeToBePlaced) {
            onSucceed();
        }
    }

    @Override
    public AbstractGuiTipVisualization createGuiTipVisualization() {
        if (getGameTipVisualConfig().getSouthLeftMouseGuiImageId() != null) {
            return new GuiPointingTipVisualization(this::providePosition, GuiPointingTipVisualization.Direction.SOUTH, getGameTipVisualConfig().getSouthLeftMouseGuiImageId());
        } else {
            logger.warning("No image defined for GameTipVisualConfig.downArrowLeftMouseGuiImageId");
            return null;
        }
    }

    private Index providePosition() {
        Rectangle rectangle = itemCockpitService.getBuildButtonLocation(itemTypeToBePlaced);
        return new Index((rectangle.startX() + rectangle.endX()) / 2, rectangle.startY());
    }
}
