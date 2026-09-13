package com.btxtech.shared.gameengine.datatypes.exception;


import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;

/**
 * User: Razarion contributors
 * Date: 29.08.2010
 * Time: 17:54:52
 */
public class PositionTakenException extends RuntimeException {
    /**
     * Used By GWT
     */
    PositionTakenException() {
    }

    public PositionTakenException(DecimalPosition position, int itemTypeId) {
        super("The position is not free: " + position + " for itemTypeId: " + itemTypeId);
    }

    public PositionTakenException(DecimalPosition position, ItemType itemType) {
        super("The position is not free: " + position + " for itemType: " + itemType);
    }
}
