package com.btxtech.webglemulator.razarion;

import com.btxtech.uiservice.DisplayService;

/**
 * Created by Beat
 * 06.07.2016.
 */
public class DevToolsDisplayService implements DisplayService {
    @Override
    public void setIntroText(String introText) {
        System.out.println("******** setIntroText: " + introText);
    }
}
