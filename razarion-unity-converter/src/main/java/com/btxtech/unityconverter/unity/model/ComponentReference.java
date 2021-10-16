package com.btxtech.unityconverter.unity.model;

public class ComponentReference {
    private FileID component;

    public FileID getComponent() {
        return component;
    }

    public void setComponent(FileID component) {
        this.component = component;
    }

    @Override
    public String toString() {
        return "ComponentReference{" +
                "component='" + component + '\'' +
                '}';
    }
}
