package com.btxtech.client.editor.generic;

import jsinterop.annotations.JsType;
import jsinterop.base.Any;

@JsType
public class AngularTreeNodeData {
    public String name;
    public Any value;
    public boolean deleteAllowed;
    public boolean createAllowed;
}
