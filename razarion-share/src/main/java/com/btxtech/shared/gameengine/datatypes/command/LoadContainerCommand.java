package com.btxtech.shared.gameengine.datatypes.command;

import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import org.dominokit.jackson.annotation.JSONMapper;

/**
 * User: Razarion contributors
 * Date: 01.05.2010
 * Time: 12:42:05
 */
@JSONMapper
public class LoadContainerCommand extends PathToDestinationCommand {
    private int itemContainer;

    public Integer getItemContainer() {
        return itemContainer;
    }

    public void setItemContainer(int itemContainer) {
        this.itemContainer = itemContainer;
    }

    @Override
    public GameConnectionPacket connectionPackage() {
        return GameConnectionPacket.LOAD_CONTAINER_COMMAND;
    }
}
