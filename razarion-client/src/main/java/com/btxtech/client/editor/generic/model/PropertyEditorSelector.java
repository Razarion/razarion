package com.btxtech.client.editor.generic.model;

public enum PropertyEditorSelector {
    STRING("string-property-editor"),
    INTEGER("integer-property-editor"),
    DOUBLE("double-property-editor"),
    BOOLEAN("boolean-property-editor"),
    RECTANGLE("rectangle-property-editor"),
    RECTANGLE_2D("rectangle-2d-property-editor"),
    DECIMAL_POSITION("decimal-position-property-editor"),
    INDEX("index-property-editor"),
    VERTEX("vertex-property-editor"),
    PLACE_CONFIG("place-config-property-editor"),
    ENUM("enum-property-editor"),
    INTEGER_MAP("integer-map-property-editor"),
    IMAGE("image-property-editor"),
    COLLADA_STRING("collada-string-property-editor"),
    I18N_STRING("i18n-string-property-editor"),
    POLYGON_2D("polygon-2d-property-editor"),
    UNKNOWN("UNKNOWN");

    private String selector;

    PropertyEditorSelector(String selector) {
        this.selector = selector;
    }

    public String getSelector() {
        return selector;
    }
}
