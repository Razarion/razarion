package com.btxtech.shared.gameengine.datatypes.command;

/**
 * User: beat
 * Date: 21.05.12
 * Time: 00:27
 */
public class PickupBoxCommand extends PathToDestinationCommand {
    private int box;

    public int getBox() {
        return box;
    }

    public void setBox(int box) {
        this.box = box;
    }
}
