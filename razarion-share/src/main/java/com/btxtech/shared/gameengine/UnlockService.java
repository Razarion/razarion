package com.btxtech.shared.gameengine;

import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 16.07.2016.
 */
@Singleton
public class UnlockService {

    public boolean isItemLocked(BaseItemType baseItemType, PlayerBase playerBase) {
        return false;
//        return baseItemType.unlockNeeded()
//                && !playerBase.getCharacter().isBot()
//                && playerBase.getHumanPlayerId().containsUnlockedItemTypeId(baseItemType.getId());
    }

    // TODO boolean isQuestLocked(QuestInfo questInfo, PlayerBase simpleBase);

    // TODO UnlockContainer getUnlockContainer(PlayerBase simpleBase);

}
