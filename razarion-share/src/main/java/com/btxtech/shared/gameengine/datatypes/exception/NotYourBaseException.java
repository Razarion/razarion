package com.btxtech.shared.gameengine.datatypes.exception;

import com.btxtech.shared.gameengine.datatypes.PlayerBase;

/**
 * User: Razarion contributors
 * Date: Sep 30, 2009
 * Time: 12:32:11 PM
 */
public class NotYourBaseException extends RuntimeException {
    public NotYourBaseException() {
    }

    public NotYourBaseException(PlayerBase actorBase, PlayerBase targetBase) {
        super("Invalid access from base: " + actorBase + " to " + targetBase);
    }
}
