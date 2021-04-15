package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
import com.btxtech.uiservice.tip.visualization.AbstractGuiTipVisualization;
import com.btxtech.uiservice.tip.visualization.GuiPointingTipVisualization;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 13.12.2016.
 */
@Dependent
public class UseInventoryItemTipTask extends AbstractTipTask {
    private Logger logger = Logger.getLogger(UseInventoryItemTipTask.class.getName());
    @Inject
    private BaseItemPlacerService baseItemPlacerService;
    @Inject
    private MainCockpitService cockpitService;
    private int inventoryItemId;

    public void init(int inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }

    @Override
    protected void internalStart() {
    }

    @Override
    protected void internalCleanup() {
    }

    @Override
    public boolean isFulfilled() {
        return false;
    }

    @Override
    protected void onInventoryDialogClosed() {
        onFailed();
    }

    @Override
    protected void onInventoryItemPlacerActivated(InventoryItem inventoryItem) {
        if (inventoryItem.getId() == inventoryItemId) {
            onSucceed();
        }
    }

    @Override
    public AbstractGuiTipVisualization createGuiTipVisualization() {
        if (getGameTipVisualConfig().getWestLeftMouseGuiImageId() != null) {
            return new GuiPointingTipVisualization(this::providePosition, GuiPointingTipVisualization.Direction.WEST, getGameTipVisualConfig().getWestLeftMouseGuiImageId());
        } else {
            logger.warning("No image defined for GameTipVisualConfig.leftArrowLeftMouseGuiImageId");
            return null;
        }
    }

    private Index providePosition() {
        Rectangle rectangle = cockpitService.getInventoryUseButtonLocation(inventoryItemId);
        return new Index(rectangle.endX(), (rectangle.endY() + rectangle.startY()) / 2);
    }

}
