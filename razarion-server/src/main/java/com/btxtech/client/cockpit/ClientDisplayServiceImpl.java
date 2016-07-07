package com.btxtech.client.cockpit;

import com.btxtech.uiservice.DisplayService;

import javax.inject.Singleton;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Singleton
public class ClientDisplayServiceImpl implements DisplayService {
    private Logger logger = Logger.getLogger(ClientDisplayServiceImpl.class.getName());

    @Override
    public void setIntroText(String introText) {
        logger.severe("++++++++ClientDisplayServiceImpl.introText: " + introText);
    }
}
