package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.uiservice.itemplacer.BaseItemPlacer;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerListener;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
import com.btxtech.uiservice.terrain.TerrainUiService;
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
public class SpawnPlacerTipTask extends AbstractTipTask implements BaseItemPlacerListener {
    @Inject
    private BaseItemPlacerService baseItemPlacerService;
    @Inject
    private TerrainUiService terrainUiService;
    private int spawnItemTypeId;
    private DecimalPosition positionHint;

    public void init(int spawnItemTypeId, DecimalPosition positionHint) {
        this.spawnItemTypeId = spawnItemTypeId;
        this.positionHint = positionHint;
    }

    @Override
    public void internalStart() {
        baseItemPlacerService.addListener(this);
    }

    @Override
    public void internalCleanup() {
        baseItemPlacerService.removeListener(this);
    }

    @Override
    public boolean isFulfilled() {
        return false;
    }

    @Override
    public InGameTipVisualization createInGameTipVisualization() {
        InGamePositionTipVisualization visualization = new InGamePositionTipVisualization(getGameTipVisualConfig().getCornerMoveDistance(), getGameTipVisualConfig().getCornerMoveDuration(), getGameTipVisualConfig().getCornerLength(), getGameTipVisualConfig().getBaseItemPlacerCornerColor(), getGameTipVisualConfig().getBaseItemPlacerShape3DId(), getGameTipVisualConfig().getOutOfViewShape3DId());
        terrainUiService.getTerrainPosition(positionHint, visualization::setPosition);
        return visualization;
    }

    @Override
    protected void onSpawnSyncItem(SyncBaseItemSimpleDto syncBaseItem) {
        if (syncBaseItem.getItemTypeId() == spawnItemTypeId) {
            onSucceed();
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
