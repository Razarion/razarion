package com.btxtech.server.marketing;

import com.btxtech.server.marketing.facebook.CreationInput;
import com.btxtech.server.marketing.facebook.CreationResult;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
@Table(name = "FB_MARKETING_CURRENT_AD")
public class CurrentAdEntity {
    public enum State {
        RUNNING,
        WAITING_FOR_ARCHIVING
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Enumerated(EnumType.STRING)
    private State state;
    @Column(columnDefinition = "DATETIME(3)")
    private Date dateStart;
    @Column(columnDefinition = "DATETIME(3)")
    private Date dateStop;
    @Column(columnDefinition = "DATETIME(3)")
    private Date scheduleTimeStart;
    @Column(columnDefinition = "DATETIME(3)")
    private Date scheduleTimeEnd;
    private long campaignId;
    private long adSetId;
    private long adId;
    private String title;
    private String body;
    private String imageHash;
    @ElementCollection
    @CollectionTable(
            name = "FB_MARKETING_CURRENT_AD_INTEREST",
            joinColumns = @JoinColumn(name = "currentAdEntityId")
    )
    private List<Interest> interests;
    private String urlTagParam;
    private boolean lifeTime;
    private Double dailyBudget;
    private Double lifeTimeBudget;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateStop() {
        return dateStop;
    }

    public void setDateStop(Date dateStop) {
        this.dateStop = dateStop;
    }

    public long getCampaignId() {
        return campaignId;
    }

    public long getAdSetId() {
        return adSetId;
    }

    public long getAdId() {
        return adId;
    }

    public void setIds(CreationResult creationResult) {
        campaignId = creationResult.getCampaignId();
        adSetId = creationResult.getAdSetId();
        adId = creationResult.getAdId();
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getImageHash() {
        return imageHash;
    }

    public List<Interest> getInterests() {
        return interests;
    }

    public String getUrlTagParam() {
        return urlTagParam;
    }

    public Date getScheduleTimeStart() {
        return scheduleTimeStart;
    }

    public Date getScheduleTimeEnd() {
        return scheduleTimeEnd;
    }

    public boolean isLifeTime() {
        return lifeTime;
    }

    public Double getDailyBudget() {
        return dailyBudget;
    }

    public Double getLifeTimeBudget() {
        return lifeTimeBudget;
    }

    public void setCreationInput(CreationInput creationInput) {
        title = creationInput.getTitle();
        body = creationInput.getBody();
        urlTagParam = creationInput.getUrlTagParam();
        if (interests != null) {
            interests.clear();
        } else {
            interests = new ArrayList<>();
        }
        interests.addAll(creationInput.getInterests());
        imageHash = creationInput.getFbAdImage().getHash();
        scheduleTimeStart = creationInput.getScheduleStartTime();
        scheduleTimeEnd = creationInput.getScheduleEndTime();
        lifeTime = creationInput.isLifeTime();
        dailyBudget = creationInput.getDailyBudget();
        lifeTimeBudget = creationInput.getLifeTimeBudget();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CurrentAdEntity that = (CurrentAdEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
