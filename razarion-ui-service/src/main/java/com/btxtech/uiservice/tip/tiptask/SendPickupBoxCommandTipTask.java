package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.command.PickupBoxCommand;
import com.btxtech.shared.gameengine.planet.BoxService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.utils.CollectionUtils;
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
    private BoxService boxService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
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
    public InGameTipVisualization createInGameTip() {
        Collection<SyncBoxItem> syncBoxItems = syncItemContainerService.findBoxItemWithPlace(boxItemTypeId, null);
        if (syncBoxItems.isEmpty()) {
            throw new IllegalArgumentException("Can not create game tip. No box available to mark: " + boxItemTypeId);
        }
        return new InGameItemTipVisualization(CollectionUtils.getFirst(syncBoxItems), getGameTipVisualConfig().getCornerMoveDistance(), getGameTipVisualConfig().getCornerMoveDuration(), getGameTipVisualConfig().getCornerLength(), getGameTipVisualConfig().getGrabCommandCornerColor(), getGameTipVisualConfig().getDefaultCommandShape3DId(), getGameTipVisualConfig().getOutOfViewShape3DId());
    }

    @Override
    protected void onCommandSent(BaseCommand baseCommand) {
        if (baseCommand instanceof PickupBoxCommand) {
            int boxItemTypeId = boxService.getSyncBoxItem(((PickupBoxCommand) baseCommand).getSynBoxItemId()).getBoxItemType().getId();
            if (boxItemTypeId == this.boxItemTypeId) {
                onSucceed();
            }
        }
    }
}
