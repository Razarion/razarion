package com.btxtech.facebookad.facade;

import com.btxtech.servercommon.FilePropertiesService;
import com.facebook.ads.sdk.APIContext;
import com.facebook.ads.sdk.APIException;
import com.facebook.ads.sdk.Ad;
import com.facebook.ads.sdk.AdAccount;
import com.facebook.ads.sdk.AdCreative;
import com.facebook.ads.sdk.AdCreativeLinkData;
import com.facebook.ads.sdk.AdCreativeLinkDataCallToAction;
import com.facebook.ads.sdk.AdCreativeLinkDataCallToActionValue;
import com.facebook.ads.sdk.AdCreativeObjectStorySpec;
import com.facebook.ads.sdk.AdSet;
import com.facebook.ads.sdk.Campaign;
import com.facebook.ads.sdk.FlexibleTargeting;
import com.facebook.ads.sdk.IDName;
import com.facebook.ads.sdk.Targeting;
import com.facebook.ads.sdk.TargetingGeoLocation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Beat
 * 17.03.2017.
 */
@ApplicationScoped
public class FbFacade {
    private static final long CAMPAIGN_ID = 6065518817021L;
    @Inject
    private FilePropertiesService filePropertiesService;


    private APIContext getContext() {
        return new APIContext(filePropertiesService.getFacebookAccessToken(), filePropertiesService.getFacebookSecret()).enableDebug(false);
    }

    private AdAccount getAdAccount(APIContext apiContext) {
        return new AdAccount(filePropertiesService.getFacebookMarketingAccount(), apiContext);
    }

    public long createCampaign() {
        try {
            Campaign campaign = getAdAccount(getContext()).createCampaign()
                    .setName("Razarion Automated Campaign")
                    .setObjective(Campaign.EnumObjective.VALUE_CANVAS_APP_INSTALLS)
                    .setSpendCap(10000L) // Min value in Rappen
                    .setStatus(Campaign.EnumStatus.VALUE_PAUSED)
                    .execute();
            return Long.parseLong(campaign.getId());
        } catch (APIException e) {
            throw new RuntimeException(e);
        }
    }

    public long getCampaignId() {
        return CAMPAIGN_ID;
    }


    public void createAd() {
        try {
            APIContext context = getContext();
            AdAccount adAccount = getAdAccount(context);
            long adSetId = createAddSet(context, adAccount, getCampaignId());
            createAdd(adAccount, adSetId);
        } catch (APIException e) {
            throw new RuntimeException(e);
        }
    }

    private long createAddSet(APIContext context, AdAccount account, long campaignId) throws APIException {
        Targeting targeting = new Targeting()
                .setFieldDevicePlatforms(Collections.singletonList(Targeting.EnumDevicePlatforms.VALUE_DESKTOP))
                .setFieldPublisherPlatforms(Collections.singletonList("facebook"))
                .setFieldFacebookPositions(Collections.singletonList("right_hand_column"))
                .setFieldExcludedConnections(Collections.singletonList(new IDName().setFieldId(filePropertiesService.getFacebookAppId()).setFieldName("Razarion")))
                .setFieldGeoLocations(new TargetingGeoLocation().setFieldCountries(Arrays.asList("CH", "AT", "DE")))
                .setFieldLocales(Collections.singletonList(5L))
                .setFieldFlexibleSpec(Collections.singletonList(new FlexibleTargeting().setFieldInterests(Arrays.asList(
                        new IDName().setFieldId("6003057392644").setFieldName("Gaming"),
                        new IDName().setFieldId("6003253267911").setFieldName("Command & Conquer"),
                        new IDName().setFieldId("6003066189670").setFieldName("Trump"),
                        new IDName().setFieldId("6003582500438").setFieldName("Strategy games")))));
        Campaign campaign = new Campaign(campaignId, context).get().execute();
        AdSet adSet = account.createAdSet()
                .setName("Automated Ad AdSet")
                .setCampaignId(campaign.getFieldId())
                .setStatus(AdSet.EnumStatus.VALUE_PAUSED)
                .setBillingEvent(AdSet.EnumBillingEvent.VALUE_IMPRESSIONS)
                .setIsAutobid(true)
                .setDailyBudget(200L)
                .setOptimizationGoal(AdSet.EnumOptimizationGoal.VALUE_APP_INSTALLS)
                .setPromotedObject("{application_id: " + filePropertiesService.getFacebookAppId() + ", object_store_url: \"https://apps.facebook.com/razarion\"}")
                .setTargeting(targeting)
                .setRedownload(true)
                .execute();
        System.out.println("Ad set created. Id: " + adSet.getId());
        return Long.parseLong(adSet.getId());
    }

    private void deleteAddSet(APIContext context, long adSetId) throws APIException {
        new AdSet.APIRequestDelete(Long.toString(adSetId), context).execute();
    }


    private void createAdd(AdAccount account, long adSetId) throws APIException {
//        AdImage image = account.createAdImage()
//                .addUploadFile("file", new File(IMAGE_DIR, "TestAdImage.jpg"))
//                .execute();
        AdCreative creative = account.createAdCreative()
                .setObjectType("SHARE")
                .setTitle("Echtzeit Strategiespiel")
                .setBody("Razarion vereint packende Echtzeit-Schlachten mit komplexer Strategie und Multiplayerspaß")
                .setObjectStorySpec(new AdCreativeObjectStorySpec().setFieldPageId(filePropertiesService.getFacebookAppPageId())
                        .setFieldLinkData(new AdCreativeLinkData()
                                .setFieldLink("https://apps.facebook.com/razarion/")
                                .setFieldMessage("Razarion ist eine wahre Rarität im Bereich der Browsergames.")
                                .setFieldImageHash("6d95067d9a6d30c3b6341eb59b5d2782")
                                .setFieldCallToAction(new AdCreativeLinkDataCallToAction()
                                        .setFieldType(AdCreativeLinkDataCallToAction.EnumType.VALUE_PLAY_GAME)
                                        .setFieldValue(new AdCreativeLinkDataCallToActionValue().setFieldApplication(filePropertiesService.getFacebookAppId())
                                                .setFieldLink("https://apps.facebook.com/razarion/")
                                                .setFieldLinkTitle("Razarion")
                                        )
                                )
                        )
                )
                // TODO .setImageHash(image.getFieldHash())
                .setImageHash("6d95067d9a6d30c3b6341eb59b5d2782")
                .execute();
        Ad ad = account.createAd()
                .setName("Automated Ad")
                .setAdsetId(adSetId)
                .setCreative(creative)
                .setStatus(Ad.EnumStatus.VALUE_PAUSED)
                .setRedownload(true)
                .execute();
    }

}
