package com.btxtech.server.marketing.facebook;

import com.btxtech.server.marketing.Interest;
import com.btxtech.server.system.FilePropertiesService;
import com.btxtech.server.util.DateUtil;
import com.btxtech.shared.rest.RestUrl;
import com.facebook.ads.sdk.APIContext;
import com.facebook.ads.sdk.APIException;
import com.facebook.ads.sdk.APINode;
import com.facebook.ads.sdk.APINodeList;
import com.facebook.ads.sdk.Ad;
import com.facebook.ads.sdk.AdAccount;
import com.facebook.ads.sdk.AdCreative;
import com.facebook.ads.sdk.AdCreativeLinkData;
import com.facebook.ads.sdk.AdCreativeLinkDataCallToAction;
import com.facebook.ads.sdk.AdCreativeLinkDataCallToActionValue;
import com.facebook.ads.sdk.AdCreativeObjectStorySpec;
import com.facebook.ads.sdk.AdImage;
import com.facebook.ads.sdk.AdSet;
import com.facebook.ads.sdk.AdsInsights;
import com.facebook.ads.sdk.Campaign;
import com.facebook.ads.sdk.FlexibleTargeting;
import com.facebook.ads.sdk.IDName;
import com.facebook.ads.sdk.Targeting;
import com.facebook.ads.sdk.TargetingGeoLocation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 17.03.2017.
 */
@ApplicationScoped
public class FbFacade {
    private static final String URL_PARAM_TRACK_KEY = "fbAdRazTrack";
    private Logger logger = Logger.getLogger(FbFacade.class.getName());
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    @Inject
    private FilePropertiesService filePropertiesService;

    private APIContext getContext() {
        return new APIContext(filePropertiesService.getFacebookAccessToken(), filePropertiesService.getFacebookSecret()).enableDebug(false);
    }

    private AdAccount getAdAccount(APIContext apiContext) {
        return new AdAccount(filePropertiesService.getFacebookMarketingAccount(), apiContext);
    }

    public CreationResult createAd(CreationInput creationInput) {
        try {
            APIContext context = getContext();
            AdAccount adAccount = getAdAccount(context);
            CreationResult creationResult = new CreationResult();
            long campaignId = createCampaign(adAccount);
            creationResult.setCampaignId(campaignId);
            long adSetId = createAddSet(context, adAccount, campaignId, creationInput);
            creationResult.setAdSetId(adSetId);
            long adId = createAdd(adAccount, adSetId, creationInput);
            creationResult.setAdId(adId);
            setTrackingTag(adId, RestUrl.fbClickTrackingReceiver());
            return creationResult;
        } catch (APIException e) {
            throw new RuntimeException(e);
        }
    }

    private long createCampaign(AdAccount account) {
        String name = "Razarion Automated Campaign: " + DateUtil.formatDateTime(new Date());
        if (filePropertiesService.isDeveloperMode()) {
            name += "_DEVELOPER_MODE";
        }
        try {
            Campaign campaign = account.createCampaign()
                    .setName(name)
                    .setObjective(Campaign.EnumObjective.VALUE_CANVAS_APP_INSTALLS)
                    .setSpendCap(10000L) // Min value in Rappen
                    .setStatus(Campaign.EnumStatus.VALUE_PAUSED)
                    .execute();
            return Long.parseLong(campaign.getId());
        } catch (APIException e) {
            throw new RuntimeException(e);
        }
    }

    private long createAddSet(APIContext context, AdAccount account, long campaignId, CreationInput creationInput) throws APIException {
        List<IDName> fbInterests = new ArrayList<>();
        for (Interest interest : creationInput.getInterests()) {
            fbInterests.add(new IDName().setFieldId(interest.getFbId()).setFieldName(interest.getName()));
        }

        Targeting targeting = new Targeting()
                .setFieldDevicePlatforms(Collections.singletonList(Targeting.EnumDevicePlatforms.VALUE_DESKTOP))
                .setFieldPublisherPlatforms(Collections.singletonList("facebook"))
                .setFieldFacebookPositions(Collections.singletonList("right_hand_column"))
                .setFieldExcludedConnections(Collections.singletonList(new IDName().setFieldId(filePropertiesService.getFacebookAppId()).setFieldName("Razarion")))
                .setFieldGeoLocations(new TargetingGeoLocation().setFieldCountries(Arrays.asList("CH", "AT", "DE")))
                .setFieldLocales(Collections.singletonList(5L))
                .setFieldFlexibleSpec(Collections.singletonList(new FlexibleTargeting().setFieldInterests(fbInterests)));
        Campaign campaign = new Campaign(campaignId, context).get().execute();
        AdAccount.APIRequestCreateAdSet apiRequestCreateAdSet = account.createAdSet()
                .setName("Automated Ad AdSet: " + DateUtil.formatDateTime(new Date()))
                .setCampaignId(campaign.getFieldId())
                .setStatus(AdSet.EnumStatus.VALUE_ACTIVE)
                .setBillingEvent(AdSet.EnumBillingEvent.VALUE_IMPRESSIONS)
                .setIsAutobid(true)
                // .setDailyBudget(200L)
                .setOptimizationGoal(AdSet.EnumOptimizationGoal.VALUE_APP_INSTALLS)
                .setPromotedObject("{application_id: " + filePropertiesService.getFacebookAppId() + ", object_store_url: \"https://apps.facebook.com/razarion\"}")
                .setTargeting(targeting)
                .setRedownload(true);
        if (creationInput.isLifeTime()) {
            if (creationInput.getLifeTimeBudget() == null) {
                throw new IllegalArgumentException("Life time budget is not set");
            }
            apiRequestCreateAdSet.setLifetimeBudget((long) (creationInput.getLifeTimeBudget() * 100));
            apiRequestCreateAdSet.setStartTime(DateUtil.toFacebookTimeString(creationInput.getScheduleStartTime()));
            apiRequestCreateAdSet.setEndTime(DateUtil.toFacebookTimeString(creationInput.getScheduleEndTime()));
        } else {
            if (creationInput.getDailyBudget() == null) {
                throw new IllegalArgumentException("Daily budget is not set");
            }
            apiRequestCreateAdSet.setDailyBudget((long) (creationInput.getDailyBudget() * 100));
        }


        AdSet adSet = apiRequestCreateAdSet.execute();
        return Long.parseLong(adSet.getId());
    }

    public void archiveCampaign(long campaignId) {
        try {
            new Campaign.APIRequestUpdate(Long.toString(campaignId), getContext()).setStatus(Campaign.EnumStatus.VALUE_ARCHIVED).execute();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void stopCampaign(long campaignId) {
        try {
            new Campaign.APIRequestUpdate(Long.toString(campaignId), getContext()).setStatus(Campaign.EnumStatus.VALUE_PAUSED).execute();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static String setupTagParam(String urlTagParam) {
        return URL_PARAM_TRACK_KEY + "=" + urlTagParam;
    }

    private long createAdd(AdAccount account, long adSetId, CreationInput creationInput) throws APIException {
        AdCreative creative = account.createAdCreative()
                .setObjectType("SHARE")
                .setTitle(creationInput.getTitle())
                .setBody(creationInput.getBody())
                .setUrlTags(setupTagParam(creationInput.getUrlTagParam()))
                .setObjectStorySpec(new AdCreativeObjectStorySpec().setFieldPageId(filePropertiesService.getFacebookAppPageId())
                        .setFieldLinkData(new AdCreativeLinkData()
                                .setFieldLink("https://apps.facebook.com/razarion/")
                                .setFieldMessage(creationInput.getBody())
                                .setFieldImageHash(creationInput.getFbAdImage().getHash())
                                .setFieldCallToAction(new AdCreativeLinkDataCallToAction()
                                        .setFieldType(AdCreativeLinkDataCallToAction.EnumType.VALUE_PLAY_GAME)
                                        .setFieldValue(new AdCreativeLinkDataCallToActionValue().setFieldApplication(filePropertiesService.getFacebookAppId())
                                                .setFieldLink("https://apps.facebook.com/razarion/")
                                                .setFieldLinkTitle("Razarion")
                                        )
                                )
                        )
                )
                .execute();
        Ad ad = account.createAd()
                .setName("Automated Ad: " + DateUtil.formatDateTime(new Date()))
                .setAdsetId(adSetId)
                .setCreative(creative)
                .setStatus(Ad.EnumStatus.VALUE_ACTIVE)
                .setRedownload(true)
                .execute();
        return Long.parseLong(ad.getId());

    }

    public Collection<AdSetInsight> getInsight(long adSetId) {
        try {
            AdSet adSet = new AdSet(adSetId, getContext());
            APINodeList<AdsInsights> adsInsightss = adSet.getInsights().setFields("date_start, date_stop, spend, clicks, impressions").execute();
            Collection<AdSetInsight> adSetInsightEntities = new ArrayList<>();
            while (adsInsightss != null) {
                for (AdsInsights adsInsights : adsInsightss) {
                    AdSetInsight historyAdEntity = new AdSetInsight();
                    historyAdEntity.setFacebookDateStart(DATE_FORMAT.parse(adsInsights.getFieldDateStart())); // May the time range of the insight query
                    historyAdEntity.setFacebookDateStop(DATE_FORMAT.parse(adsInsights.getFieldDateStop())); // May the time range of the insight query
                    historyAdEntity.setClicks(Integer.parseInt(adsInsights.getFieldClicks()));
                    historyAdEntity.setImpressions(Integer.parseInt(adsInsights.getFieldImpressions()));
                    historyAdEntity.setSpent(Double.parseDouble(adsInsights.getFieldSpend()));
                    adSetInsightEntities.add(historyAdEntity);
                }
                adsInsightss = adsInsightss.nextPage();
            }
            return adSetInsightEntities;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public String getCurrentCampaignsString() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            APINodeList<Campaign> campaigns = getAdAccount(getContext()).getCampaigns().requestAllFields().execute();
            while (campaigns != null) {
                for (Campaign campaign : campaigns) {
                    append("Campaign", campaign, stringBuilder);
                    APINodeList<AdSet> adSets = campaign.getAdSets().requestAllFields().execute();
                    while (adSets != null) {
                        for (AdSet adSet : adSets) {
                            append("AdSet", adSet, stringBuilder);
                            APINodeList<Ad> ads = adSet.getAds().requestAllFields().execute();
                            while (ads != null) {
                                for (Ad ad : ads) {
                                    append("Ad", ad, stringBuilder);
                                    APINodeList<AdCreative> adCreatives = ad.getAdCreatives().requestAllFields().execute();
                                    while (adCreatives != null) {
                                        for (AdCreative adCreative : adCreatives) {
                                            append("AdCreative", adCreative, stringBuilder);
                                        }
                                        adCreatives = adCreatives.nextPage();
                                    }
                                }
                                ads = ads.nextPage();
                            }
                        }
                        adSets = adSets.nextPage();
                    }
                }
                campaigns = campaigns.nextPage();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        return stringBuilder.toString();
    }

    private void append(String message, APINode apiNode, StringBuilder stringBuilder) {
        stringBuilder.append(message).append(": ").append(apiNode.getRawResponse()).append("<br /><br />");
    }

    public List<AdInterest> queryAdInterest(String query) {
        Client client = ClientBuilder.newClient();
        // client.register(new LoggingFilter());
        TargetingAdInterestData data = client.target("https://graph.facebook.com/v2.8/search").queryParam("access_token", filePropertiesService.getFacebookAccessToken()).queryParam("type", "adinterest").queryParam("q", query).request(MediaType.APPLICATION_JSON).get(TargetingAdInterestData.class);
        return data.getData();
    }

    private void setTrackingTag(long addId, String url) {
        Client client = ClientBuilder.newClient();
        // client.register(new LoggingFilter());
        String fields = "{\"access_token\": \"" + filePropertiesService.getFacebookAccessToken() + "\", \"url\": \"" + url + "\", \"add_template_param\": \"1\"}";
        logger.severe("setTrackingTag: " + url);
        client.target("https://graph.facebook.com/v2.8").path(Long.toString(addId)).path("trackingtag").request(MediaType.APPLICATION_JSON).post(Entity.entity(fields, MediaType.APPLICATION_JSON_TYPE));
    }

    public List<FbAdImage> queryFbAdImages() {
        try {
            APINodeList<AdImage> adImages = getAdAccount(getContext()).getAdImages().requestAllFields().execute();
            List<FbAdImage> fbAdImages = new ArrayList<>();
            while (adImages != null) {
                for (AdImage adImage : adImages) {
                    fbAdImages.add(new FbAdImage(adImage));
                }
                adImages = adImages.nextPage();
            }
            return fbAdImages;
        } catch (APIException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteFbAdImage(FbAdImage image) {
        try {
            getAdAccount(getContext()).deleteAdImages().setHash(image.getHash()).execute();
        } catch (APIException e) {
            throw new RuntimeException(e);
        }
    }

    public void uploadImageFile(String base64File) {
        try {
            getAdAccount(getContext()).createAdImage().setParam("bytes", base64File).execute();
        } catch (APIException e) {
            throw new RuntimeException(e);
        }
    }
}
