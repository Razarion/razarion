package com.btxtech.shared.gameengine;

import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 16.07.2016.
 */
// Only used on the server. See: com.btxtech.server.gameengine.ServerUnlockService, com.btxtech.uiservice.unlock.UnlockUiService
@Deprecated
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
