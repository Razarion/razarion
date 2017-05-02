package com.btxtech.server.web;

import com.btxtech.server.user.UserService;
import com.btxtech.shared.system.ExceptionHandler;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

/**
 * Created by Beat
 * 02.05.2017.
 */
@Named
@RequestScoped
public class HomePageBean {
    @Inject
    private UserService userService;
    @Inject
    private ExceptionHandler exceptionHandler;
    private String fbUserId;

    public Object fbLoginResponse() {
        if (StringUtils.isNoneEmpty(fbUserId)) {
            userService.handleFacebookUserLogin(fbUserId);
        } else {
            userService.handleUnregisteredLogin();
        }
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(GamePageBean.GAME_PAGE);
        } catch (IOException e) {
            exceptionHandler.handleException(e);
        }
        return null;
    }

    public void setFbUserId(String fbUserId) {
        this.fbUserId = fbUserId;
    }

    public String getFbUserId() {
        return fbUserId;
    }
}
