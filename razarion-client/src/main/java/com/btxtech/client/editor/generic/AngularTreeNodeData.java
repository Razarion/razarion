package com.btxtech.client.editor.generic;

import jsinterop.annotations.JsType;
import jsinterop.base.Any;

@JsType(isNative = true, namespace = "com.btxtech.shared.json")
public class AngularTreeNodeData {
    public String name;
    public Any value;
}
