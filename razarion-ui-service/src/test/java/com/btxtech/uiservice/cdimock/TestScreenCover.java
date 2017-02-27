package com.btxtech.uiservice.cdimock;

import com.btxtech.uiservice.cockpit.ScreenCover;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 24.01.2017.
 */
@ApplicationScoped
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
    public void fadeOutAndForward(String url) {
        throw new UnsupportedOperationException();
    }
}
