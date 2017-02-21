package com.btxtech.uiservice.user;

import com.btxtech.shared.dto.FacebookUserLoginInfo;

import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 21.02.2017.
 */
@ApplicationScoped
public class UserUiService {
    private static final String FACEBOOK_STATUS_CONNECTED = "connected";
    private Logger logger = Logger.getLogger(UserUiService.class.getName());
    private FacebookUserLoginInfo facebookUserLoginInfo;

    public void facebookLoginState(String status, String accessToken, Integer expiresIn, String signedRequest, String userId) {
        if (FACEBOOK_STATUS_CONNECTED.equalsIgnoreCase(status)) {
            facebookUserLoginInfo = new FacebookUserLoginInfo();
            facebookUserLoginInfo.setAccessToken(accessToken).setExpiresIn(expiresIn).setSignedRequest(signedRequest).setUserId(userId);
        } else {
            facebookUserLoginInfo = null;
        }
    }

    public void facebookLoginState(String status) {
        facebookUserLoginInfo = null;
        logger.warning("Facebook login unclear: " + status);
    }

    public void facebookLoginStateFailed() {
        facebookUserLoginInfo = null;
    }

    public FacebookUserLoginInfo getFacebookUserLoginInfo() {
        return facebookUserLoginInfo;
    }
}
