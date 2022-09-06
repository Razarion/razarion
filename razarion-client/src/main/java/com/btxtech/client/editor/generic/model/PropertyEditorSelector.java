package com.btxtech.client.editor.generic.model;

import com.btxtech.shared.dto.editor.CollectionReferenceType;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import jsinterop.base.Any;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import jsinterop.base.JsPropertyMapOfAny;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public enum PropertyEditorSelector {
    STRING("string-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            return value.asString();
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);
        }
    },
    INTEGER("integer-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            return value.asInt();
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);
        }
    },
    DOUBLE("double-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            return value.asDouble();
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);
        }
    },
    BOOLEAN("boolean-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            return value.asBoolean();
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);
        }
    },
    RECTANGLE("rectangle-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            JsPropertyMapOfAny mapOfAny = value.asPropertyMap();
            return new Rectangle((int) mapOfAny.get("x"),
                    (int) mapOfAny.get("y"),
                    (int) mapOfAny.get("w"),
                    (int) mapOfAny.get("h"));
        }

        @Override
        public Any convertToAngular(Object object) {
            Rectangle rectangle = (Rectangle) object;
            JsPropertyMapOfAny jsPropertyMap = JsPropertyMap.of("x", rectangle.startX(),"y", rectangle.startY());
            jsPropertyMap.set("w", rectangle.width());
            jsPropertyMap.set("h", rectangle.height());
            return Js.cast(jsPropertyMap);
        }
    },
    RECTANGLE_2D("rectangle-2d-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            JsPropertyMapOfAny mapOfAny = value.asPropertyMap();
            return new Rectangle2D((double) mapOfAny.get("x"),
                    (double) mapOfAny.get("y"),
                    (double) mapOfAny.get("w"),
                    (double) mapOfAny.get("h"));
        }

        @Override
        public Any convertToAngular(Object object) {
            Rectangle2D rectangle2D = (Rectangle2D) object;
            JsPropertyMapOfAny jsPropertyMap = JsPropertyMap.of("x", rectangle2D.startX(),"y", rectangle2D.startY());
            jsPropertyMap.set("w", rectangle2D.width());
            jsPropertyMap.set("h", rectangle2D.height());
            return Js.cast(jsPropertyMap);
        }
    },
    DECIMAL_POSITION("decimal-position-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            JsPropertyMapOfAny mapOfAny = value.asPropertyMap();
            return new DecimalPosition((double) mapOfAny.get("x"), (double) mapOfAny.get("y"));
        }

        @Override
        public Any convertToAngular(Object object) {
            return Js.cast(JsPropertyMap.of("x", ((DecimalPosition)object).getX(),"y", ((DecimalPosition)object).getY()));
        }
    },
    INDEX("index-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            JsPropertyMapOfAny mapOfAny = value.asPropertyMap();
            return new Index((int) mapOfAny.get("x"), (int) mapOfAny.get("y"));
        }

        @Override
        public Any convertToAngular(Object object) {
            return Js.cast(JsPropertyMap.of("x", ((Index) object).getX(), "y", ((Index) object).getY()));
        }
    },
    VERTEX("vertex-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            JsPropertyMapOfAny mapOfAny = value.asPropertyMap();
            return new Vertex((double) mapOfAny.get("x"), (double) mapOfAny.get("y"), (double) mapOfAny.get("z"));
        }

        @Override
        public Any convertToAngular(Object object) {
            return Js.cast(JsPropertyMap.of("x", ((Vertex) object).getX(),
                    "y", ((Vertex) object).getY(),
                    "z", ((Vertex) object).getZ()));
        }
    },
    PLACE_CONFIG("place-config-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            JsPropertyMapOfAny mapOfAny = value.asPropertyMap();
            if (mapOfAny.has("x")) {
                return new PlaceConfig()
                        .position(new DecimalPosition((double) mapOfAny.get("x"), (double) mapOfAny.get("y")))
                        .radius((Double) mapOfAny.get("r"));
            } else if (mapOfAny.has("p")) {
                return new PlaceConfig().polygon2D((Polygon2D) mapOfAny.get("p"));
            } else {
                return null;
            }
        }

        @Override
        public Any convertToAngular(Object object) {
            PlaceConfig placeConfig = (PlaceConfig) object;
            if (placeConfig.getPosition() != null) {
                return Js.cast(JsPropertyMap.of("x", placeConfig.getPosition().getX(),
                        "y", placeConfig.getPosition().getY(),
                        "r", placeConfig.getRadius()));
            } else {
                return Js.cast(JsPropertyMap.of("p", Any.of(placeConfig.getPolygon2D())));
            }
        }
    },
    ENUM("enum-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            return Arrays.stream(propertyClass.getEnumConstants())
                    .filter(e -> ((Enum<?>) e).name().equals(value.asString()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Enum member " + value.asString()+" + not found in: " + propertyClass));
        }

        @Override
        public Any convertToAngular(Object object) {
            return Js.cast(((Enum<?>) object).name());
        }

        @Override
        public String[] angularOptions(Class<?> propertyClass) {
            return Arrays.stream(propertyClass.getEnumConstants())
                    .map(e -> ((Enum<?>) e).name())
                    .sorted(Comparator.comparing(Object::toString))
                    .toArray(String[]::new);
        }
    },
    INTEGER_MAP("integer-map-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            Map<Integer, Integer> resultMap = new HashMap<>();
            elemental2.core.Map<Any, Any> jsMap = Js.cast(value);
            jsMap.forEach((numberValue, numberKey, ignoreMap) -> {
                resultMap.put(numberKey.asInt(), numberValue.asInt());
                return null;
            });
            return resultMap;
        }

        @Override
        public Any convertToAngular(Object object) {
            @SuppressWarnings("ALL")
            Map<Integer, Integer> map = (Map<Integer, Integer>) object;
            elemental2.core.Map<Any, Any> jsMap = new elemental2.core.Map<>();
            map.forEach((key, value) -> jsMap.set(Any.of(key.intValue()), Any.of(value.intValue())));
            return Any.of(jsMap);
        }
    },
    IMAGE_REFERENCE("collection-reference-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            return value.asInt();
        }

        @Override
        public Any convertToAngular(Object object) {
            return convertCollectionReferenceToAngular(CollectionReferenceType.IMAGE, object);
        }

        @Override
        public Any convertNullToAngular() {
            return convertNullCollectionReferenceToAngular(CollectionReferenceType.IMAGE);
        }
    },
    BASE_ITEM_REFERENCE("collection-reference-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            return value.asInt();
        }

        @Override
        public Any convertToAngular(Object object) {
            return convertCollectionReferenceToAngular(CollectionReferenceType.BASE_ITEM, object);
        }

        @Override
        public Any convertNullToAngular() {
            return convertNullCollectionReferenceToAngular(CollectionReferenceType.BASE_ITEM);
        }
    },
    RESOURCE_ITEM_REFERENCE("collection-reference-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            return value.asInt();
        }

        @Override
        public Any convertToAngular(Object object) {
            return convertCollectionReferenceToAngular(CollectionReferenceType.RESOURCE_ITEM, object);
        }

        @Override
        public Any convertNullToAngular() {
            return convertNullCollectionReferenceToAngular(CollectionReferenceType.RESOURCE_ITEM);
        }
    },
    SHAPE_3D_REFERENCE("collection-reference-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            return value.asInt();
        }

        @Override
        public Any convertToAngular(Object object) {
            return convertCollectionReferenceToAngular(CollectionReferenceType.SHAPE_3D, object);
        }

        @Override
        public Any convertNullToAngular() {
            return convertNullCollectionReferenceToAngular(CollectionReferenceType.SHAPE_3D);
        }
    },
    MESH_CONTAINER("collection-reference-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            return value.asInt();
        }

        @Override
        public Any convertToAngular(Object object) {
            return convertCollectionReferenceToAngular(CollectionReferenceType.MESH_CONTAINER, object);
        }

        @Override
        public Any convertNullToAngular() {
            return convertNullCollectionReferenceToAngular(CollectionReferenceType.MESH_CONTAINER);
        }
    },
    THREE_JS_MODEL_REFERENCE("collection-reference-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            return value.asInt();
        }

        @Override
        public Any convertToAngular(Object object) {
            return convertCollectionReferenceToAngular(CollectionReferenceType.THREE_JS_MODEL, object);
        }

        @Override
        public Any convertNullToAngular() {
            return convertNullCollectionReferenceToAngular(CollectionReferenceType.THREE_JS_MODEL);
        }
    },
    THREE_JS_MODEL_PACK_REFERENCE("collection-reference-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            return value.asInt();
        }

        @Override
        public Any convertToAngular(Object object) {
            return convertCollectionReferenceToAngular(CollectionReferenceType.THREE_JS_MODEL_PACK, object);
        }

        @Override
        public Any convertNullToAngular() {
            return convertNullCollectionReferenceToAngular(CollectionReferenceType.THREE_JS_MODEL_PACK);
        }
    },
    WATER_REFERENCE("collection-reference-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            return value.asInt();
        }

        @Override
        public Any convertToAngular(Object object) {
            return convertCollectionReferenceToAngular(CollectionReferenceType.WATER, object);
        }

        @Override
        public Any convertNullToAngular() {
            return convertNullCollectionReferenceToAngular(CollectionReferenceType.WATER);
        }
    },
    GROUND_REFERENCE("collection-reference-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            return value.asInt();
        }

        @Override
        public Any convertToAngular(Object object) {
            return convertCollectionReferenceToAngular(CollectionReferenceType.GROUND, object);
        }

        @Override
        public Any convertNullToAngular() {
            return convertNullCollectionReferenceToAngular(CollectionReferenceType.GROUND);
        }
    },
    COLLADA_STRING("collada-string-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            return value.asString();
        }

        @Override
        public Any convertToAngular(Object object) {
            return null;
        }
    },
    I18N_STRING("i18n-string-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            throw new UnsupportedOperationException("...TODO..."); // TODO
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);  // TODO
        }
    },
    POLYGON_2D("polygon-2d-property-editor") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            return value;
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);
        }
    },
    UNKNOWN("UNKNOWN") {
        @Override
        public Object convertFromAngular(Any value, Class<?> propertyClass) {
            return value;
        }

        @Override
        public Any convertToAngular(Object object) {
            return Any.of(object);
        }
    };

    private static Any convertCollectionReferenceToAngular(CollectionReferenceType type, Object object) {
        return Js.cast(JsPropertyMap.of("collection", type.getCollectionName(), "value", object));
    }

    private static Any convertNullCollectionReferenceToAngular(CollectionReferenceType type) {
        return Js.cast(JsPropertyMap.of("collection", type.getCollectionName()));
    }

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

    public abstract Object convertFromAngular(Any value, Class<?> propertyClass);

    public abstract Any convertToAngular(Object object);

    public Any convertNullToAngular() {
        return null;
    }

    public String[] angularOptions(Class<?> propertyClass) {
        return null;
    }
}
