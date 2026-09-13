package com.btxtech.shared.gameengine.datatypes.exception;


/**
 * User: Razarion contributors
 * Date: 14.11.2009
 * Time: 19:48:26
 */
public class ItemDoesNotExistException extends RuntimeException {
    private int id;

    public ItemDoesNotExistException() {
    }

    public ItemDoesNotExistException(int id) {
        super("Item does not exist: " + id);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
