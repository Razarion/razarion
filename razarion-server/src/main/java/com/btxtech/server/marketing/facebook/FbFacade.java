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

    public CreationData createAd(List<Interest> interests) {
        try {
            APIContext context = getContext();
            AdAccount adAccount = getAdAccount(context);
            CreationData creationData = new CreationData();
            long campaignId = createCampaign(adAccount);
            creationData.setCampaignId(campaignId);
            long adSetId = createAddSet(context, adAccount, campaignId, interests);
            creationData.setAdSetId(adSetId);
            long adId = createAdd(adAccount, adSetId);
            creationData.setAdId(adId);
            setTrackingTag(adId, RestUrl.fbClickTrackingReceiver());
            return creationData;
        } catch (APIException e) {
            throw new RuntimeException(e);
        }
    }

    private long createCampaign(AdAccount account) {
        try {
            Campaign campaign = account.createCampaign()
                    .setName("Razarion Automated Campaign: " + DateUtil.formatDateTime(new Date()))
                    .setObjective(Campaign.EnumObjective.VALUE_CANVAS_APP_INSTALLS)
                    .setSpendCap(10000L) // Min value in Rappen
                    .setStatus(Campaign.EnumStatus.VALUE_PAUSED)
                    .execute();
            return Long.parseLong(campaign.getId());
        } catch (APIException e) {
            throw new RuntimeException(e);
        }
    }

    private long createAddSet(APIContext context, AdAccount account, long campaignId, Collection<Interest> interests) throws APIException {
        List<IDName> fbInterests = new ArrayList<>();
        for (Interest interest : interests) {
            fbInterests.add(new IDName().setFieldId(interest.getId()).setFieldName(interest.getName()));
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
        AdSet adSet = account.createAdSet()
                .setName("Automated Ad AdSet: " + DateUtil.formatDateTime(new Date()))
                .setCampaignId(campaign.getFieldId())
                .setStatus(AdSet.EnumStatus.VALUE_PAUSED)
                .setBillingEvent(AdSet.EnumBillingEvent.VALUE_IMPRESSIONS)
                .setIsAutobid(true)
                .setDailyBudget(200L)
                .setOptimizationGoal(AdSet.EnumOptimizationGoal.VALUE_APP_INSTALLS)
                .setPromotedObject("{application_id: " + filePropertiesService.getFacebookAppId() + ", object_store_url: \"https://apps.facebook.com/razarion\"}")
                .setTargeting(targeting)
                .setRedownload(true)
                // Does not work .setAdsetSchedule(Collections.singletonList(new DayPart().setFieldDays(Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L, 6L)).setFieldStartMinute(14L * 60).setFieldEndMinute(23L * 60 + 59)))
                .execute();
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

    private long createAdd(AdAccount account, long adSetId) throws APIException {
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
                                .setFieldMessage("Razarion vereint packende Echtzeit-Schlachten mit komplexer Strategie")
                                .setFieldImageHash("f889056506d773565829a57eff09e095")
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
                .setStatus(Ad.EnumStatus.VALUE_PAUSED)
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
}