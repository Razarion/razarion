package com.btxtech.shared.dto;

public interface Config extends ObjectNameIdProvider {
    int getId();

    String getInternalName();

    void setInternalName(String internalName);

    @Override
    default ObjectNameId createObjectNameId() {
        return new ObjectNameId(getId(), getInternalName());
    }
}
