package com.btxtech.shared.system.alarm;

import java.util.Date;

public class Alarm {
    public enum Type {
        NO_LEVELS,
        USER_HAS_NO_LEVEL,
        NO_WARM_GAME_UI_CONTEXT
    }
    private Type type;
    private Date date;

    public Alarm(Type type) {
        this.type = type;
        date = new Date();
    }

    public Type getType() {
        return type;
    }

    public Date getDate() {
        return date;
    }
}
