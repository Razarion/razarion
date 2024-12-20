package com.btxtech.shared.gameengine.datatypes.command;

import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import org.dominokit.jackson.annotation.JSONMapper;

/**
 * User: beat
 * Date: 21.05.12
 * Time: 00:27
 */
@JSONMapper
public class PickupBoxCommand extends PathToDestinationCommand {
    private int synBoxItemId;

    public int getSynBoxItemId() {
        return synBoxItemId;
    }

    public void setSynBoxItemId(int synBoxItemId) {
        this.synBoxItemId = synBoxItemId;
    }

    @Override
    public GameConnectionPacket connectionPackage() {
        return GameConnectionPacket.PICK_BOX_COMMAND;
    }
}
