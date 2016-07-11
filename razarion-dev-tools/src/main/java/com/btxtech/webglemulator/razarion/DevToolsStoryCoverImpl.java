package com.btxtech.webglemulator.razarion;

import com.btxtech.uiservice.cockpit.StoryCover;

/**
 * Created by Beat
 * 06.07.2016.
 */
public class DevToolsStoryCoverImpl implements StoryCover {
    @Override
    public void show(String html) {
        System.out.println("******** DevToolsStoryCoverImpl show: " + html);
    }

    @Override
    public void hide() {
        System.out.println("******** DevToolsStoryCoverImpl hide");
    }
}
