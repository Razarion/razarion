package com.btxtech.server.persistence.tracker;

import com.btxtech.shared.system.perfmon.PerfmonEnum;
import com.btxtech.shared.system.perfmon.PerfmonStatistic;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * 27.03.2017.
 */
@Entity
@Table(name = "TRACKER_PERFMON", indexes = {@Index(columnList = "sessionId")})
public class PerfmonStatisticEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;
    private Date timeStamp;
    @Column(nullable = false, length = 190)// Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    private String sessionId;
    @Enumerated(EnumType.STRING)
    private PerfmonEnum perfmonEnum;
    @ElementCollection
    @CollectionTable(name = "TRACKER_PERFMON_FREQUENCY", joinColumns = @JoinColumn(name = "perfmonStatisticEntityId"))
    @OrderColumn(name = "orderColumn")
    private List<Double> frequency;
    @ElementCollection
    @CollectionTable(name = "TRACKER_PERFMON_DURATION", joinColumns = @JoinColumn(name = "perfmonStatisticEntityId"))
    @OrderColumn(name = "orderColumn")
    private List<Double> avgDuration;
    private Date clientTimeStamp;

    public void fromPerfmonStatistic(String sessionId, Date timeStamp, PerfmonStatistic perfmonStatistic) {
        this.sessionId = sessionId;
        this.timeStamp = timeStamp;
        perfmonEnum = perfmonStatistic.getPerfmonEnum();
        if (frequency == null) {
            frequency = new ArrayList<>();
        }
        frequency.clear();
        frequency.addAll(perfmonStatistic.getFrequency());
        if (avgDuration == null) {
            avgDuration = new ArrayList<>();
        }
        avgDuration.clear();
        avgDuration.addAll(perfmonStatistic.getAvgDuration());
        clientTimeStamp = perfmonStatistic.getTimeStamp();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PerfmonStatisticEntity that = (PerfmonStatisticEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
