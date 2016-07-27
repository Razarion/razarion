package com.btxtech;

import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.shared.dto.GroundSkeletonConfig;
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
@Deprecated
public class GameMock {
    @Deprecated
    public static TerrainUiService startTerrainSurface(String slopeFile, String beachFile, String groundFile, String terrainSlopePositionFile) {
        throw new UnsupportedOperationException();
//        // Load terrain data
//        Gson gson = new Gson();
//        SlopeSkeletonConfig slopeSkeletonConfigBeach = gson.fromJson(new InputStreamReader(GameMock.class.getResourceAsStream(beachFile)), SlopeSkeletonConfig.class);
//        SlopeSkeletonConfig slopeSkeletonSlopeConfig = gson.fromJson(new InputStreamReader(GameMock.class.getResourceAsStream(slopeFile)), SlopeSkeletonConfig.class);
//        GroundSkeletonConfig groundSkeletonConfig = gson.fromJson(new InputStreamReader(GameMock.class.getResourceAsStream(groundFile)), GroundSkeletonConfig.class);
//        List<TerrainSlopePosition> terrainSlopePositions = gson.fromJson(new InputStreamReader(GameMock.class.getResourceAsStream(terrainSlopePositionFile)), new TypeToken<List<TerrainSlopePosition>>(){}.getType());
//
//        // Setup terrain surface
//        TerrainUiService terrainUiService = new TerrainUiService();
//        try {
//            terrainUiService.setGroundSkeleton(groundSkeletonConfig);
//            terrainUiService.setAllSlopeSkeletons(Arrays.asList(slopeSkeletonSlopeConfig, slopeSkeletonConfigBeach));
//            terrainUiService.setTerrainSlopePositions(terrainSlopePositions);
//            terrainUiService.setup();
//            return terrainUiService;
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw e;
//        }

    }
}
