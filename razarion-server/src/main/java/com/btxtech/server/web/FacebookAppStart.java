package com.btxtech.server.web;

import com.btxtech.server.system.FilePropertiesService;
import com.btxtech.server.user.UserService;
import com.btxtech.server.util.facebook.FacebookSignedRequest;
import com.btxtech.server.util.facebook.FacebookUtil;
import com.btxtech.shared.system.ExceptionHandler;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 01.05.2017.
 */
@Named
@RequestScoped
public class FacebookAppStart {
    private static final String SIGNED_REQUEST_KEY = "signed_request";
    @Inject
    private HttpServletRequest httpServletRequest;
    @Inject
    private Logger logger;
    @Inject
    private FilePropertiesService filePropertiesService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private UserService userService;
    @Inject
    private PageTrackerBean pageTrackerBean;
    private String fbUserId;

    public String check(String page) {
        String[] values = httpServletRequest.getParameterMap().get(SIGNED_REQUEST_KEY);
        if (values == null || values.length == 0) {
            logger.warning("No signed_request for FacebookAppStart");
            pageTrackerBean.trackPage(page);
            // return GamePageBean.GAME_PAGE;
            return null;
        }

        try {
            FacebookSignedRequest facebookSignedRequest = FacebookUtil.createAndCheckFacebookSignedRequest(filePropertiesService.getFacebookSecret(), values[0]);
            if (facebookSignedRequest.hasUserId()) {
                // Is authorized by facebook
                // TODO userService.handleFacebookUserLogin(facebookSignedRequest.getUserId());
                pageTrackerBean.trackPage(page);
                // return GamePageBean.GAME_PAGE;
                return null;
            } else {
                // Is NOT authorized by facebook
                return null;
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            pageTrackerBean.trackPage(page);
            // return GamePageBean.GAME_PAGE;
            return null;
        }
    }

    public String getFacebookAppId() {
        return filePropertiesService.getFacebookAppId();
    }

//    public Object fbLoginResponse() {
//        if (StringUtils.isNoneEmpty(fbUserId)) {
//            userService.handleFacebookUserLogin(fbUserId);
//        } else {
//            userService.handleUnregisteredLogin();
//        }
////        try {
////            FacesContext.getCurrentInstance().getExternalContext().redirect(GamePageBean.GAME_PAGE);
////        } catch (IOException e) {
////            exceptionHandler.handleException(e);
////        }
//        return null;
//    }

    public void setFbUserId(String fbUserId) {
        this.fbUserId = fbUserId;
    }

    public String getFbUserId() {
        return fbUserId;
    }
}
