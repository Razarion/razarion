package com.btxtech.shared.dto.editor;

public class CollectionReferenceInfo {
    private String javaParentPropertyClass;
    private String javaPropertyName;
    private CollectionReferenceType type;

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

    public CollectionReferenceType getType() {
        return type;
    }

    public void setType(CollectionReferenceType type) {
        this.type = type;
    }

    public CollectionReferenceInfo javaParentPropertyClass(String javaParentPropertyClass) {
        setJavaParentPropertyClass(javaParentPropertyClass);
        return this;
    }

    public CollectionReferenceInfo javaPropertyName(String javaPropertyName) {
        setJavaPropertyName(javaPropertyName);
        return this;
    }

    public CollectionReferenceInfo type(CollectionReferenceType type) {
        setType(type);
        return this;
    }
}
