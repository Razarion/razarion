package com.btxtech.shared.gameengine.datatypes.exception;

import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;

/**
 * User: Razarion contributors
 * Date: 14.09.2010
 * Time: 12:45:12
 * <p>
 * The item limit defined in the level has been exceeded
 */
public class ItemLimitExceededException extends RuntimeException {

    public ItemLimitExceededException() {
    }

    public ItemLimitExceededException(BaseItemType newItemType, int itemCount2Add, PlayerBase simpleBase) {
        super("Item type limitation exceeded for '" + newItemType + "' to added '" + itemCount2Add + "' for base '" + simpleBase + "'");
    }
}
