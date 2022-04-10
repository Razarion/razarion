package com.btxtech.uiservice.system.boot;

import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;
import elemental2.promise.Promise;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface GwtAngularBoot {
    Promise<Void> loadThreeJsModels(ThreeJsModelConfig[] threeJsModelConfigs);
}
