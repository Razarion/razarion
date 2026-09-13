package com.btxtech.shared.gameengine.datatypes.command;

import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import org.dominokit.jackson.annotation.JSONMapper;

/**
 * User: Razarion contributors
 * Date: Aug 1, 2009
 * Time: 1:04:16 PM
 */
@JSONMapper
public class HarvestCommand extends PathToDestinationCommand {
    private Integer target;

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    @Override
    public GameConnectionPacket connectionPackage() {
        return GameConnectionPacket.HARVESTER_COMMAND;
    }

    @Override
    public String toString() {
        return super.toString() + " target: " + target;
    }

}