package com.btxtech.webglemulator.razarion;

import com.btxtech.uiservice.cockpit.StoryCover;

/**
 * Created by Beat
 * 06.07.2016.
 */
public class DevToolsDisplayService implements StoryCover {
    @Override
    public void show(String html) {
        System.out.println("******** DevToolsDisplayService show: " + html);
    }

    @Override
    public void hide() {
        System.out.println("******** DevToolsDisplayService hide");
    }
}
