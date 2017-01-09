package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.tip.visualization.AbstractGuiTipVisualization;
import com.btxtech.uiservice.tip.visualization.GuiPointingTipVisualization;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 22.08.12
 * Time: 13:19
 */
public class SendFactorizeCommandTipTask extends AbstractTipTask {
    private Logger logger = Logger.getLogger(SendFactorizeCommandTipTask.class.getName());
    @Inject
    private ItemCockpitService itemCockpitService;
    private int itemTypeToFactorized;

    public void init(int itemTypeToFactorized) {
        this.itemTypeToFactorized = itemTypeToFactorized;
        activateFailOnSelectionCleared();
    }

    @Override
    public void internalStart() {
    }

    @Override
    public boolean isFulfilled() {
        return false;
    }

    @Override
    public void internalCleanup() {
    }

    @Override
    protected void onCommandSent(CommandInfo commandInfo) {
        if (commandInfo.getType() == CommandInfo.Type.FABRICATE && commandInfo.getToBeBuiltId() == itemTypeToFactorized) {
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
        Rectangle rectangle = itemCockpitService.getBuildButtonLocation(itemTypeToFactorized);
        return new Index((rectangle.startX() + rectangle.endX()) / 2, rectangle.startY());
    }
}
