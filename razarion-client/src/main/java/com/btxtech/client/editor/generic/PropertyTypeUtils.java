package com.btxtech.client.editor.generic;

import com.btxtech.client.editor.generic.propertyeditors.DoubleEditor;
import com.btxtech.client.editor.generic.propertyeditors.EnumEditor;
import com.btxtech.client.editor.generic.propertyeditors.GenericPropertyEditor;
import com.btxtech.client.editor.generic.propertyeditors.GenericPropertyType;
import com.btxtech.client.editor.generic.propertyeditors.IntegerEditor;
import com.btxtech.client.editor.generic.propertyeditors.Rectangle2DEditor;
import com.btxtech.client.editor.generic.propertyeditors.RectangleEditor;
import com.btxtech.client.editor.generic.propertyeditors.StringEditor;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.Node;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.databinding.client.PropertyType;
import org.jboss.errai.ioc.client.container.IOC;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class PropertyTypeUtils {
    // private static Logger LOGGER = Logger.getLogger(PropertyTypeUtils.class.getName());
    private static final List<String> READ_ONLY_PROPERTIES = Collections.singletonList("id");
    private static final Collection<GenericPropertyType> GENERIC_TYPES = Arrays.asList(
            new GenericPropertyType<>(String.class, StringEditor.class),
            new GenericPropertyType<>(Integer.class, IntegerEditor.class),
            new GenericPropertyType<>(Double.class, DoubleEditor.class),
            new GenericPropertyType<>(Rectangle.class, RectangleEditor.class),
            new GenericPropertyType<>(Rectangle2D.class, Rectangle2DEditor.class),
            new GenericPropertyType<>(DecimalPosition.class, null),
            new GenericPropertyType<>(Index.class, null));

    private PropertyTypeUtils() {
    }

    public static boolean isGenericProperty(PropertyType propertyType) {
        return GENERIC_TYPES.stream().anyMatch(genericPropertyType -> genericPropertyType.getPropertyClass().equals(propertyType.getType()));
    }

    public static boolean isBindableProperty(PropertyType propertyType) {
        try {
            BindableProxyFactory.getBindableProxy(propertyType);
            return true;
        } catch (Throwable ignore) {
            return false;
        }
    }

    public static boolean isReadOnly(String propertyName) {
        return READ_ONLY_PROPERTIES.stream().anyMatch(s -> s.equalsIgnoreCase(propertyName));
    }

    public static Node createPropertyEditor(String propertyName, PropertyType propertyType, HasProperties hasProperties) {
        Class propertyClass = propertyType.getType();
        Class<? extends GenericPropertyEditor> propertyEditorClass;
        if (propertyType.getType().isEnum()) {
            propertyEditorClass = EnumEditor.class;
        } else {
            GenericPropertyType<? extends GenericPropertyEditor> genericPropertyType = GENERIC_TYPES.stream()
                    .filter(type -> type.getPropertyClass().equals(propertyClass))
                    .findFirst()
                    .orElse(null);
            if (genericPropertyType == null) {
                return setupUnknownInformation(propertyClass, "genericPropertyType == null");
            }
            propertyEditorClass = genericPropertyType.getGenericPropertyEditorClass();
            if (propertyEditorClass == null) {
                return setupUnknownInformation(propertyClass, "genericPropertyType.getGenericPropertyEditorClass() == null");
            }
        }

        GenericPropertyEditor bean = IOC.getBeanManager().lookupBean(propertyEditorClass).getInstance();
        bean.init(propertyName, propertyClass, hasProperties);
        return bean.getElement();
    }

    public static Node setupUnknownInformation(Class propertyClass, String additionalInfo) {
        HTMLDivElement divElement = (HTMLDivElement) DomGlobal.document.createElement("div");
        divElement.textContent = "No editor for <" + propertyClass + ">" + additionalInfo;
        return divElement;
    }
}

