package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.gameengine.datatypes.command.AttackCommand;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.tip.visualization.InGameItemTipVisualization;
import com.btxtech.uiservice.tip.visualization.InGameTipVisualization;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collection;

/**
 * User: beat
 * Date: 22.08.12
 * Time: 12:53
 */
@Dependent
public class SendAttackCommandTipTask extends AbstractTipTask {
    @Inject
    private SyncItemContainerService syncItemContainerService;
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
    protected void onCommandSent(BaseCommand baseCommand) {
        if (baseCommand instanceof AttackCommand) {
            onSucceed();
        }
    }

    public InGameTipVisualization createInGameTip() {
        Collection<SyncBaseItem> targets = syncItemContainerService.findEnemyBaseItemWithPlace(null, getPlayerBase(), placeConfig);
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("Can not create visualization. No target available to attack. targetItemTypeId: " + targetItemTypeId + " placeConfig: " + placeConfig);
        }
        return new InGameItemTipVisualization(CollectionUtils.getFirst(targets), getGameTipVisualConfig().getCornerMoveDistance(), getGameTipVisualConfig().getCornerMoveDuration(), getGameTipVisualConfig().getCornerLength(), getGameTipVisualConfig().getAttackCommandCornerColor(), getGameTipVisualConfig().getDefaultCommandShape3DId());
    }
}
