package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
import com.btxtech.uiservice.tip.visualization.InGamePositionTipVisualization;
import com.btxtech.uiservice.tip.visualization.InGameTipVisualization;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * User: beat
 * Date: 22.08.12
 * Time: 13:19
 */
@Dependent
public class SpawnPlacerTipTask extends AbstractTipTask {
    @Inject
    private BaseItemPlacerService baseItemPlacerService;
    @Inject
    private TerrainService terrainService;
    private int spawnItemTypeId;
    private DecimalPosition positionHint;

    public void init(int spawnItemTypeId, DecimalPosition positionHint) {
        this.spawnItemTypeId = spawnItemTypeId;
        this.positionHint = positionHint;
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
    public InGameTipVisualization createInGameTip() {
        return new InGamePositionTipVisualization(terrainService.getPosition3d(positionHint), getGameTipVisualConfig().getCornerMoveDistance(), getGameTipVisualConfig().getCornerMoveDuration(), getGameTipVisualConfig().getCornerLength(), getGameTipVisualConfig().getBaseItemPlacerCornerColor(), getGameTipVisualConfig().getBaseItemPlacerShape3DId(), getGameTipVisualConfig().getOutOfViewShape3DId());
    }

    @Override
    protected void onSpawnSyncItem(SyncBaseItem syncBaseItem) {
        if (syncBaseItem.getBaseItemType().getId() == spawnItemTypeId) {
            onSucceed();
        }
    }
}
