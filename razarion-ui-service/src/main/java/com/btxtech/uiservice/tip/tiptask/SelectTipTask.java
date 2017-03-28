package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.uiservice.Group;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.nativejs.NativeMatrixFactory;
import com.btxtech.uiservice.tip.visualization.InGameItemTipVisualization;
import com.btxtech.uiservice.tip.visualization.InGameTipVisualization;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * User: beat
 * Date: 22.08.12
 * Time: 12:53
 */
@Dependent
public class SelectTipTask extends AbstractTipTask {
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;
    private int itemTypeId;

    public void init(int itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    @Override
    public void internalStart() {
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
        }
    }

    @Override
    public InGameTipVisualization createInGameTipVisualization() {
        return new InGameItemTipVisualization(() -> baseItemUiService.monitorMySyncBaseItemOfType(itemTypeId), getGameTipVisualConfig().getCornerMoveDistance(), getGameTipVisualConfig().getCornerMoveDuration(), getGameTipVisualConfig().getCornerLength(), getGameTipVisualConfig().getSelectCornerColor(), getGameTipVisualConfig().getSelectShape3DId(), getGameTipVisualConfig().getOutOfViewShape3DId(), nativeMatrixFactory);
    }
}
