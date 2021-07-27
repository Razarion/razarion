package com.btxtech.shared.dto;


import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 23.04.2016.
 */
@JsType
public class ObjectNameId {
    public int id; // Only public for Angular access
    public String internalName; // Only public for Angular access

    /**
     * Used by GWT
     */
    @JsIgnore
    public ObjectNameId() {
    }

    @JsIgnore
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

    public static int compare(ObjectNameId o1, ObjectNameId o2) {
        if (o1.getInternalName()== null && o2.getInternalName() != null) {
            return 1;
        } else if (o1.getInternalName() != null && o2.getInternalName() == null) {
            return -1;
        } else if (o1.getInternalName() == null&& o2.getInternalName() == null) {
            return 0;
        } else {
            return o1.getInternalName().compareTo(o2.getInternalName());
        }
    }
}
