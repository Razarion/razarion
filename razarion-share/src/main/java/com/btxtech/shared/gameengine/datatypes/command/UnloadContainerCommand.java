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
 * Date: 05.05.2010
 * Time: 12:27:00
 */
public class UnloadContainerCommand extends BaseCommand {
    private DecimalPosition unloadPos;

    public DecimalPosition getUnloadPos() {
        return unloadPos;
    }

    public void setUnloadPos(DecimalPosition unloadPos) {
        this.unloadPos = unloadPos;
    }

    @Override
    public ConnectionMarshaller.Package connectionPackage() {
        return ConnectionMarshaller.Package.UNLOAD_CONTAINER_COMMAND;
    }
}
