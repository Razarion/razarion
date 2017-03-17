package com.btxtech.servercommon;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 14.03.2017.
 */
@ApplicationScoped
public class FilePropertiesService {
    private static final String PROPERTY_FILE_NAME = "razarion.properties";
    private static final String FACEBOOK_APP_ID = "facebook.app_id";
    private static final String FACEBOOK_APP_PAGE_ID = "facebook.app_page_id";
    private static final String FACEBOOK_SECRET = "facebook.secret";
    private static final String FACEBOOK_ACCESS_TOKEN = "facebook.access_token";
    private static final String FACEBOOK_MARKETING_ACCOUNT_ID = "facebook.marketing_account_id";
    private static final String DEVELOPER_MODE = "system.dev-mode";
    private Logger logger = Logger.getLogger(FilePropertiesService.class.getName());
    //    @Inject
//    private ExceptionHandler exceptionHandler; // Use exception handler id the share module is spilt in share and game engine
    private Properties properties;
    private boolean developerMode;

    @PostConstruct
    public void postConstruct() {
        try {
            File file = new File(System.getProperty("user.home"), PROPERTY_FILE_NAME);
            logger.warning("Reading property from: " + file);
            Properties tmpProperties = new Properties();
            tmpProperties.load(new FileInputStream(file));
            properties = tmpProperties;
            developerMode = Boolean.parseBoolean(properties.getProperty(DEVELOPER_MODE));
            if (developerMode) {
                logger.warning("Running in developer mode");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            // exceptionHandler.handleException(e);
        }
    }

    public String getFacebookAppId() {
        return getPropertyThrows(FACEBOOK_APP_ID);
    }

    public String getFacebookSecret() {
        return getPropertyThrows(FACEBOOK_SECRET);
    }

    public String getFacebookAccessToken() {
        return getPropertyThrows(FACEBOOK_ACCESS_TOKEN);
    }

    public String getFacebookMarketingAccount() {
        return getPropertyThrows(FACEBOOK_MARKETING_ACCOUNT_ID);
    }

    public String getFacebookAppPageId() {
        return getPropertyThrows(FACEBOOK_APP_PAGE_ID);
    }

    public boolean isDeveloperMode() {
        return developerMode;
    }

    private String getPropertyThrows(String key) {
        if (properties == null) {
            throw new IllegalStateException("Properties is not initialized");
        }
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("No property found for: " + key);
        }
        return value;
    }
}