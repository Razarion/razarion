package com.btxtech.shared.system.alarm;

import java.util.Date;

public class Alarm {
    public enum Type {
        NO_LEVELS,
        USER_HAS_NO_LEVEL,
        NO_WARM_GAME_UI_CONTEXT,
        NO_PLANETS
    }
    private Type type;
    private Date date;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Alarm type(Type type) {
        setType(type);
        return this;
    }

    public Alarm date(Date date) {
        setDate(date);
        return this;
    }

    @Override
    public String toString() {
        return "Alarm{" +
                "type=" + type +
                ", date=" + date +
                '}';
    }
}
