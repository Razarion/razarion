package com.btxtech.facebookmarketing;

import com.facebook.ads.sdk.APIContext;
import com.facebook.ads.sdk.APIException;
import com.facebook.ads.sdk.APINodeList;
import com.facebook.ads.sdk.AdAccount;
import com.facebook.ads.sdk.AdImage;

/**
 * Created by Beat
 * 17.03.2017.
 */
public class FacebookAdMain {
    private static final String ACCESS_TOKEN = "";
    private static final String SECRET = "";
    private static final String MARKETING_ACCOUNT = "";

    public static void main(String[] args) {
        try {
            APIContext apiContext = new APIContext(ACCESS_TOKEN, SECRET).enableDebug(true);
            AdAccount adAccount = new AdAccount(MARKETING_ACCOUNT, apiContext);
            APINodeList<AdImage> adImageAPINodeList = adAccount.getAdImages().requestAccountIdField().execute();
            System.out.println("----------------------------");
            while (adImageAPINodeList != null) {
                for (AdImage adImage : adImageAPINodeList) {
                    System.out.println(adImage.getRawResponse());
                }
                adImageAPINodeList = adImageAPINodeList.nextPage();
            }
            System.out.println("----------------------------");
        } catch (APIException e) {
            e.printStackTrace();
        }

    }

}
