package com.btxtech.server.marketing;

import com.btxtech.server.marketing.facebook.AdSetInsight;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * 19.03.2017.
 */

// If move to the web-server: remove all weld and Hibernate dependency and persistence.xml

@ApplicationScoped
public class MarketingService {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private FbFacade fbFacade;

    @Transactional
    @SecurityCheck
    public void startAd() {
        List<Interest> interests = new ArrayList<>();
        interests.add(new Interest().setId("6003057392644").setName("Gaming"));
        interests.add(new Interest().setId("6003253267911").setName("Command & Conquer"));
        interests.add(new Interest().setId("6003066189670").setName("Trump"));
        interests.add(new Interest().setId("6003582500438").setName("Strategy games"));

        long adSetId = fbFacade.createAd(interests);

        CurrentAdEntity currentAdEntity = new CurrentAdEntity();
        currentAdEntity.setState(CurrentAdEntity.State.RUNNING);
        currentAdEntity.setAdSetId(adSetId);
        currentAdEntity.setDateStart(new Date());
        entityManager.persist(currentAdEntity);
    }

    @Transactional
    @SecurityCheck
    public void stopAd(long adSetId) {
        fbFacade.stopAddSet(adSetId);

        CurrentAdEntity currentAdEntity = getCurrentAdEntity(adSetId);
        currentAdEntity.setDateStop(new Date());
        currentAdEntity.setState(CurrentAdEntity.State.WAITING_FOR_DELETION);
        entityManager.merge(currentAdEntity);
    }

    @Transactional
    @SecurityCheck
    public void deleteAdAndHistorize(long adSetId) {
        CurrentAdEntity currentAdEntity = getCurrentAdEntity(adSetId);
        if (currentAdEntity.getState() != CurrentAdEntity.State.WAITING_FOR_DELETION) {
            throw new IllegalStateException("Can not delete running ad with id: " + currentAdEntity.getState());
        }

        Collection<AdSetInsight> adSetInsights = fbFacade.getInsight(adSetId);
        if (adSetInsights.size() != 1) {
            throw new IllegalStateException("More then one AdSetInsight received: " + adSetInsights.size() + " for Facebook AdSet: " + adSetId);
        }

        HistoryAdEntity historyAdEntity = new HistoryAdEntity();
        historyAdEntity.fill(currentAdEntity, CollectionUtils.getFirst(adSetInsights));
        entityManager.persist(historyAdEntity);

        fbFacade.deleteAddSet(adSetId);
        entityManager.remove(currentAdEntity);
    }

    private CurrentAdEntity getCurrentAdEntity(long adSetId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<CurrentAdEntity> userQuery = criteriaBuilder.createQuery(CurrentAdEntity.class);
        Root<CurrentAdEntity> from = userQuery.from(CurrentAdEntity.class);
        CriteriaQuery<CurrentAdEntity> userSelect = userQuery.select(from);
        userSelect.where(criteriaBuilder.equal(from.get(CurrentAdEntity_.adSetId), adSetId));
        return entityManager.createQuery(userQuery).getSingleResult();
    }

    @SecurityCheck
    public String getCurrentAdAsString() {
        return fbFacade.getCurrentAdSetAsString();
    }

    @SecurityCheck
    public String getAdInsight(long adSetId) {
        StringBuilder stringBuilder = new StringBuilder();
        Collection<AdSetInsight> adSetInsights = fbFacade.getInsight(adSetId);
        for (AdSetInsight adSetInsight : adSetInsights) {
            stringBuilder.append(adSetInsight);
            stringBuilder.append("<br />");

        }
        return stringBuilder.toString();
    }
}
