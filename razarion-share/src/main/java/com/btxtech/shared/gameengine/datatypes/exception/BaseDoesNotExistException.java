package com.btxtech.shared.gameengine.datatypes.exception;


import com.btxtech.shared.gameengine.datatypes.PlayerBase;

/**
 * User: Razarion contributors
 * Date: 02.12.2009
 * Time: 16:10:39
 */
public class BaseDoesNotExistException extends RuntimeException {
    public BaseDoesNotExistException() {
    }

    public BaseDoesNotExistException(PlayerBase playerBase) {
        super("Base does not exist: " + playerBase);
    }
}
