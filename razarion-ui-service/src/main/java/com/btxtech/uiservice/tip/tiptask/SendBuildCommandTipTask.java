package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.command.BuilderCommand;
import com.btxtech.shared.gameengine.datatypes.command.BuilderFinalizeCommand;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.uiservice.itemplacer.BaseItemPlacer;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerListener;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
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
    private TerrainService terrainService;
    private int toBeBuildId;
    private SyncBaseItem toBeFinalized;
    private DecimalPosition positionHint;

    public void init(int toBeBuildId, DecimalPosition positionHint) {
        this.toBeBuildId = toBeBuildId;
        this.positionHint = positionHint;
        activateFailOnSelectionCleared();
    }

    @Override
    public void internalStart() {
        for (SyncBaseItem existingItem : findItemsOfType(toBeBuildId)) {
            if (!existingItem.isBuildup()) {
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
    protected void onCommandSent(BaseCommand baseCommand) {
        if (toBeFinalized != null) {
            if (baseCommand instanceof BuilderFinalizeCommand && ((BuilderFinalizeCommand) baseCommand).getBuildingId() == toBeFinalized.getId()) {
                onSucceed();
            }
        } else {
            if (baseCommand instanceof BuilderCommand && ((BuilderCommand) baseCommand).getToBeBuiltId() == toBeBuildId) {
                onSucceed();
            }
        }
    }

    @Override
    public InGameTipVisualization createInGameTip() {
        if (toBeFinalized != null) {
            return new InGameItemTipVisualization(toBeFinalized, getGameTipVisualConfig().getCornerMoveDistance(), getGameTipVisualConfig().getCornerMoveDuration(), getGameTipVisualConfig().getCornerLength(), getGameTipVisualConfig().getToBeFinalizedCornerColor(), getGameTipVisualConfig().getDefaultCommandShape3DId());
        } else {
            return new InGamePositionTipVisualization(terrainService.getPosition3d(positionHint), getGameTipVisualConfig().getCornerMoveDistance(), getGameTipVisualConfig().getCornerMoveDuration(), getGameTipVisualConfig().getCornerLength(), getGameTipVisualConfig().getBaseItemPlacerCornerColor(), getGameTipVisualConfig().getBaseItemPlacerShape3DId());
        }
    }

    @Override
    public void onStateChanged(BaseItemPlacer baseItemPlacer) {
        if (baseItemPlacer == null) {
            onFailed();
        }
    }
}
