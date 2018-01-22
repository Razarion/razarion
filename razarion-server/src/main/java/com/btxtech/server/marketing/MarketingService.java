package com.btxtech.server.marketing;

import com.btxtech.server.marketing.facebook.AdInterest;
import com.btxtech.server.marketing.facebook.AdSetInsight;
import com.btxtech.server.marketing.facebook.CreationInput;
import com.btxtech.server.marketing.facebook.CreationResult;
import com.btxtech.server.marketing.facebook.FbAdImage;
import com.btxtech.server.marketing.facebook.FbFacade;
import com.btxtech.server.marketing.restdatatypes.CampaignJson;
import com.btxtech.server.marketing.restdatatypes.ClicksPerHourJson;
import com.btxtech.server.persistence.tracker.PageTrackerEntity;
import com.btxtech.server.persistence.tracker.PageTrackerEntity_;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.server.util.DateUtil;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.utils.CollectionUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 19.03.2017.
 */

// If move to the web-server: remove all weld and Hibernate dependency and persistence.xml

@ApplicationScoped
public class MarketingService {
    private static final long FB_AD_IMAGE_CACHE_PERIOD = 2 * 60 * 60 * 1000;
    private Logger logger = Logger.getLogger(MarketingService.class.getName());
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private FbFacade fbFacade;
    @Inject
    private ExceptionHandler exceptionHandler;
    private List<FbAdImage> fbAdImageCache;
    private long lastFbAdImageCacheUpdate;

    @Transactional
    @SecurityCheck
    public CreationResult startCampaign(CreationInput creationInput) {
        CreationResult creationResult = fbFacade.createAd(creationInput);

        CurrentAdEntity currentAdEntity = new CurrentAdEntity();
        currentAdEntity.setState(AdState.RUNNING);
        currentAdEntity.setIds(creationResult);
        currentAdEntity.setDateStart(new Date());
        currentAdEntity.setCreationInput(creationInput);
        currentAdEntity.setFacebookPositions("feed");
        entityManager.persist(currentAdEntity);
        return creationResult;
    }

    @Transactional
    @SecurityCheck
    public void stopCampaigns(long campaignId) {
        CurrentAdEntity currentAdEntity = getCurrentAdEntity(campaignId);
        if (currentAdEntity.getState() != AdState.RUNNING) {
            throw new IllegalStateException("Ad is not running: " + currentAdEntity.getState());
        }

        fbFacade.stopCampaign(campaignId);

        currentAdEntity.setDateStop(new Date());
        currentAdEntity.setState(AdState.WAITING_FOR_ARCHIVING);
        entityManager.merge(currentAdEntity);
    }

    @Transactional
    @SecurityCheck
    public void archiveCampaignAndHistorize(long campaignId) {
        CurrentAdEntity currentAdEntity = getCurrentAdEntity(campaignId);
        if (currentAdEntity.getState() != AdState.WAITING_FOR_ARCHIVING) {
            throw new IllegalStateException("Campaign is not in WAITING_FOR_ARCHIVING state: " + currentAdEntity.getState());
        }

        Collection<AdSetInsight> adSetInsights = fbFacade.getInsight(currentAdEntity.getAdSetId());
        AdSetInsight adSetInsight;
        if (adSetInsights.size() != 1) {
            adSetInsight = new AdSetInsight();
            logger.warning("adSetInsights.size() != 1: AdSetInsight received: " + adSetInsights.size() + " for Facebook AdSet: " + currentAdEntity.getAdSetId());
        } else {
            adSetInsight = CollectionUtils.getFirst(adSetInsights);
        }

        HistoryAdEntity historyAdEntity = new HistoryAdEntity();
        historyAdEntity.fill(currentAdEntity, adSetInsight);
        entityManager.persist(historyAdEntity);

        fbFacade.archiveCampaign(campaignId);
        entityManager.remove(currentAdEntity);
    }

    private CurrentAdEntity getCurrentAdEntity(long campaignId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<CurrentAdEntity> userQuery = criteriaBuilder.createQuery(CurrentAdEntity.class);
        Root<CurrentAdEntity> from = userQuery.from(CurrentAdEntity.class);
        CriteriaQuery<CurrentAdEntity> userSelect = userQuery.select(from);
        userSelect.where(criteriaBuilder.equal(from.get(CurrentAdEntity_.campaignId), campaignId));
        return entityManager.createQuery(userQuery).getSingleResult();
    }

    @SecurityCheck
    public void archiveCampaign(long campaignId) {
        fbFacade.archiveCampaign(campaignId);
    }

    @SecurityCheck
    public String getCurrentCampaignsString() {
        return fbFacade.getCurrentCampaignsString();
    }

    @SecurityCheck
    public String getAdInsight(long adSetId) {
        StringBuilder stringBuilder = new StringBuilder();
        Collection<AdSetInsight> adSetInsights = fbFacade.getInsight(adSetId);
        if (adSetInsights.isEmpty()) {
            return "No insights";
        } else {
            for (AdSetInsight adSetInsight : adSetInsights) {
                stringBuilder.append(adSetInsight);
                stringBuilder.append("<br />");
            }
            return stringBuilder.toString();
        }
    }

    @SecurityCheck
    public List<DetailedAdInterest> queryAdInterest(String query) {
        List<AdInterest> adInterests = fbFacade.queryAdInterest(query);
        List<DetailedAdInterest> result = new ArrayList<>();
        for (AdInterest adInterest : adInterests) {
            result.add(new DetailedAdInterest().setAdInterest(adInterest).setUsedCurrentDates(getCurrentAdEntity4Interest(adInterest)).setUsedHistoryDates(getHistoryAdEntity4Interest(adInterest)));
        }
        return result;
    }

    private List<Date> getCurrentAdEntity4Interest(AdInterest adInterest) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> userQuery = criteriaBuilder.createTupleQuery();
        Root<CurrentAdEntity> from = userQuery.from(CurrentAdEntity.class);
        userQuery.multiselect(from.get(CurrentAdEntity_.dateStart));
        // Interest_.id -> Interest_.fbId: Join always takes attribute type from CurrentAdEntity if it is called id
        userQuery.where(criteriaBuilder.equal(from.join(CurrentAdEntity_.interests).get(Interest_.fbId), adInterest.getId()));
        List<Date> dates = new ArrayList<>();
        for (Tuple tuple : entityManager.createQuery(userQuery).getResultList()) {
            dates.add((Date) tuple.get(0));
        }
        return dates;
    }

    private List<Date> getHistoryAdEntity4Interest(AdInterest adInterest) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> userQuery = criteriaBuilder.createTupleQuery();
        Root<HistoryAdEntity> from = userQuery.from(HistoryAdEntity.class);
        userQuery.multiselect(from.get(HistoryAdEntity_.dateStart));
        // Interest_.id -> Interest_.fbId: Join always takes attribute type from HistoryAdEntity if it is called id
        userQuery.where(criteriaBuilder.equal(from.join(HistoryAdEntity_.interests).get(Interest_.fbId), adInterest.getId()));
        List<Date> dates = new ArrayList<>();
        for (Tuple tuple : entityManager.createQuery(userQuery).getResultList()) {
            dates.add((Date) tuple.get(0));
        }
        return dates;
    }

    @Transactional
    public void onClickTrackerReceived(String adId) {
        ClickTrackerEntity clickTrackerEntity = new ClickTrackerEntity();
        clickTrackerEntity.setAdId(adId);
        clickTrackerEntity.setTimeStamp(new Date());
        entityManager.persist(clickTrackerEntity);
    }

    @SecurityCheck
    public void updateFbAdImageCache() {
        try {
            fbAdImageCache = fbFacade.queryFbAdImages();
            lastFbAdImageCacheUpdate = System.currentTimeMillis();
            logger.severe("TODO remove me::: fbAdImageCache updated"); // TODO remove me
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @SecurityCheck
    public List<FbAdImage> getFbAdImages() {
        if (fbAdImageCache == null) {
            updateFbAdImageCache();
        }
        if (lastFbAdImageCacheUpdate + FB_AD_IMAGE_CACHE_PERIOD < System.currentTimeMillis()) {
            updateFbAdImageCache();
        }
        return fbAdImageCache;
    }


    @SecurityCheck
    public void deleteFbAdImage(FbAdImage image) {
        fbFacade.deleteFbAdImage(image);
    }

    @SecurityCheck
    public void uploadImageFile(byte[] uploadImageFile) {
        fbFacade.uploadImageFile(new String(Base64.getEncoder().encode(uploadImageFile)));
    }

    @Transactional
    @SecurityCheck
    public List<CampaignJson> getCampaignHistory() {
        Map<String, FbAdImage> hashUrlMap = getFbAdImageHashes();
        List<CampaignJson> campaignJsons = new ArrayList<>();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<HistoryAdEntity> userQuery = criteriaBuilder.createQuery(HistoryAdEntity.class);
        Root<HistoryAdEntity> root = userQuery.from(HistoryAdEntity.class);
        CriteriaQuery<HistoryAdEntity> userSelect = userQuery.select(root);
        for (HistoryAdEntity historyAdEntity : entityManager.createQuery(userSelect).getResultList()) {
            CampaignJson campaignJson = historyAdEntity.createCampaignJson();
            campaignJson.setClicksPerHour(getClicksPerHour(campaignJson));
            FbAdImage fbAdImage = hashUrlMap.get(historyAdEntity.getImageHash());
            if (fbAdImage != null) {
                campaignJson.setImageUrl(fbAdImage.getUrl()).setImageUrl128(fbAdImage.getUrl128());
            }
            campaignJsons.add(campaignJson);
        }
        return campaignJsons;
    }

    private List<ClicksPerHourJson> getClicksPerHour(CampaignJson campaignJson) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PageTrackerEntity> userQuery = criteriaBuilder.createQuery(PageTrackerEntity.class);
        Root<PageTrackerEntity> root = userQuery.from(PageTrackerEntity.class);
        CriteriaQuery<PageTrackerEntity> userSelect = userQuery.select(root);
        userSelect.where(criteriaBuilder.like(root.get(PageTrackerEntity_.params), "%" + FbFacade.setupTagParam(campaignJson.getUrlTagParam()) + "%"));
        Map<Date, Integer> hourlyClicks = new HashMap<>();
        for (PageTrackerEntity pageTrackerEntity : entityManager.createQuery(userSelect).getResultList()) {
            Date hourDate = DateUtil.hourStart(pageTrackerEntity.getTimeStamp());
            Integer clicks = hourlyClicks.get(hourDate);
            if (clicks == null) {
                clicks = 0;
            }
            hourlyClicks.put(hourDate, clicks + 1);
        }

        Date campaignStart = DateUtil.hourStart(campaignJson.getDateStart());
        if (!hourlyClicks.containsKey(campaignStart)) {
            hourlyClicks.put(campaignStart, 0);
        }

        List<ClicksPerHourJson> clicksPerHourJsons = new ArrayList<>();
        for (Map.Entry<Date, Integer> entry : hourlyClicks.entrySet()) {
            clicksPerHourJsons.add(new ClicksPerHourJson().setDate(entry.getKey()).setClicks(entry.getValue()));
        }
        return clicksPerHourJsons;
    }

    private Map<String, FbAdImage> getFbAdImageHashes() {
        try {
            List<FbAdImage> fbAdImages = getFbAdImages();
            Map<String, FbAdImage> hashUrlMap = new HashMap<>();
            for (FbAdImage fbAdImage : fbAdImages) {
                hashUrlMap.put(fbAdImage.getHash(), fbAdImage);
            }
            return hashUrlMap;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            return Collections.emptyMap();
        }
    }

    @Transactional
    @SecurityCheck
    public List<ActiveAdInfo> getActiveAdInfos() {
        Map<String, FbAdImage> hashUrlMap = getFbAdImageHashes();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<CurrentAdEntity> userQuery = criteriaBuilder.createQuery(CurrentAdEntity.class);
        Root<CurrentAdEntity> root = userQuery.from(CurrentAdEntity.class);
        CriteriaQuery<CurrentAdEntity> userSelect = userQuery.select(root);
        List<ActiveAdInfo> activeAdInfos = new ArrayList<>();
        entityManager.createQuery(userSelect).getResultList().forEach(currentAdEntity -> {
            ActiveAdInfo activeAdInfo = new ActiveAdInfo().setCampaignId(currentAdEntity.getCampaignId()).setAdSetId(currentAdEntity.getAdSetId()).setAdId(currentAdEntity.getAdId());
            activeAdInfo.setAdState(currentAdEntity.getState()).setTitle(currentAdEntity.getTitle()).setBody(currentAdEntity.getBody()).setScheduledDateStart(currentAdEntity.getScheduleTimeStart());
            activeAdInfo.setScheduledDateEnd(currentAdEntity.getScheduleTimeEnd());
            FbAdImage fbAdImage = hashUrlMap.get(currentAdEntity.getImageHash());
            if (fbAdImage != null) {
                activeAdInfo.setUrl128(fbAdImage.getUrl128());
            }
            if (currentAdEntity.getInterests() != null) {
                activeAdInfo.setAdInterestJsons(currentAdEntity.getInterests().stream().map(Interest::generateAdInterestJson).collect(Collectors.toList()));
            }
            activeAdInfos.add(activeAdInfo);
        });
        return activeAdInfos;
    }

//  DB MIGRATION CAN BE REMOVED
////    ad_id=6065557113021           291514 0001
////    ad_id=6065806661021 22.3 23.3 295848 0002
////    ad_id=6066198224421 28.3 29.3 301297 0003
////    ad_id=6067562012621 14.4 15.4 302169 0004
////    ad_id=6067648817421 16.4 17.4        0005
//
//    private List<ClicksPerHourJson> _getClicksPerHour(CampaignJson campaignJson) {
//        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//        CriteriaQuery<PageTrackerEntity> userQuery = criteriaBuilder.createQuery(PageTrackerEntity.class);
//        Root<PageTrackerEntity> root = userQuery.from(PageTrackerEntity.class);
//        CriteriaQuery<PageTrackerEntity> userSelect = userQuery.select(root);
//        // Predicate ge = criteriaBuilder.greaterThanOrEqualTo(root.get(PageTrackerEntity_.timeStamp), campaignJson.getDateStart());
//        // Predicate le = criteriaBuilder.lessThanOrEqualTo(root.get(PageTrackerEntity_.timeStamp), campaignJson.getDateStop());
//        // userSelect.where(criteriaBuilder.and(le, ge));
//        // userSelect.where(criteriaBuilder.like(root.get(PageTrackerEntity_.params), "%ad_id=" + campaignJson.getAdId() + "%"));
//
//        Set<String> ids = new HashSet<>();
//        Map<Date, Integer> hourlyClicks = new HashMap<>();
//        System.out.println(campaignJson.getAdId() + "-------------------------------------------------------------");
//        for (PageTrackerEntity pageTrackerEntity : entityManager.createQuery(userSelect).getResultList()) {
//            String params = pageTrackerEntity.getParams();
//            if (params != null && !params.trim().isEmpty()) {
//                int index = params.indexOf("ad_id=");
//                if (index < 0) {
//                    continue;
//                }
//                int end = params.indexOf("||", index);
//                ids.add(params.substring(index, end));
//            }
//        }
//        for (String id : ids) {
//            System.out.println(id);
//        }
//
//        return null;
//    }

}