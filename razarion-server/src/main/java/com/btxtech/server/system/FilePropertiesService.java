package com.btxtech.server.system;

import com.btxtech.shared.system.ExceptionHandler;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 14.03.2017.
 */
@ApplicationScoped
public class FilePropertiesService {
    private static final String PROPERTY_FILE_NAME = "razarion.properties";
    private static final String FACEBOOK_APP_ID = "facebook.appid";
    private Logger logger = Logger.getLogger(FilePropertiesService.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    private Properties properties;

    @PostConstruct
    public void postConstruct() {
        try {
            File file = new File(System.getProperty("user.home"), PROPERTY_FILE_NAME);
            logger.warning("Reading property from: " + file);
            Properties tmpProperties = new Properties();
            tmpProperties.load(new FileInputStream(file));
            properties = tmpProperties;
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
    }

    public String getFacebookAppId() {
        return getPropertyThrows(FACEBOOK_APP_ID);
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