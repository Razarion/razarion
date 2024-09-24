package com.btxtech.uiservice.cdimock;

import com.btxtech.uiservice.cockpit.ScreenCover;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 24.01.2017.
 */
@Singleton
public class TestScreenCover implements ScreenCover {
    @Override
    public void showStoryCover(String html) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void hideStoryCover() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeLoadingCover() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fadeOutLoadingCover() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fadeInLoadingCover() {
        throw new UnsupportedOperationException();
    }
}
