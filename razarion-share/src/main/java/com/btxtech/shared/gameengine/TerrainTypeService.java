package com.btxtech.shared.gameengine;

import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SlopeSkeletonConfig;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 19.07.2016.
 */
@Singleton
public class TerrainTypeService {
    private Map<Integer, SlopeSkeletonConfig> slopeSkeletonConfigs = new HashMap<>();
    private GroundSkeletonConfig groundSkeletonConfig;

    public void onGameEngineInit(@Observes GameEngineInitEvent engineInitEvent) {
        groundSkeletonConfig = engineInitEvent.getGameEngineConfig().getGroundSkeletonConfig();
        slopeSkeletonConfigs.clear();
        if (engineInitEvent.getGameEngineConfig().getSlopeSkeletonConfigs() != null) {
            for (SlopeSkeletonConfig slopeSkeletonConfig : engineInitEvent.getGameEngineConfig().getSlopeSkeletonConfigs()) {
                slopeSkeletonConfigs.put(slopeSkeletonConfig.getId(), slopeSkeletonConfig);
            }
        }
    }

    public GroundSkeletonConfig getGroundSkeletonConfig() {
        return groundSkeletonConfig;
    }

    public SlopeSkeletonConfig getSlopeSkeleton(int id) {
        SlopeSkeletonConfig slopeSkeletonConfig = slopeSkeletonConfigs.get(id);
        if (slopeSkeletonConfig == null) {
            throw new IllegalArgumentException("No entry in integerSlopeSkeletonMap for id: " + id);
        }
        return slopeSkeletonConfig;
    }

}
