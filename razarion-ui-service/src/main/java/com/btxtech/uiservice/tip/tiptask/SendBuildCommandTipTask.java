package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.itemplacer.BaseItemPlacer;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerListener;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.uiservice.tip.visualization.InGameItemTipVisualization;
import com.btxtech.uiservice.tip.visualization.InGamePositionTipVisualization;
import com.btxtech.uiservice.tip.visualization.InGameTipVisualization;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * User: beat
 * Date: 22.08.12
 * Time: 12:53
 */
@Dependent
public class SendBuildCommandTipTask extends AbstractTipTask implements BaseItemPlacerListener {
    @Inject
    private BaseItemPlacerService baseItemPlacerService;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private BaseItemUiService baseItemUiService;
    private int toBeBuildId;
    private SyncBaseItemSimpleDto toBeFinalized;
    private DecimalPosition positionHint;

    public void init(int toBeBuildId, DecimalPosition positionHint) {
        this.toBeBuildId = toBeBuildId;
        this.positionHint = positionHint;
        activateFailOnSelectionCleared();
    }

    @Override
    public void internalStart() {
        for (SyncBaseItemSimpleDto existingItem : findItemsOfType(toBeBuildId)) {
            if (!existingItem.checkBuildup()) {
                toBeFinalized = existingItem;
                break;
            }
        }
        if (toBeFinalized == null) {
            baseItemPlacerService.addListener(this);
        }
    }

    @Override
    public boolean isFulfilled() {
        return false;
    }

    @Override
    public void internalCleanup() {
        if (toBeFinalized == null) {
            baseItemPlacerService.removeListener(this);
        }
    }

    @Override
    protected void onCommandSent(CommandInfo commandInfo) {
        if (toBeFinalized != null) {
            if (commandInfo.getType() == CommandInfo.Type.FINALIZE_BUILD && commandInfo.getToBeFinalizedId() == toBeFinalized.getId()) {
                onSucceed();
            }
        } else {
            if (commandInfo.getType() == CommandInfo.Type.BUILD && commandInfo.getToBeBuiltId() == toBeBuildId) {
                onSucceed();
            }
        }
    }

    @Override
    public InGameTipVisualization createInGameTipVisualization() {
        if (toBeFinalized != null) {
            return new InGameItemTipVisualization(() -> baseItemUiService.monitorSyncItem(toBeFinalized), getGameTipVisualConfig().getCornerMoveDistance(), getGameTipVisualConfig().getCornerMoveDuration(), getGameTipVisualConfig().getCornerLength(), getGameTipVisualConfig().getToBeFinalizedCornerColor(), getGameTipVisualConfig().getDefaultCommandShape3DId(), getGameTipVisualConfig().getOutOfViewShape3DId());
        } else {
            InGamePositionTipVisualization visualization = new InGamePositionTipVisualization(getGameTipVisualConfig().getCornerMoveDistance(), getGameTipVisualConfig().getCornerMoveDuration(), getGameTipVisualConfig().getCornerLength(), getGameTipVisualConfig().getBaseItemPlacerCornerColor(), getGameTipVisualConfig().getBaseItemPlacerShape3DId(), getGameTipVisualConfig().getOutOfViewShape3DId());
            terrainUiService.getTerrainPosition(positionHint, visualization::setPosition);
            return visualization;
        }
    }

    @Override
    public void activatePlacer(BaseItemPlacer baseItemPlacer) {
        // Ignore
    }

    @Override
    public void deactivatePlacer(boolean canceled) {
        if (canceled) {
            onFailed();
        }
    }
}
