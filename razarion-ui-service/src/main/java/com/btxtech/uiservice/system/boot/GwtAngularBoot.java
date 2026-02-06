package com.btxtech.uiservice.system.boot;

import java.util.function.Consumer;

public interface GwtAngularBoot {
    void loadThreeJsModels(Runnable onSuccess, Consumer<String> onError);
}
