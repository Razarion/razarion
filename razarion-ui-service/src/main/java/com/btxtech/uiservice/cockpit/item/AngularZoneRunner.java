package com.btxtech.uiservice.cockpit.item;

import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface AngularZoneRunner {
    void runInAngularZone(BuildupItemCockpit.AngularZoneCallback callback);
}
