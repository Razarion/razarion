package com.btxtech.shared.dto;


/**
 * Created by Beat
 * 23.04.2016.
 */
public class ObjectNameId {
    private int id;
    private String internalName;

    /**
     * Used by GWT
     */
    public ObjectNameId() {
    }

    public ObjectNameId(int id, String internalName) {
        this.id = id;
        this.internalName = internalName;
    }

    public int getId() {
        return id;
    }

    public String getInternalName() {
        return internalName;
    }

    @Override
    public String toString() {
        return internalName + " (" + id + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ObjectNameId that = (ObjectNameId) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
