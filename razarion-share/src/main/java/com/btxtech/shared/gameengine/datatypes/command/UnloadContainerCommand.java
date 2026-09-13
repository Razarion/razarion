package com.btxtech.shared.gameengine.datatypes.command;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import org.dominokit.jackson.annotation.JSONMapper;

/**
 * User: Razarion contributors
 * Date: 05.05.2010
 * Time: 12:27:00
 */
@JSONMapper
public class UnloadContainerCommand extends /*PathToDestinationCommand*/ BaseCommand {
    private DecimalPosition unloadPos;

    public DecimalPosition getUnloadPos() {
        return unloadPos;
    }

    public void setUnloadPos(DecimalPosition unloadPos) {
        this.unloadPos = unloadPos;
    }

    @Override
    public GameConnectionPacket connectionPackage() {
        return GameConnectionPacket.UNLOAD_CONTAINER_COMMAND;
    }
}
