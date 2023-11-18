package com.btxtech.shared.system.alarm;

import jsinterop.annotations.JsType;

import java.util.Date;

@JsType
public class Alarm {
    public enum Type {
        NO_LEVELS,
        USER_HAS_NO_LEVEL,
        NO_WARM_GAME_UI_CONTEXT,
        TERRAIN_SHAPE_SETUP_FAILED,
        NO_GAME_UI_CONTROL_CONFIG_ENTITY_FOR_LEVEL_ID,
        INVALID_GAME_UI_CONTEXT,
        NO_SCENES,
        FAIL_START_GAME_ENGINE,
        RENDER_GROUND_FAILED,
        INVALID_SLOPE_CONFIG,
        INVALID_VERTEX_CONTAINER,
        RENDER_ENGINE_UNIFORM,
        INVALID_AUDIO_SERVICE,
        INVALID_PROPERTY,
        INVALID_TERRAIN_OBJECT,
        INVALID_SHAPE_3D,
        INVALID_BASE_ITEM,
        INVALID_RESOURCE_ITEM,
        INVALID_PARTICLE_SHAPE_CONFIG,
        TERRAIN_SHAPE_FAILED_SLOPE_POSITION,
        TERRAIN_SHAPE_FAILED_TERRAIN_OBJECT_POSITION
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

    /**
     * Also called angular
     * @return detailed alarm message
     */
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

    @SuppressWarnings("unused") // Called by Angular
    public String angularTypeString() {
        return type.name();
    }

    @SuppressWarnings("unused") // Called by Angular
    public long angularDateAsLong() {
        return date.getTime();
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
