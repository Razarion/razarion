package com.btxtech.shared.gameengine.datatypes.exception;


import com.btxtech.shared.datatypes.Index;

/**
 * User: beat
 * Date: 02.10.2011
 * Time: 16:38:22
 */
public class PathCanNotBeFoundException extends RuntimeException {
    public PathCanNotBeFoundException() {
    }

    public PathCanNotBeFoundException(String message, Index start, Index destination) {
        super(message + " start: " + start + " destination: " + destination);
    }
}
