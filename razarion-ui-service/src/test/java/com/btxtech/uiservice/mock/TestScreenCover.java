package com.btxtech.uiservice.mock;

import com.btxtech.uiservice.cockpit.ScreenCover;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Created by Beat
 * 24.01.2017.
 */
@Singleton
public class TestScreenCover implements ScreenCover {

    @Inject
    public TestScreenCover() {
    }

    @Override
    public void showStoryCover(String html) {
    }

    @Override
    public void hideStoryCover() {
    }

    @Override
    public void removeLoadingCover() {
    }

    @Override
    public void fadeInLoadingCover() {
    }

    @Override
    public void onStartupProgress(double percent) {
    }
}
