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


import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.connection.ConnectionMarshaller;

/**
 * User: beat
 * Date: Aug 1, 2009
 * Time: 1:04:16 PM
 */
public class BuilderCommand extends PathToDestinationCommand {
    private int toBeBuiltId;
    private DecimalPosition positionToBeBuilt;

    public int getToBeBuiltId() {
        return toBeBuiltId;
    }

    public void setToBeBuiltId(int toBeBuiltId) {
        this.toBeBuiltId = toBeBuiltId;
    }

    public DecimalPosition getPositionToBeBuilt() {
        return positionToBeBuilt;
    }

    public void setPositionToBeBuilt(DecimalPosition positionToBeBuilt) {
        this.positionToBeBuilt = positionToBeBuilt;
    }

    @Override
    public ConnectionMarshaller.Package connectionPackage() {
        return ConnectionMarshaller.Package.BUILDER_COMMAND;
    }

    @Override
    public String toString() {
        return super.toString() + " toBeBuiltId: " + toBeBuiltId + " positionToBeBuilt: " + positionToBeBuilt;
    }

}
