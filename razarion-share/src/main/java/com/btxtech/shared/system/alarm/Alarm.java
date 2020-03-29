package com.btxtech.shared.system.alarm;

import java.util.Date;

public class Alarm {
    public enum Type {
        NO_LEVELS,
        USER_HAS_NO_LEVEL,
        NO_WARM_GAME_UI_CONTEXT,
        TERRAIN_SHAPE_SETUP_FAILED,
        NO_GAME_UI_CONTROL_CONFIG_ENTITY_FOR_LEVEL_ID,
        INVALID_GAME_UI_CONTEXT,
        START_GAME_UI_CONTROL_FAILED,
        NO_SCENES,
        FAIL_START_GAME_ENGINE,
        RENDER_GROUND_FAILED
    }
    private Type type;
    private Date date;
    private String text;
    private Integer id;

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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Alarm type(Type type) {
        setType(type);
        return this;
    }

    public Alarm date(Date date) {
        setDate(date);
        return this;
    }

    public Alarm text(String text) {
        setText(text);
        return this;
    }

    public Alarm id(Integer id) {
        setId(id);
        return this;
    }

    @Override
    public String toString() {
        return "Alarm{" +
                "type=" + type +
                ", date=" + date +
                ", text=" + text +
                ", id=" + id +
                '}';
    }
}
