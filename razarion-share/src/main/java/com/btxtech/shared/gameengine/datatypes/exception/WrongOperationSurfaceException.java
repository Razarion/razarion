package com.btxtech.shared.gameengine.datatypes.exception;

import com.btxtech.shared.gameengine.planet.model.SyncItem;

/**
 * User: beat
 * Date: 15.03.2012
 * Time: 15:12:49
 */
public class WrongOperationSurfaceException extends Exception {
    /**
     * Used by GWT
     */
    public WrongOperationSurfaceException() {
    }

    public WrongOperationSurfaceException(SyncItem syncItem) {
        super("SyncItem can not perform the operation because the operation surface type is wrong. Item: " + syncItem);
    }
}
