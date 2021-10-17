package com.btxtech.unityconverter.unity.model;

public class ComponentReference {
    // TODO this property name should be dynamic
    private Reference component;

    public Reference getComponent() {
        return component;
    }

    public void setComponent(Reference component) {
        this.component = component;
    }

    @Override
    public String toString() {
        return "ComponentReference{" +
                "component='" + component + '\'' +
                '}';
    }
}
