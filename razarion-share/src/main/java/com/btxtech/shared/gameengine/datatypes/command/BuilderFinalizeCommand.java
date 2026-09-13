package com.btxtech.shared.gameengine.datatypes.command;

import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import org.dominokit.jackson.annotation.JSONMapper;

/**
 * User: Razarion contributors
 * Date: Sep 12, 2010
 * Time: 1:04:16 PM
 */
@JSONMapper
public class BuilderFinalizeCommand extends PathToDestinationCommand {
    private int buildingId;

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    @Override
    public GameConnectionPacket connectionPackage() {
        return GameConnectionPacket.BUILDER_FINALIZE_COMMAND;
    }

    @Override
    public String toString() {
        return "BuilderFinalizeCommand{" +
                "buildingId=" + buildingId +
                '}';
    }
}
