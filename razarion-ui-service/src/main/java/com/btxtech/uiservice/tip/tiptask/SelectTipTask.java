package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.datatypes.Group;
import com.btxtech.uiservice.tip.visualization.InGameItemTipVisualization;
import com.btxtech.uiservice.tip.visualization.InGameTipVisualization;

import javax.enterprise.context.Dependent;

/**
 * User: beat
 * Date: 22.08.12
 * Time: 12:53
 */
@Dependent
public class SelectTipTask extends AbstractTipTask {
    private int itemTypeId;

    public void init(int itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    @Override
    public void internalStart() {
        activateFailOnSelectionCleared();
        activateFailOnTargetSelectionChanged();
        // TODO may needed activateConversionOnMouseMove();
        setShowInGameQuestVisualisation(false);
    }

    @Override
    public boolean isFulfilled() {
        return isSingleSelection(itemTypeId);
    }

    @Override
    public void internalCleanup() {
        setShowInGameQuestVisualisation(true);
    }

    @Override
    public void onOwnSelectionChanged(Group selectedGroup) {
        if (isSingleSelection(itemTypeId, selectedGroup)) {
            onSucceed();
        } else {
            onFailed();
        }
    }

    public InGameTipVisualization createInGameTip() {
        return new InGameItemTipVisualization(getPlayerBase().findSyncBaseItemOfType(itemTypeId), getGameTipVisualConfig().getCornerMoveDistance(), getGameTipVisualConfig().getCornerMoveDuration(), getGameTipVisualConfig().getCornerLength(), getGameTipVisualConfig().getSelectCornerColor(), getGameTipVisualConfig().getSelectShape3DId());
    }
}
