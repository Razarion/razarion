package com.btxtech.webglemulator.razarion;

import com.btxtech.uiservice.cockpit.ScreenCover;

/**
 * Created by Beat
 * 06.07.2016.
 */
public class DevToolsScreenCoverImpl implements ScreenCover {
    @Override
    public void showStoryCover(String html) {
        System.out.println("******** DevToolsScreenCoverImpl showStoryCover: " + html);
    }

    @Override
    public void hideStoryCover() {
        System.out.println("******** DevToolsScreenCoverImpl hideStoryCover");
    }

    @Override
    public void removeLoadingCover() {
        System.out.println("******** DevToolsScreenCoverImpl removeLoadingCover");
    }

    @Override
    public void fadeOutLoadingCover() {
        System.out.println("******** DevToolsScreenCoverImpl fadeOutLoadingCover");
    }
}
