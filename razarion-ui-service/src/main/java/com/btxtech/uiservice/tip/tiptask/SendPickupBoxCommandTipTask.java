package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.tip.visualization.InGameItemTipVisualization;
import com.btxtech.uiservice.tip.visualization.InGameTipVisualization;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 09.12.2016.
 */
@Dependent
public class SendPickupBoxCommandTipTask extends AbstractTipTask {
    @Inject
    private BoxUiService boxUiService;
    private int boxItemTypeId;

    public void init(int boxItemTypeId) {
        this.boxItemTypeId = boxItemTypeId;
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
    public InGameTipVisualization createInGameTipVisualization() {
        return new InGameItemTipVisualization(() -> boxUiService.findFirstBoxItem(boxItemTypeId), getGameTipVisualConfig().getCornerMoveDistance(), getGameTipVisualConfig().getCornerMoveDuration(), getGameTipVisualConfig().getCornerLength(), getGameTipVisualConfig().getGrabCommandCornerColor(), getGameTipVisualConfig().getDefaultCommandShape3DId(), getGameTipVisualConfig().getOutOfViewShape3DId());
    }

    @Override
    protected void onCommandSent(CommandInfo commandInfo) {
        if (commandInfo.getType() == CommandInfo.Type.PICK_BOX) {
            int boxItemTypeId = boxUiService.getSyncBoxItem(commandInfo.getSynBoxItemId()).getItemTypeId();
            if (boxItemTypeId == this.boxItemTypeId) {
                onSucceed();
            }
        }
    }
}
