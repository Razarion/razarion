package com.btxtech.server.marketing;

import com.btxtech.server.marketing.facebook.AdInterest;
import com.btxtech.server.marketing.facebook.AdSetInsight;
import com.btxtech.server.marketing.facebook.CreationData;
import com.btxtech.server.marketing.facebook.FbAdImage;
import com.btxtech.server.marketing.facebook.FbFacade;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.utils.CollectionUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 19.03.2017.
 */

// If move to the web-server: remove all weld and Hibernate dependency and persistence.xml

@ApplicationScoped
public class MarketingService {
    private Logger logger = Logger.getLogger(MarketingService.class.getName());
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private FbFacade fbFacade;

    @Transactional
    @SecurityCheck
    public CreationData startCampaign(String title, String body, FbAdImage fbAdImage, List<Interest> interests) {
        CreationData creationData = fbFacade.createAd(title, body, fbAdImage, interests);

        CurrentAdEntity currentAdEntity = new CurrentAdEntity();
        currentAdEntity.setState(CurrentAdEntity.State.RUNNING);
        currentAdEntity.setIds(creationData);
        currentAdEntity.setDateStart(new Date());
        currentAdEntity.setTitle(title);
        currentAdEntity.setBody(body);
        currentAdEntity.setImageHash(fbAdImage.getHash());
        currentAdEntity.setInterests(interests);
        entityManager.persist(currentAdEntity);
        return creationData;
    }

    @Transactional
    @SecurityCheck
    public void stopCampaigns(long campaignId) {
        CurrentAdEntity currentAdEntity = getCurrentAdEntity(campaignId);
        if (currentAdEntity.getState() != CurrentAdEntity.State.RUNNING) {
            throw new IllegalStateException("Ad is not running: " + currentAdEntity.getState());
        }

        fbFacade.stopCampaign(campaignId);

        currentAdEntity.setDateStop(new Date());
        currentAdEntity.setState(CurrentAdEntity.State.WAITING_FOR_ARCHIVING);
        entityManager.merge(currentAdEntity);
    }

    @Transactional
    @SecurityCheck
    public void archiveCampaignAndHistorize(long campaignId) {
        CurrentAdEntity currentAdEntity = getCurrentAdEntity(campaignId);
        if (currentAdEntity.getState() != CurrentAdEntity.State.WAITING_FOR_ARCHIVING) {
            throw new IllegalStateException("Campaign is not in WAITING_FOR_ARCHIVING state: " + currentAdEntity.getState());
        }

        Collection<AdSetInsight> adSetInsights = fbFacade.getInsight(currentAdEntity.getAdSetId());
        AdSetInsight adSetInsight;
        if (adSetInsights.size() != 1) {
            adSetInsight = new AdSetInsight();
            logger.warning("More then one AdSetInsight received: " + adSetInsights.size() + " for Facebook AdSet: " + currentAdEntity.getAdSetId());
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
    public List<AdInterest> queryAdInterest(String query) {
        return fbFacade.queryAdInterest(query);
    }

    @Transactional
    public void onClickTrackerReceived(String adId) {
        ClickTrackerEntity clickTrackerEntity = new ClickTrackerEntity();
        clickTrackerEntity.setAdId(adId);
        clickTrackerEntity.setTimeStamp(new Date());
        entityManager.persist(clickTrackerEntity);
    }

    public List<FbAdImage> queryFbAdImages() {
        return fbFacade.queryFbAdImages();
    }

    public void deleteFbAdImage(FbAdImage image) {
        fbFacade.deleteFbAdImage(image);
    }

    public void uploadImageFile(byte[] uploadImageFile) {
        fbFacade.uploadImageFile(new String(Base64.getEncoder().encode(uploadImageFile)));
    }
}