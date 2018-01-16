package com.btxtech.server.persistence.tracker;

import com.btxtech.shared.system.perfmon.PerfmonStatisticEntry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Beat
 * on 25.09.2017.
 */
@Entity
@Table(name = "TRACKER_PERFMON_ENTRY")
public class PerfmonStatisticEntryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private double frequency;
    private double avgDuration;
    private int samples;
    @Column(columnDefinition = "DATETIME(3)")
    private Date date;

    public PerfmonStatisticEntryEntity fromPerfmonStatisticEntry(PerfmonStatisticEntry perfmonStatisticEntry) {
        frequency = perfmonStatisticEntry.getFrequency();
        avgDuration = perfmonStatisticEntry.getAvgDuration();
        samples = perfmonStatisticEntry.getSamples();
        date = perfmonStatisticEntry.getDate();
        return this;
    }

    public double getFrequency() {
        return frequency;
    }

    public double getAvgDuration() {
        return avgDuration;
    }

    public int getSamples() {
        return samples;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PerfmonStatisticEntryEntity that = (PerfmonStatisticEntryEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
