package com.btxtech.server.marketing;

import com.btxtech.server.marketing.facebook.AdSetInsight;
import com.btxtech.server.marketing.restdatatypes.AdInterestJson;
import com.btxtech.server.marketing.restdatatypes.CampaignJson;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * 19.03.2017.
 */
@Entity
@Table(name = "FB_MARKETING_HISTORY_AD")
public class HistoryAdEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private long campaignId;
    private long adSetId;
    private long adId;
    private Date dateStart;
    private Date dateStop;
    private int clicks;
    private int impressions;
    private double spent;
    private String title;
    private String body;
    private String imageHash;
    @ElementCollection
    @CollectionTable(
            name = "FB_MARKETING_HISTORY_AD_INTEREST",
            joinColumns = @JoinColumn(name = "historyAdEntityId")
    )
    private List<Interest> interests;
    private String urlTagParam;
    private Date scheduleTimeStart;
    private Date scheduleTimeEnd;
    private boolean lifeTime;
    private Double dailyBudget;
    private Double lifeTimeBudget;

    public void fill(CurrentAdEntity currentAdEntity, AdSetInsight adSetInsight) {
        campaignId = currentAdEntity.getCampaignId();
        adSetId = currentAdEntity.getAdSetId();
        adId = currentAdEntity.getAdId();
        dateStart = currentAdEntity.getDateStart();
        dateStop = currentAdEntity.getDateStop();
        title = currentAdEntity.getTitle();
        body = currentAdEntity.getBody();
        imageHash = currentAdEntity.getImageHash();
        interests = new ArrayList<>();
        for (Interest currentInterest : currentAdEntity.getInterests()) {
            interests.add(new Interest(currentInterest));
        }
        clicks = adSetInsight.getClicks();
        impressions = adSetInsight.getImpressions();
        spent = adSetInsight.getSpent();
        urlTagParam = currentAdEntity.getUrlTagParam();
        scheduleTimeStart = currentAdEntity.getScheduleTimeStart();
        scheduleTimeEnd = currentAdEntity.getScheduleTimeEnd();
        lifeTime = currentAdEntity.isLifeTime();
        dailyBudget = currentAdEntity.getDailyBudget();
        lifeTimeBudget = currentAdEntity.getLifeTimeBudget();
    }

    public CampaignJson createCampaignJson() {
        List<AdInterestJson> adInterest = new ArrayList<>();
        for (Interest interest : interests) {
            adInterest.add(interest.generateAdInterestJson());
        }
        CampaignJson campaignJson = new CampaignJson().setAdId(Long.toString(adId)).setBody(body).setTitle(title).setClicks(clicks).setDateStart(dateStart).setDateStop(dateStop);
        campaignJson.setImpressions(impressions).setSpent(spent).setUrlTagParam(urlTagParam).setAdInterests(adInterest);
        campaignJson.setScheduleTimeStart(scheduleTimeStart).setScheduleTimeEnd(scheduleTimeEnd).setLifeTime(lifeTime).setDailyBudget(dailyBudget).setLifeTimeBudget(lifeTimeBudget);
        return campaignJson;
    }

    public String getImageHash() {
        return imageHash;
    }

    @Override
    public String toString() {
        return "HistoryAdEntity{" +
                "dateStart=" + dateStart +
                ", dateStop=" + dateStop +
                ", clicks=" + clicks +
                ", impressions=" + impressions +
                ", spent=" + spent +
                '}';
    }
}
