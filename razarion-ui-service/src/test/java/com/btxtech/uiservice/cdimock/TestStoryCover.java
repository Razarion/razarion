package com.btxtech.uiservice.cdimock;

import com.btxtech.uiservice.cockpit.StoryCover;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 24.01.2017.
 */
@ApplicationScoped
public class TestStoryCover implements StoryCover {
    @Override
    public void show(String html) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void hide() {
        throw new UnsupportedOperationException();
    }
}
