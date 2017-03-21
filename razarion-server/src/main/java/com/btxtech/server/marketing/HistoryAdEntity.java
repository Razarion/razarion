package com.btxtech.server.marketing;

import com.btxtech.server.marketing.facebook.AdSetInsight;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
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
    @GeneratedValue
    private Long id;
    private long adSetId;
    private Date dateStart;
    private Date dateStop; // Wrong data from facebook
    private Date facebookDateStart;
    private Date facebookDateStop; // Wrong data from facebook
    private int clicks;
    private int impressions;
    private double spent;
    @ElementCollection
    @CollectionTable(
            name = "FB_MARKETING_HISTORY_AD_INTEREST",
            joinColumns = @JoinColumn(name = "historyAdEntityId")
    )
    private List<Interest> interests;


    public void fill(CurrentAdEntity currentAdEntity, AdSetInsight adSetInsight) {
        adSetId = currentAdEntity.getAdSetId();
        dateStart = currentAdEntity.getDateStart();
        dateStop = currentAdEntity.getDateStop();
        interests = new ArrayList<>();
        for (Interest currentInterest : currentAdEntity.getInterests()) {
            interests.add(new Interest(currentInterest));
        }
        facebookDateStart = adSetInsight.getFacebookDateStart();
        facebookDateStop = adSetInsight.getFacebookDateStop();
        clicks = adSetInsight.getClicks();
        impressions = adSetInsight.getImpressions();
        spent = adSetInsight.getSpent();
    }

    @Override
    public String toString() {
        return "HistoryAdEntity{" +
                "facebookDateStart=" + facebookDateStart +
                ", facebookDateStop=" + facebookDateStop +
                ", clicks=" + clicks +
                ", impressions=" + impressions +
                ", spent=" + spent +
                '}';
    }
}
