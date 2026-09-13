package com.btxtech.shared.gameengine.datatypes.command;

import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import org.dominokit.jackson.annotation.JSONMapper;

/**
 * Removes a waiting unit from a factory's build queue. {@code queueIndex} indexes the waiting queue
 * only (the actively built unit is not cancelable). Master-authoritative; the resulting queue change
 * is synced back to all clients via SyncBaseItemInfo.
 */
@JSONMapper
public class FactoryCancelQueueCommand extends BaseCommand {
    private int queueIndex;

    public int getQueueIndex() {
        return queueIndex;
    }

    public void setQueueIndex(int queueIndex) {
        this.queueIndex = queueIndex;
    }

    @Override
    public GameConnectionPacket connectionPackage() {
        return GameConnectionPacket.FACTORY_CANCEL_QUEUE_COMMAND;
    }

    @Override
    public String toString() {
        return super.toString() + " queueIndex: " + queueIndex;
    }

}
