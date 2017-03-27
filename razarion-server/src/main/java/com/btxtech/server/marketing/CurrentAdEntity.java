package com.btxtech.server.marketing;

import com.btxtech.server.marketing.facebook.CreationData;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
@Table(name = "FB_MARKETING_CURRENT_AD")
public class CurrentAdEntity {
    public enum State {
        RUNNING,
        WAITING_FOR_ARCHIVING
    }

    @Id
    @GeneratedValue
    private Long id;
    @Enumerated(EnumType.STRING)
    private State state;
    private Date dateStart;
    private Date dateStop;
    private long campaignId;
    private long adSetId;
    private long adId;

    @ElementCollection
    @CollectionTable(
            name = "FB_MARKETING_CURRENT_AD_INTEREST",
            joinColumns = @JoinColumn(name = "currentAdEntityId")
    )
    private List<Interest> interests;

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

    public void setIds(CreationData creationData) {
        campaignId = creationData.getCampaignId();
        adSetId = creationData.getAdSetId();
        adId = creationData.getAdId();
    }

    public List<Interest> getInterests() {
        return interests;
    }

    public void setInterests(List<Interest> interests) {
        if (this.interests != null) {
            this.interests.clear();
        } else {
            this.interests = new ArrayList<>();
        }
        this.interests.addAll(interests);
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
