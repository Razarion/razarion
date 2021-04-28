package com.btxtech.client.editor.generic.model;

import com.btxtech.shared.datatypes.DecimalPosition;
import jsinterop.base.Any;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import jsinterop.base.JsPropertyMapOfAny;

import java.util.HashMap;
import java.util.Map;

public enum PropertyEditorSelector {
    STRING("string-property-editor") {
        @Override
        public Object convertFromAngular(Any value) {
            return value.asString();
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);
        }
    },
    INTEGER("integer-property-editor") {
        @Override
        public Object convertFromAngular(Any value) {
            return value.asInt();
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);
        }
    },
    DOUBLE("double-property-editor") {
        @Override
        public Object convertFromAngular(Any value) {
            return value.asDouble();
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);
        }
    },
    BOOLEAN("boolean-property-editor") {
        @Override
        public Object convertFromAngular(Any value) {
            return value.asBoolean();
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);
        }
    },
    RECTANGLE("rectangle-property-editor") {
        @Override
        public Object convertFromAngular(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);  // TODO
        }
    },
    RECTANGLE_2D("rectangle-2d-property-editor") {
        @Override
        public Object convertFromAngular(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);  // TODO
        }
    },
    DECIMAL_POSITION("decimal-position-property-editor") {
        @Override
        public Object convertFromAngular(Any value) {
            JsPropertyMapOfAny mapOfAny = value.asPropertyMap();
            return new DecimalPosition(mapOfAny.getAny("x").asDouble(), mapOfAny.getAny("y").asDouble());
        }

        @Override
        public Any convertToAngular(Object object) {
            return Js.cast(JsPropertyMap.of("x", ((DecimalPosition)object).getX(),"y", ((DecimalPosition)object).getY()));
        }
    },
    INDEX("index-property-editor") {
        @Override
        public Object convertFromAngular(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);  // TODO
        }
    },
    VERTEX("vertex-property-editor") {
        @Override
        public Object convertFromAngular(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);  // TODO
        }
    },
    PLACE_CONFIG("place-config-property-editor") {
        @Override
        public Object convertFromAngular(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);  // TODO
        }
    },
    ENUM("enum-property-editor") {
        @Override
        public Object convertFromAngular(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);  // TODO
        }
    },
    INTEGER_MAP("integer-map-property-editor") {
        @Override
        public Object convertFromAngular(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);  // TODO
        }
    },
    IMAGE("image-property-editor") {
        @Override
        public Object convertFromAngular(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);  // TODO
        }
    },
    COLLADA_STRING("collada-string-property-editor") {
        @Override
        public Object convertFromAngular(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);  // TODO
        }
    },
    I18N_STRING("i18n-string-property-editor") {
        @Override
        public Object convertFromAngular(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);  // TODO
        }
    },
    POLYGON_2D("polygon-2d-property-editor") {
        @Override
        public Object convertFromAngular(Any value) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);  // TODO
        }
    },
    UNKNOWN("UNKNOWN") {
        @Override
        public Object convertFromAngular(Any value) {
            return value;
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);
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

    public abstract Object convertFromAngular(Any value);

    public abstract Any convertToAngular(Object object);
}
