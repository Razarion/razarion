package com.btxtech.server.web;

import com.btxtech.server.system.FilePropertiesService;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

/**
 * Created by Beat
 * 14.03.2017.
 */
@Named
@SessionScoped
public class GamePageBean implements Serializable {
    public static final String GAME_PAGE = "game.xhtml";
    @Inject
    private FilePropertiesService filePropertiesService;
    @Inject
    private SessionHolder sessionHolder;

    public String getFacebookAppId() {
        return filePropertiesService.getFacebookAppId();
    }

    public String getLanguage() {
        return sessionHolder.getPlayerSession().getLocale().toString();
    }
}
