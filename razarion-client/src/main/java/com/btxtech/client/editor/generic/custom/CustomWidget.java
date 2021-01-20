package com.btxtech.client.editor.generic.custom;


import org.jboss.errai.ui.client.local.api.elemental2.IsElement;

public interface CustomWidget<T> extends IsElement {
    void setRootPropertyValue(T rootPropertyValue);
}
