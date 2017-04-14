package com.btxtech.facebookmarketing;

import com.facebook.ads.sdk.APIContext;
import com.facebook.ads.sdk.APINodeList;
import com.facebook.ads.sdk.AdAccount;
import com.facebook.ads.sdk.AdImage;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by Beat
 * 17.03.2017.
 */
public class FacebookAdMain {
    private static final String ACCESS_TOKEN;
    private static final String SECRET;
    private static final String MARKETING_ACCOUNT;

    public static void main(String[] args) {
        try {
            System.out.println("----------INIT------------------");
            APIContext apiContext = new APIContext(ACCESS_TOKEN, SECRET).enableDebug(true);
            AdAccount adAccount = new AdAccount(MARKETING_ACCOUNT, apiContext);
            System.out.println("----------------------------");

            APINodeList<AdImage> adImages = adAccount.getAdImages().requestAllFields().execute();
            while (adImages != null) {
                for (AdImage adImage : adImages) {
                    System.out.println("adImage: URL:" + adImage.getFieldUrl() + " " + adImage.getFieldUrl128() + "" + adImage);
                }
                adImages = adImages.nextPage();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    static {
        try {
            File file = new File(System.getProperty("user.home"), "razarion.properties");
            System.out.println("Reading property from: " + file);
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            SECRET = properties.getProperty("facebook.secret");
            ACCESS_TOKEN = properties.getProperty("facebook.access_token");
            MARKETING_ACCOUNT = properties.getProperty("facebook.marketing_account_id");
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }


}
