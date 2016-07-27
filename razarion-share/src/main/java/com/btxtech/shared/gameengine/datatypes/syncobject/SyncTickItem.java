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

package com.btxtech.shared.gameengine.datatypes.syncobject;


import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;

/**
 * User: beat
 * Date: 05.10.2010
 * Time: 22:32:21
 */
@Deprecated
abstract public class SyncTickItem extends SyncItem {
    /**
     * Ticks this sync item
     *
     * @return true if more tick are needed to fullfil the job
     * @throws ItemDoesNotExistException if the target item does no exist any longer
     * @throws NoSuchItemTypeException   if the target item type does not exist
     */
    public abstract boolean tick() throws ItemDoesNotExistException, NoSuchItemTypeException;

    public abstract void stop();
}
