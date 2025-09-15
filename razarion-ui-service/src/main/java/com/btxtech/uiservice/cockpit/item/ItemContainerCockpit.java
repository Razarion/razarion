package com.btxtech.uiservice.cockpit.item;

import jsinterop.annotations.JsType;

@JsType
public abstract class ItemContainerCockpit {
    public int count;

    public abstract void onUnload();
}
