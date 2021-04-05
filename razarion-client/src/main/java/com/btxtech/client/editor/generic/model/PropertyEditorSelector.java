package com.btxtech.client.editor.generic.model;

import jsinterop.base.Any;

import java.util.HashMap;
import java.util.Map;

public enum PropertyEditorSelector {
    STRING("string-property-editor") {
        @Override
        public Object convert(Any value) {
            return value.asString();
        }
    },
    INTEGER("integer-property-editor") {
        @Override
        public Object convert(Any value) {
            return value.asInt();
        }
    },
    DOUBLE("double-property-editor") {
        @Override
        public Object convert(Any value) {
            return value.asDouble();
        }
    },
    BOOLEAN("boolean-property-editor") {
        @Override
        public Object convert(Any value) {
            return value.asBoolean();
        }
    },
    RECTANGLE("rectangle-property-editor") {
        @Override
        public Object convert(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }
    },
    RECTANGLE_2D("rectangle-2d-property-editor") {
        @Override
        public Object convert(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }
    },
    DECIMAL_POSITION("decimal-position-property-editor") {
        @Override
        public Object convert(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }
    },
    INDEX("index-property-editor") {
        @Override
        public Object convert(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }
    },
    VERTEX("vertex-property-editor") {
        @Override
        public Object convert(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }
    },
    PLACE_CONFIG("place-config-property-editor") {
        @Override
        public Object convert(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }
    },
    ENUM("enum-property-editor") {
        @Override
        public Object convert(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }
    },
    INTEGER_MAP("integer-map-property-editor") {
        @Override
        public Object convert(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }
    },
    IMAGE("image-property-editor") {
        @Override
        public Object convert(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }
    },
    COLLADA_STRING("collada-string-property-editor") {
        @Override
        public Object convert(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }
    },
    I18N_STRING("i18n-string-property-editor") {
        @Override
        public Object convert(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }
    },
    POLYGON_2D("polygon-2d-property-editor") {
        @Override
        public Object convert(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }
    },
    UNKNOWN("UNKNOWN") {
        @Override
        public Object convert(Any value) {
            return value;
        }
    };

    private String selector;
    private static Map<String, PropertyEditorSelector> selectors = new HashMap<>();

    static {
        for (PropertyEditorSelector propertyEditorSelector : PropertyEditorSelector.values())
            selectors.put(propertyEditorSelector.selector, propertyEditorSelector);
    }

    PropertyEditorSelector(String selector) {
        this.selector = selector;
    }

    public String getSelector() {
        return selector;
    }

    public static PropertyEditorSelector fromSelector(String selector) {
        return selectors.get(selector);
    }

    public abstract Object convert(Any value);
}
