package com.btxtech.shared.gameengine.datatypes.exception;

import com.btxtech.shared.gameengine.planet.model.SyncItem;

/**
 * User: beat
 * Date: 22.02.13
 * Time: 16:42
 */
public class TargetHasNoPositionException extends RuntimeException {
    public TargetHasNoPositionException() {
    }

    public TargetHasNoPositionException(SyncItem syncItem) {
        super("SyncItem has no position: " + syncItem);
    }
}
