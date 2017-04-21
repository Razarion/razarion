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

import com.btxtech.shared.gameengine.planet.connection.ConnectionMarshaller;

/**
 * User: beat
 * Date: Sep 12, 2010
 * Time: 1:04:16 PM
 */
public class BuilderFinalizeCommand extends PathToDestinationCommand {
    private int buildingId;

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    @Override
    public ConnectionMarshaller.Package connectionPackage() {
        return ConnectionMarshaller.Package.BUILDER_FINALIZE_COMMAND;
    }

    @Override
    public String toString() {
        return "BuilderFinalizeCommand{" +
                "buildingId=" + buildingId +
                '}';
    }
}
