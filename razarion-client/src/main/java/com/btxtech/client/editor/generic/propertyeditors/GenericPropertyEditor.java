package com.btxtech.client.editor.generic.propertyeditors;

import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.databinding.client.HasProperties;

public interface GenericPropertyEditor extends IsElement {
    void init(String propertyName, Class propertyClass, HasProperties hasProperties);
}
