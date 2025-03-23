package com.btxtech.shared.dto;

public class ObjectNameId {
    public int id;
    public String internalName;

    public ObjectNameId(int id, String internalName) {
        this.id = id;
        this.internalName = internalName;
    }

    public static int compare(ObjectNameId o1, ObjectNameId o2) {
        if (o1.getInternalName() == null && o2.getInternalName() != null) {
            return 1;
        } else if (o1.getInternalName() != null && o2.getInternalName() == null) {
            return -1;
        } else if (o1.getInternalName() == null && o2.getInternalName() == null) {
            return 0;
        } else {
            return o1.getInternalName().compareTo(o2.getInternalName());
        }
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
