package com.btxtech.client.gwtangular;

import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface Callback {
    @SuppressWarnings("unused") // Called by Angular
    void onCallback();
}
