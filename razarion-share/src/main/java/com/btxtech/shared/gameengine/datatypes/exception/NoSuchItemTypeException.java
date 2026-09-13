package com.btxtech.shared.gameengine.datatypes.exception;

/**
 * User: Razarion contributors
 * Date: 02.12.2009
 * Time: 16:10:39
 */
public class NoSuchItemTypeException extends RuntimeException {

    /**
     * Used bw GWT
     */
    NoSuchItemTypeException() {
    }

    public NoSuchItemTypeException(String name) {
        super("No such item type: " + name);
    }

    public NoSuchItemTypeException(Class clazz, Integer itemTypeId) {
        super("No such item type: " + clazz + " id: " + itemTypeId);
    }
}
