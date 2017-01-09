package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.uiservice.item.BaseItemUiService;
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
public class SendAttackCommandTipTask extends AbstractTipTask {
    @Inject
    private BaseItemUiService baseItemUiService;
    private Integer targetItemTypeId;
    private PlaceConfig placeConfig;

    public void init(Integer targetItemTypeId, PlaceConfig placeConfig) {
        this.targetItemTypeId = targetItemTypeId;
        this.placeConfig = placeConfig;
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
        if (commandInfo.getType() == CommandInfo.Type.ATTACK) {
            onSucceed();
        }
    }

    @Override
    public InGameTipVisualization createInGameTipVisualization() {
        SyncBaseItemSimpleDto target = baseItemUiService.findEnemyItemWithPlace(placeConfig);
        if (target == null) {
            throw new IllegalArgumentException("Can not create visualization. No target available to attack. targetItemTypeId: " + targetItemTypeId + " placeConfig: " + placeConfig);
        }
        return new InGameItemTipVisualization(target, getGameTipVisualConfig().getCornerMoveDistance(), getGameTipVisualConfig().getCornerMoveDuration(), getGameTipVisualConfig().getCornerLength(), getGameTipVisualConfig().getAttackCommandCornerColor(), getGameTipVisualConfig().getDefaultCommandShape3DId(), getGameTipVisualConfig().getOutOfViewShape3DId());
    }
}
