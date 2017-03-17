package com.btxtech.server.web;

import com.btxtech.servercommon.FilePropertiesService;

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
public class MainPageBean implements Serializable {
    @Inject
    private FilePropertiesService filePropertiesService;
    @Inject
    private Session session;

    public String getFacebookAppId() {
        return filePropertiesService.getFacebookAppId();
    }

    public String getLanguage() {
        return session.getLocale().toString();
    }
}
