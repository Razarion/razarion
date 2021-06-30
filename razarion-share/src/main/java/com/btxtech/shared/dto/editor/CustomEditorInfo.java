package com.btxtech.shared.dto.editor;

public class CustomEditorInfo {
    private String javaParentPropertyClass;
    private String javaPropertyName;
    private CustomEditorType type;

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

    public CustomEditorType getType() {
        return type;
    }

    public void setType(CustomEditorType type) {
        this.type = type;
    }

    public CustomEditorInfo javaParentPropertyClass(String javaParentPropertyClass) {
        setJavaParentPropertyClass(javaParentPropertyClass);
        return this;
    }

    public CustomEditorInfo javaPropertyName(String javaPropertyName) {
        setJavaPropertyName(javaPropertyName);
        return this;
    }

    public CustomEditorInfo type(CustomEditorType type) {
        setType(type);
        return this;
    }
}
