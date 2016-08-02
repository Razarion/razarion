package com.btxtech.shared.gameengine.datatypes.exception;

import com.btxtech.shared.gameengine.planet.model.SyncItemContainer;

/**
 * User: beat
 * Date: 15.03.2012
 * Time: 15:12:49
 */
public class ItemContainerFullException extends Exception {
    public ItemContainerFullException() {
    }

    public ItemContainerFullException(SyncItemContainer syncItemContainer, int count) {
        super("Item container is full (count " + count + ") " + syncItemContainer.getSyncBaseItem());
    }
}
