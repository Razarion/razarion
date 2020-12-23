package com.btxtech.shared.dto.editor;

public class OpenApi3Schema {
    private String javaParentPropertyClass;
    private String javaPropertyName;
    private String type;

    public String getJavaParentPropertyClass() {
        return javaParentPropertyClass;
    }

    public void setJavaParentPropertyClass(String javaParentPropertyClass) {
        this.javaParentPropertyClass = javaParentPropertyClass;
    }

    public String getJavaPropertyName() {
        return javaPropertyName;
    }

    public void setJavaPropertyName(String javaPropertyName) {
        this.javaPropertyName = javaPropertyName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public OpenApi3Schema javaParentPropertyClass(String javaParentPropertyClass) {
        setJavaParentPropertyClass(javaParentPropertyClass);
        return this;
    }

    public OpenApi3Schema javaPropertyName(String javaPropertyName) {
        setJavaPropertyName(javaPropertyName);
        return this;
    }

    public OpenApi3Schema type(String type) {
        setType(type);
        return this;
    }
}
