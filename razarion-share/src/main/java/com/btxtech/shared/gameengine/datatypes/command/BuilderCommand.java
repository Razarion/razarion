package com.btxtech.shared.gameengine.datatypes.command;


import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import org.dominokit.jackson.annotation.JSONMapper;

/**
 * User: Razarion contributors
 * Date: Aug 1, 2009
 * Time: 1:04:16 PM
 */
@JSONMapper
public class BuilderCommand extends PathToDestinationCommand {
    private int toBeBuiltId;
    private DecimalPosition positionToBeBuilt;
    private DecimalPosition rallyPoint;

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

    public DecimalPosition getRallyPoint() {
        return rallyPoint;
    }

    public void setRallyPoint(DecimalPosition rallyPoint) {
        this.rallyPoint = rallyPoint;
    }

    @Override
    public GameConnectionPacket connectionPackage() {
        return GameConnectionPacket.BUILDER_COMMAND;
    }

    @Override
    public String toString() {
        return super.toString() + " toBeBuiltId: " + toBeBuiltId + " positionToBeBuilt: " + positionToBeBuilt + " rallyPoint: " + rallyPoint;
    }

}
