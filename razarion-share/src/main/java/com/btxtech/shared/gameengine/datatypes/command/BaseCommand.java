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

package com.btxtech.shared.gameengine.datatypes.command;

import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;

import java.util.Date;

/**
 * User: beat
 * Date: Aug 1, 2009
 * Time: 12:56:55 PM
 */
public abstract class BaseCommand {
    private int id;
    private Date timeStamp;

    public abstract GameConnectionPacket connectionPackage();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void updateTimeStamp() {
        timeStamp = new Date();
    }

    @Override
    public String toString() {
        return getClass().getName() + " " + id;
    }
}
