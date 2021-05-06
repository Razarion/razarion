package com.btxtech.client.editor.generic;

import jsinterop.annotations.JsType;
import jsinterop.base.Any;

import java.util.logging.Logger;

@JsType
public class AngularTreeNodeData {
    private static final Logger LOGGER = Logger.getLogger(AngularTreeNodeData.class.getName());
    public String name;
    public Any value;
    public String[] options;
    public String propertyEditorSelector;
    public boolean nullable;
    public boolean deleteAllowed;
    public boolean createAllowed;
    public boolean canHaveChildren;

    @SuppressWarnings("unused") // Called by Angular
    public void onCreate(GwtAngularPropertyTable gwtAngularPropertyTable) {
        LOGGER.severe("onCreate");
    }

    @SuppressWarnings("unused") // Called by Angular
    public void onDelete(GwtAngularPropertyTable gwtAngularPropertyTable) {
        LOGGER.severe("onDelete");
    }

    @SuppressWarnings("unused") // Called by Angular
    public void setValue(Any value) {
        LOGGER.severe("setValue: " + value);
    }

}
