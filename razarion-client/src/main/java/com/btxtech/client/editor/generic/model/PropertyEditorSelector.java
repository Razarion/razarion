package com.btxtech.client.editor.generic.model;

public enum PropertyEditorSelector {
    STRING("string-property-editor"),
    INTEGER("string-property-editor"),
    DOUBLE("string-property-editor"),
    BOOLEAN("string-property-editor"),
    RECTANGLE("string-property-editor"),
    RECTANGLE_2D("string-property-editor"),
    DECIMAL_POSITION("string-property-editor"),
    INDEX("string-property-editor"),
    VERTEX("string-property-editor"),
    PLACE_CONFIG("string-property-editor"),
    ENUM("string-property-editor"),
    INTEGER_MAP("string-property-editor"),
    IMAGE("string-property-editor"),
    COLLADA_STRING("string-property-editor"),
    UNKNOWN("UNKNOWN");

    private String selector;

    PropertyEditorSelector(String selector) {
        this.selector = selector;
    }

    public String getSelector() {
        return selector;
    }
}
