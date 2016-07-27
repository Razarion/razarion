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

/**
 * User: beat
 * Date: Aug 1, 2009
 * Time: 1:04:16 PM
 */
public class FactoryCommand extends BaseCommand {
    private int toBeBuilt;

    public int getToBeBuilt() {
        return toBeBuilt;
    }

    public void setToBeBuilt(int toBeBuilt) {
        this.toBeBuilt = toBeBuilt;
    }

    @Override
    public String toString() {
        return super.toString() + " toBeBuilt: " + toBeBuilt;
    }

}