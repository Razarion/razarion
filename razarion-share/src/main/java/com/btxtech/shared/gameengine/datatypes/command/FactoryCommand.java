package com.btxtech.shared.gameengine.datatypes.command;

import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import org.dominokit.jackson.annotation.JSONMapper;

/**
 * User: Razarion contributors
 * Date: Aug 1, 2009
 * Time: 1:04:16 PM
 */
@JSONMapper
public class FactoryCommand extends BaseCommand {
    private int toBeBuiltId;

    public int getToBeBuiltId() {
        return toBeBuiltId;
    }

    public void setToBeBuiltId(int toBeBuiltId) {
        this.toBeBuiltId = toBeBuiltId;
    }

    @Override
    public GameConnectionPacket connectionPackage() {
        return GameConnectionPacket.FACTORY_COMMAND;
    }

    @Override
    public String toString() {
        return super.toString() + " toBeBuiltId: " + toBeBuiltId;
    }

}