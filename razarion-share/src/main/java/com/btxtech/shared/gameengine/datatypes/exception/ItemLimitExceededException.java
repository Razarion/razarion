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

import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;

/**
 * User: beat
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
