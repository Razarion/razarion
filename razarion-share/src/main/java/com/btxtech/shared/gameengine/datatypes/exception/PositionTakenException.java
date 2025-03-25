/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.shared.gameengine.datatypes.exception;


import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;

/**
 * User: beat
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
