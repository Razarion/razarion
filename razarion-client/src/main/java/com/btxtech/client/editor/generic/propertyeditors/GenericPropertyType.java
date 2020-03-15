package com.btxtech.client.editor.generic.propertyeditors;

public class GenericPropertyType<T extends GenericPropertyEditor> {
    private Class propertyClass;
    private Class<T> genericPropertyEditorClass;

    public GenericPropertyType(Class propertyClass, Class<T> genericPropertyEditorClass) {
        this.propertyClass = propertyClass;
        this.genericPropertyEditorClass = genericPropertyEditorClass;
    }

    public Class getPropertyClass() {
        return propertyClass;
    }

    public Class<T> getGenericPropertyEditorClass() {
        return genericPropertyEditorClass;
    }
}
