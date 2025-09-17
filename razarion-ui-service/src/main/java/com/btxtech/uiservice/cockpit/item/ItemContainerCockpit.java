package com.btxtech.uiservice.cockpit.item;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

@JsType
public abstract class ItemContainerCockpit {
    @JsIgnore
    public AngularZoneRunner angularZoneRunner;
    public int count;

    @SuppressWarnings("unused") // Called by Angular
    public abstract void onUnload();

    @SuppressWarnings("unused") // Called by Angular
    public abstract void setAngularZoneRunner(AngularZoneRunner angularZoneRunner);

}
