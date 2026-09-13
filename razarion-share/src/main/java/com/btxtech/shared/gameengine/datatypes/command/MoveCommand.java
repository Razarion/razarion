package com.btxtech.shared.gameengine.datatypes.command;

import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import org.dominokit.jackson.annotation.JSONMapper;

/**
 * User: Razarion contributors
 * Date: Aug 1, 2009
 * Time: 1:04:35 PM
 */
@JSONMapper
public class MoveCommand extends PathToDestinationCommand {
    @Override
    public GameConnectionPacket connectionPackage() {
        return GameConnectionPacket.MOVE_COMMAND;
    }
}
