package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.tip.visualization.InGameItemTipVisualization;
import com.btxtech.uiservice.tip.visualization.InGameTipVisualization;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collection;

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
        SyncBoxItemSimpleDto syncBoxItem = boxUiService.findFirstBoxItem(boxItemTypeId);
        if (syncBoxItem == null) {
            throw new IllegalArgumentException("Can not create game tip. No box available to mark: " + boxItemTypeId);
        }
        return new InGameItemTipVisualization(syncBoxItem, getGameTipVisualConfig().getCornerMoveDistance(), getGameTipVisualConfig().getCornerMoveDuration(), getGameTipVisualConfig().getCornerLength(), getGameTipVisualConfig().getGrabCommandCornerColor(), getGameTipVisualConfig().getDefaultCommandShape3DId(), getGameTipVisualConfig().getOutOfViewShape3DId());
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
