package com.btxtech.shared.system;

/**
 * Created by Beat
 * 25.04.2017.
 */
public enum SystemConnectionPacket implements ConnectionMarshaller.Packet {
    LEVEL_UPDATE(Integer.class);

    private Class theClass;

    SystemConnectionPacket(Class theClass) {
        this.theClass = theClass;
    }

    @Override
    public Class getTheClass() {
        return theClass;
    }

}
