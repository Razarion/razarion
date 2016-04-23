package com.btxtech.shared;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by Beat
 * 23.04.2016.
 */
@Portable
public class SlopeNameId {
    private int id;
    private String internalName;

    /**
     * Used by Errai
     */
    public SlopeNameId() {
    }

    public SlopeNameId(int id, String internalName) {
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
}
