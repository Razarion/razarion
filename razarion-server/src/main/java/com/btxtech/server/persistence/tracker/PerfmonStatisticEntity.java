package com.btxtech.server.persistence.tracker;

import com.btxtech.shared.system.perfmon.PerfmonEnum;
import com.btxtech.shared.system.perfmon.PerfmonStatistic;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 27.03.2017.
 */
@Entity
@Table(name = "TRACKER_PERFMON", indexes = {@Index(columnList = "sessionId")})
public class PerfmonStatisticEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(columnDefinition = "DATETIME(3)")
    private Date timeStamp;
    @Column(nullable = false, length = 190)
// Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    private String sessionId;
    @Enumerated(EnumType.STRING)
    private PerfmonEnum perfmonEnum;
    @Column(columnDefinition = "DATETIME(3)")
    private Date clientTimeStamp;
    @Column(length = 190)
// Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    private String gameSessionUuid;
    @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "perfmonStatisticEntity", nullable = false)
    @OrderColumn(name = "orderColumn")
    private List<PerfmonStatisticEntryEntity> perfmonStatisticEntryEntities;

    public void fromPerfmonStatistic(String sessionId, Date timeStamp, PerfmonStatistic perfmonStatistic) {
        this.sessionId = sessionId;
        this.timeStamp = timeStamp;
        perfmonEnum = perfmonStatistic.getPerfmonEnum();
        clientTimeStamp = perfmonStatistic.getTimeStamp();
        gameSessionUuid = perfmonStatistic.getGameSessionUuid();
        perfmonStatisticEntryEntities = perfmonStatistic.getPerfmonStatisticEntries().stream().map(perfmonStatisticEntry -> new PerfmonStatisticEntryEntity().fromPerfmonStatisticEntry(perfmonStatisticEntry)).collect(Collectors.toList());
    }

    public List<PerfmonTrackerDetail> toPerfmonTrackerDetails() {
        if (perfmonStatisticEntryEntities != null) {
            return perfmonStatisticEntryEntities.stream().map(perfmonStatisticEntryEntity -> {
                PerfmonTrackerDetail perfmonTrackerDetail = new PerfmonTrackerDetail();
                perfmonTrackerDetail.setClientStartTime(perfmonStatisticEntryEntity.getDate());
                perfmonTrackerDetail.setDuration(perfmonStatisticEntryEntity.getAvgDuration());
                perfmonTrackerDetail.setFrequency(perfmonStatisticEntryEntity.getFrequency());
                perfmonTrackerDetail.setSamples(perfmonStatisticEntryEntity.getSamples());
                perfmonTrackerDetail.setType(perfmonEnum.toString());
                return perfmonTrackerDetail;
            }).collect(Collectors.toList());
        }
        return Collections.emptyList();
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
