package com.btxtech;

import com.btxtech.client.terrain.TerrainSurface;
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

        // Setup TerrainSlopePosition
//        TerrainSlopePosition terrainSlopePosition = new TerrainSlopePosition();
//        terrainSlopePosition.setId(1);
//        terrainSlopePosition.setSlopeId(2706);
//        terrainSlopePosition.setPolygon(Arrays.asList(new Index(908, 980), new Index(880, 1036), new Index(836, 1080), new Index(807, 1095), new Index(795, 1118), new Index(751, 1162), new Index(695, 1190), new Index(633, 1200), new Index(571, 1190), new Index(515, 1162), new Index(471, 1118), new Index(443, 1062), new Index(440, 1042), new Index(402, 1004), new Index(374, 948), new Index(368, 910), new Index(363, 905), new Index(335, 849), new Index(325, 787), new Index(335, 725), new Index(363, 669), new Index(388, 644), new Index(411, 598), new Index(455, 554), new Index(460, 551), new Index(479, 514), new Index(523, 470), new Index(579, 442), new Index(641, 432), new Index(703, 442), new Index(759, 470), new Index(803, 514), new Index(829, 567), new Index(838, 571), new Index(882, 615), new Index(907, 665), new Index(935, 693), new Index(963, 749), new Index(973, 811), new Index(963, 873), new Index(935, 929), new Index(913, 951)));
        // terrainSlopePosition.setPolygon(Arrays.asList(new Index(300, 200), new Index(400, 200), new Index(400, 400), new Index(200, 400), new Index(300, 300)));
        // terrainSlopePosition.setPolygon(Arrays.asList(new Index(300, 200), new Index(400, 200), new Index(400, 400), new Index(200, 400), new Index(300, 300), new Index(300, 280)));
        //terrainSlopePosition.setPolygon(Arrays.asList(new Index(300, 200), new Index(400, 200), new Index(400, 400), new Index(200, 400), new Index(300, 300), new Index(305, 280)));

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
