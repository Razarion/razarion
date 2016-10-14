package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 14.10.2016.
 */
public class ClipConfig {
    private int id;
    private int shape3DId;
    private Integer soundId;
    private int durationMillis;

    public int getId() {
        return id;
    }

    public ClipConfig setId(int id) {
        this.id = id;
        return this;
    }

    public int getShape3DId() {
        return shape3DId;
    }

    public ClipConfig setShape3DId(int shape3DId) {
        this.shape3DId = shape3DId;
        return this;
    }

    public Integer getSoundId() {
        return soundId;
    }

    public ClipConfig setSoundId(Integer soundId) {
        this.soundId = soundId;
        return this;
    }

    public int getDurationMillis() {
        return durationMillis;
    }

    public ClipConfig setDurationMillis(int durationMillis) {
        this.durationMillis = durationMillis;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClipConfig that = (ClipConfig) o;
        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
