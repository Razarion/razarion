package com.btxtech.shared.system.perfmon;

import com.btxtech.shared.datatypes.Index;

import java.util.Date;

/**
 * Created by Beat
 * on 17.08.2017.
 */
public class TerrainTileStatistic {
    private Index terrainTileIndex;
    private int generationTime;
    private Date timeStamp;
    private String gameSessionUuid;

    public Index getTerrainTileIndex() {
        return terrainTileIndex;
    }

    public TerrainTileStatistic setTerrainTileIndex(Index terrainTileIndex) {
        this.terrainTileIndex = terrainTileIndex;
        return this;
    }

    public int getGenerationTime() {
        return generationTime;
    }

    public TerrainTileStatistic setGenerationTime(int generationTime) {
        this.generationTime = generationTime;
        return this;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public TerrainTileStatistic setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }

    public TerrainTileStatistic setGameSessionUuid(String gameSessionUuid) {
        this.gameSessionUuid = gameSessionUuid;
        return this;
    }
}
