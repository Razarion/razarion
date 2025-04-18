package com.btxtech.uiservice.cockpit;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 05.07.2016.
 */

@JsType(isNative = true)
public interface ScreenCover {
    void showStoryCover(String html);

    void hideStoryCover();

    void removeLoadingCover();

    void fadeInLoadingCover();

    void onStartupProgress(double percent);
}
