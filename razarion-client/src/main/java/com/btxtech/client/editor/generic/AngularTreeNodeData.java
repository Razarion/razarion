package com.btxtech.client.editor.generic;

import jsinterop.annotations.JsType;
import jsinterop.base.Any;

import java.util.logging.Logger;

@JsType
public class AngularTreeNodeData {
    private static final Logger LOGGER = Logger.getLogger(AngularTreeNodeData.class.getName());
    public String name;
    public Any value;
    public String propertyEditorSelector;
    public boolean nullable;
    public boolean deleteAllowed;
    public boolean createAllowed;

    public void onCreate(GwtAngularPropertyTable gwtAngularPropertyTable) {
        LOGGER.severe("onCreate");
    }

    public void onDelete(GwtAngularPropertyTable gwtAngularPropertyTable) {
        LOGGER.severe("onDelete");
    }

}
