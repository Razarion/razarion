package com.btxtech;

import com.btxtech.uiservice.terrain.TerrainSurface;
import com.btxtech.shared.dto.GroundSkeleton;
import com.btxtech.shared.dto.SlopeSkeleton;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 18.05.2016.
 */
public class GameMock {
    public static TerrainSurface startTerrainSurface(String slopeFile, String beachFile, String groundFile, String terrainSlopePositionFile) {
        // Load terrain data
        Gson gson = new Gson();
        SlopeSkeleton slopeSkeletonBeach = gson.fromJson(new InputStreamReader(GameMock.class.getResourceAsStream(beachFile)), SlopeSkeleton.class);
        SlopeSkeleton slopeSkeletonSlope = gson.fromJson(new InputStreamReader(GameMock.class.getResourceAsStream(slopeFile)), SlopeSkeleton.class);
        GroundSkeleton groundSkeleton = gson.fromJson(new InputStreamReader(GameMock.class.getResourceAsStream(groundFile)), GroundSkeleton.class);
        List<TerrainSlopePosition> terrainSlopePositions = gson.fromJson(new InputStreamReader(GameMock.class.getResourceAsStream(terrainSlopePositionFile)), new TypeToken<List<TerrainSlopePosition>>(){}.getType());

        // Setup terrain surface
        TerrainSurface terrainSurface = new TerrainSurface();
        try {
            terrainSurface.setGroundSkeleton(groundSkeleton);
            terrainSurface.setAllSlopeSkeletons(Arrays.asList(slopeSkeletonSlope, slopeSkeletonBeach));
            terrainSurface.setTerrainSlopePositions(terrainSlopePositions);
            terrainSurface.init();
            return terrainSurface;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }
}
