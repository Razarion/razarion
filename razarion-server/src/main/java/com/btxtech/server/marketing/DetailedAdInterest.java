package com.btxtech.server.marketing;

import com.btxtech.server.marketing.facebook.AdInterest;

import java.util.Collection;
import java.util.Date;

/**
 * Created by Beat
 * 01.05.2017.
 */
public class DetailedAdInterest {
    private AdInterest adInterest;
    private boolean usedInCurrent;
    private boolean usedInHistory;
    private Collection<Date> usedCurrentDates;
    private Collection<Date> usedHistoryDates;

    public AdInterest getAdInterest() {
        return adInterest;
    }

    public DetailedAdInterest setAdInterest(AdInterest adInterest) {
        this.adInterest = adInterest;
        return this;
    }

    public boolean isUsedInCurrent() {
        return usedInCurrent;
    }

    public boolean isUsedInHistory() {
        return usedInHistory;
    }

    public Collection<Date> getUsedCurrentDates() {
        return usedCurrentDates;
    }

    public DetailedAdInterest setUsedCurrentDates(Collection<Date> usedCurrentDates) {
        this.usedCurrentDates = usedCurrentDates;
        usedInCurrent = !usedCurrentDates.isEmpty();
        return this;
    }

    public Collection<Date> getUsedHistoryDates() {
        return usedHistoryDates;
    }

    public DetailedAdInterest setUsedHistoryDates(Collection<Date> usedHistoryDates) {
        this.usedHistoryDates = usedHistoryDates;
        usedInHistory = !usedHistoryDates.isEmpty();
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DetailedAdInterest that = (DetailedAdInterest) o;

        return adInterest.equals(that.adInterest);
    }

    @Override
    public int hashCode() {
        return adInterest.hashCode();
    }
}
