package com.btxtech.uiservice.cockpit;

/**
 * Created by Beat
 * 05.07.2016.
 */
public interface ScreenCover {
    long FADE_DURATION = 2000; // Edit in razarion.css

    void showStoryCover(String html);

    void hideStoryCover();

    void removeLoadingCover();

    void fadeOutLoadingCover();

    void fadeOutAndForward(String url);

    void fadeInLoadingCover();
}
