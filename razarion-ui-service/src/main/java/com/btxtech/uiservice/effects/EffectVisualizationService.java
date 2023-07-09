package com.btxtech.uiservice.effects;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.uiservice.audio.AudioService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 14.10.2016.
 */
@ApplicationScoped
public class EffectVisualizationService {
    private final Logger logger = Logger.getLogger(EffectVisualizationService.class.getName());
    @Inject
    private AudioService audioService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private TrailService trailService;
    private Map<Integer, DemolitionBaseItemEntry> demolitionBaseItemEntries = new HashMap<>();

    public void onProjectileFired(BaseItemType baseItemType, Vertex muzzlePosition, Vertex target) {
        Integer muzzleFlashParticleEmitterSequenceConfigId = baseItemType.getWeaponType().getMuzzleFlashParticleSystemConfigId();
        if (muzzleFlashParticleEmitterSequenceConfigId == null) {
            logger.warning("No muzzleFlashParticleEmitterSequenceConfigId configured for: " + baseItemType);
            return;
        }
        playParticle(System.currentTimeMillis(), muzzlePosition, target.sub(muzzlePosition), muzzleFlashParticleEmitterSequenceConfigId);
    }

    public void onProjectileDetonation(int baseItemTypeId, Vertex position) {
    }

    public void baseItemRemoved(int[] removeSyncBaseItemIds) {
        for (int syncBaseItemId : removeSyncBaseItemIds) {
            removeBuildingDemolitionEffect(syncBaseItemId);
        }
    }

    private void playParticle(long timeStamp, Vertex position, Vertex direction, Integer muzzleFlashParticleEmitterSequenceConfigId) {
    }

    public void updateBuildingDemolitionEffect(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo, Vertex position3d, BaseItemType baseItemType) {
    }

    private void removeBuildingDemolitionEffect(int syncBaseItemId) {
        DemolitionBaseItemEntry demolitionBaseItemEntry = demolitionBaseItemEntries.remove(syncBaseItemId);
        if (demolitionBaseItemEntry != null) {
            demolitionBaseItemEntry.disposeParticles();
        }
    }
}
