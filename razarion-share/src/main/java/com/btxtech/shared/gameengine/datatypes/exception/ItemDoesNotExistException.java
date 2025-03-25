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


/**
 * User: beat
 * Date: 14.11.2009
 * Time: 19:48:26
 */
public class ItemDoesNotExistException extends RuntimeException {
    private int id;

    public ItemDoesNotExistException() {
    }

    public ItemDoesNotExistException(int id) {
        super("Item does not exist: " + id);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
