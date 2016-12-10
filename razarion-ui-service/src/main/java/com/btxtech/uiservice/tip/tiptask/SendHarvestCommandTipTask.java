package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.command.HarvestCommand;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
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
public class SendHarvestCommandTipTask extends AbstractTipTask {
    @Inject
    private SyncItemContainerService syncItemContainerService;
    private int toCollectFormId;
    private PlaceConfig resourceSelection;

    public void init(int toCollectFormId, PlaceConfig resourceSelection) {
        this.toCollectFormId = toCollectFormId;
        this.resourceSelection = resourceSelection;
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
        if (baseCommand instanceof HarvestCommand) {
            onSucceed();
        }
    }

    @Override
    public InGameTipVisualization createInGameTip() {
        Collection<SyncResourceItem> syncResourceItems = syncItemContainerService.findResourceItemWithPlace(toCollectFormId, resourceSelection);
        if (syncResourceItems.isEmpty()) {
            throw new IllegalArgumentException("Can not create game tip. No resource available to mark: " + resourceSelection);
        }
        return new InGameItemTipVisualization(CollectionUtils.getFirst(syncResourceItems), getGameTipVisualConfig().getCornerMoveDistance(), getGameTipVisualConfig().getCornerMoveDuration(), getGameTipVisualConfig().getCornerLength(), getGameTipVisualConfig().getGrabCommandCornerColor(), getGameTipVisualConfig().getDefaultCommandShape3DId(), getGameTipVisualConfig().getOutOfViewShape3DId());
    }
}
