package com.btxtech.server.persistence.tracker;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.system.perfmon.TerrainTileStatistic;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
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
 * on 18.08.2017.
 */
@Entity
@Table(name = "TRACKER_TERRAIN_TILE", indexes = {@Index(columnList = "sessionId")})
public class TerrainTileStatisticEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(columnDefinition = "DATETIME(3)")
    private Date timeStamp;
    @Column(nullable = false, length = 190)
    // Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    private String sessionId;
    @Column(columnDefinition = "DATETIME(3)")
    private Date clientTimeStamp;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "terrainTileX")),
            @AttributeOverride(name = "y", column = @Column(name = "terrainTileY")),
    })
    private com.btxtech.shared.datatypes.Index terrainTileIndex;
    private int generationTime;
    @Column(length = 190)// Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    private String gameSessionUuid;

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void fromTerrainTileStatistic(TerrainTileStatistic terrainTileStatistic) {
        clientTimeStamp = terrainTileStatistic.getTimeStamp();
        terrainTileIndex = terrainTileStatistic.getTerrainTileIndex();
        generationTime = terrainTileStatistic.getGenerationTime();
        gameSessionUuid = terrainTileStatistic.getGameSessionUuid();
    }

    public PerfmonTerrainTileDetail toPerfmonTerrainTileDetail() {
        DecimalPosition position = TerrainUtil.toTileAbsolute(terrainTileIndex);
        return new PerfmonTerrainTileDetail().setDuration(generationTime).setClientStartTime(clientTimeStamp).setPositionX(position.getX()).setPositionY(position.getY());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TerrainTileStatisticEntity that = (TerrainTileStatisticEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
