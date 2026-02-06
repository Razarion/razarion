package com.btxtech.uiservice.cockpit;

/**
 * Created by Beat
 * 05.07.2016.
 */

public interface ScreenCover {
    void showStoryCover(String html);

    void hideStoryCover();

    void removeLoadingCover();

    void fadeInLoadingCover();

    void onStartupProgress(double percent);
}
