package com.btxtech.shared.gameengine.datatypes.command;

import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;

import java.util.Date;

/**
 * User: Razarion contributors
 * Date: Aug 1, 2009
 * Time: 12:56:55 PM
 */
public abstract class BaseCommand {
    private int id;
    private Date timeStamp;

    public abstract GameConnectionPacket connectionPackage();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void updateTimeStamp() {
        timeStamp = new Date();
    }

    @Override
    public String toString() {
        return getClass().getName() + " " + id;
    }
}
