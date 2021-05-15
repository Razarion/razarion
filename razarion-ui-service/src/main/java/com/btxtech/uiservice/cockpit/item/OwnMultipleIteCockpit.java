package com.btxtech.uiservice.cockpit.item;

import jsinterop.annotations.JsType;

@JsType
public abstract class OwnMultipleIteCockpit {
    public OwnItemCockpit ownItemCockpit;
    public int count;
    public String tooltip;

    @SuppressWarnings("unused") // Called by Angular
    public abstract void onSelect();
}
