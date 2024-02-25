package com.btxtech.shared.dto;

/**
 * Created by Beat
 * on 23.09.2017.
 */

@Deprecated
public class UnlockedBackendInfo {
    private int id;
    private String internalName;

    public int getId() {
        return id;
    }

    public UnlockedBackendInfo setId(int id) {
        this.id = id;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public UnlockedBackendInfo setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }
}
