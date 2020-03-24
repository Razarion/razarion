package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;

import java.util.HashMap;
import java.util.Map;

public class PropertyEditorClassFactory {
    public static Map<Class, Class<? extends AbstractPropertyEditor>> GENERIC_TYPES = new HashMap<>();

    static {
        GENERIC_TYPES.put(String.class, StringEditor.class);
        GENERIC_TYPES.put(Integer.class, IntegerEditor.class);
        GENERIC_TYPES.put(Double.class, DoubleEditor.class);
        GENERIC_TYPES.put(Boolean.class, BooleanEditor.class);
        GENERIC_TYPES.put(Rectangle.class, RectangleEditor.class);
        GENERIC_TYPES.put(Rectangle2D.class, Rectangle2DEditor.class);
        GENERIC_TYPES.put(DecimalPosition.class, null); // TODO
        GENERIC_TYPES.put(Index.class, null);// TODO
    }

    public static Class get(Class type) {
        return GENERIC_TYPES.get(type);
    }


}
