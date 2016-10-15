package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 14.10.2016.
 */
public class ClipConfig implements ObjectNameIdProvider {
    private int id;
    private String internalName;
    private Integer shape3DId;
    private Integer soundId;
    private Integer durationMillis;

    public int getId() {
        return id;
    }

    public String getInternalName() {
        return internalName;
    }

    public ClipConfig setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public ClipConfig setId(int id) {
        this.id = id;
        return this;
    }

    public Integer getShape3DId() {
        return shape3DId;
    }

    public ClipConfig setShape3DId(Integer shape3DId) {
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

    public Integer getDurationMillis() {
        return durationMillis;
    }

    public ClipConfig setDurationMillis(Integer durationMillis) {
        this.durationMillis = durationMillis;
        return this;
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(id, internalName);
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

    @Override
    public String toString() {
        return "ClipConfig{" +
                "id=" + id +
                ", internalName='" + internalName + '\'' +
                ", shape3DId=" + shape3DId +
                ", soundId=" + soundId +
                ", durationMillis=" + durationMillis +
                '}';
    }
}
