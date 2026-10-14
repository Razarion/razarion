package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import jakarta.inject.Inject;

/**
 * Fulfilled while the player has nothing of their own left in the place: no unit, no building, no
 * site going up. How it went does not matter - sold, destroyed, never there - so a player who has
 * nothing there passes at once. Level 9 starts by clearing the noob island (2026-10-14); counting
 * sales instead would have needed to know beforehand what each player had left there.
 */
public class RegionClearedComparison extends AbstractTickComparison {
    private final BaseItemService baseItemService;
    private AbstractConditionProgress abstractConditionProgress;
    private PlaceConfig placeConfig;
    private String userId;
    private Integer remaining;

    @Inject
    public RegionClearedComparison(GameLogicService gameLogicService, BaseItemService baseItemService) {
        super(gameLogicService);
        this.baseItemService = baseItemService;
    }

    public void init(PlaceConfig placeConfig, String userId) {
        this.placeConfig = placeConfig;
        this.userId = userId;
        remaining = count();
    }

    @Override
    public void tick() {
        int now = count();
        if (remaining == null || now != remaining) {
            remaining = now;
            onProgressChanged();
        }
    }

    private int count() {
        PlayerBaseFull playerBase = baseItemService.getPlayerBaseFull4UserId(userId);
        if (playerBase == null) {
            return 0;
        }
        // findItemsInPlace leaves out what sits in a container: a unit loaded into a transporter is
        // wherever the transporter is.
        return (int) playerBase.findItemsInPlace(placeConfig).stream().filter(SyncBaseItem::isAlive).count();
    }

    @Override
    public boolean isFulfilled() {
        return remaining != null && remaining == 0;
    }

    @Override
    public AbstractConditionProgress getAbstractConditionProgress() {
        return abstractConditionProgress;
    }

    @Override
    public void setAbstractConditionProgress(AbstractConditionProgress abstractConditionProgress) {
        this.abstractConditionProgress = abstractConditionProgress;
    }

    @Override
    public void fillGenericComparisonValues(BackupComparisionInfo backupComparisionInfo) {
        // Nothing to keep: what is left there is counted again from the base.
    }

    @Override
    public void restoreFromGenericComparisonValue(BackupComparisionInfo backupComparisionInfo) {
        remaining = null;
    }

    /** {@link QuestProgressInfo#getCount()} is what is still left in the place. */
    @Override
    public QuestProgressInfo generateQuestProgressInfo() {
        return new QuestProgressInfo().setCount(remaining != null ? remaining : count());
    }
}
