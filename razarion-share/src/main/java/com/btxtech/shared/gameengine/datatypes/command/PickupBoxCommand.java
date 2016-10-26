package com.btxtech.shared.gameengine.datatypes.command;

/**
 * User: beat
 * Date: 21.05.12
 * Time: 00:27
 */
public class PickupBoxCommand extends PathToDestinationCommand {
    private int synBoxItemId;

    public int getSynBoxItemId() {
        return synBoxItemId;
    }

    public void setSynBoxItemId(int synBoxItemId) {
        this.synBoxItemId = synBoxItemId;
    }
}
