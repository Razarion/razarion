package com.btxtech.uiservice.effects;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.particle.ParticleEmitterSequenceConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionParticleConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionStepEffect;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSimpleSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.nativejs.NativeVertexDto;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.particle.ParticleEmitterSequenceHandler;
import com.btxtech.uiservice.particle.ParticleService;
import com.btxtech.uiservice.renderer.ViewService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static com.btxtech.shared.gameengine.datatypes.workerdto.NativeUtil.toVertex;

/**
 * Created by Beat
 * 14.10.2016.
 */
@ApplicationScoped
public class EffectVisualizationService {
    private Logger logger = Logger.getLogger(EffectVisualizationService.class.getName());
    @Inject
    private AudioService audioService;
    @Inject
    private ViewService viewService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private ParticleService particleService;
    @Inject
    private TrailService trailService;
    private Map<Integer, DemolitionBaseItemEntry> demolitionBaseItemEntries = new HashMap<>();
    private Map<Integer, ParticleEmitterSequenceHandler> buildupBaseItemEntries = new HashMap<>();

    public void onProjectileFired(BaseItemType baseItemType, Vertex muzzlePosition, Vertex target) {
        Integer muzzleFlashParticleEmitterSequenceConfigId = baseItemType.getWeaponType().getMuzzleFlashParticleSystemConfigId();
        if (muzzleFlashParticleEmitterSequenceConfigId == null) {
            logger.warning("No muzzleFlashParticleEmitterSequenceConfigId configured for: " + baseItemType);
            return;
        }
        playParticle(System.currentTimeMillis(), muzzlePosition, target.sub(muzzlePosition), muzzleFlashParticleEmitterSequenceConfigId);
    }

    public void onProjectileDetonation(int baseItemTypeId, Vertex position) {
        BaseItemType baseItemType = itemTypeService.getBaseItemType(baseItemTypeId);
        Integer detonationParticleEmitterSequenceConfigId = baseItemType.getWeaponType().getDetonationParticleConfigId();
        if (detonationParticleEmitterSequenceConfigId == null) {
            logger.warning("No projectile detonationParticleEmitterSequenceConfigId configured for: " + baseItemType);
            return;
        }
        playParticle(System.currentTimeMillis(), position, null, detonationParticleEmitterSequenceConfigId);
    }

    public void baseItemRemoved(int[] removeSyncBaseItemIds) {
        for (int syncBaseItemId : removeSyncBaseItemIds) {
            removeBuildingDemolitionEffect(syncBaseItemId);
        }
    }

    private void onSyncBaseItemExplode(NativeSimpleSyncBaseItemTickInfo nativeSimpleSyncBaseItemTickInfo, long timeStamp) {
        removeBuildingDemolitionEffect(nativeSimpleSyncBaseItemTickInfo.id);
        trailService.addWreckage(nativeSimpleSyncBaseItemTickInfo);
        BaseItemType baseItemType = itemTypeService.getBaseItemType(nativeSimpleSyncBaseItemTickInfo.itemTypeId);
        Integer explosionParticleEmitterSequenceConfigId = baseItemType.getExplosionParticleConfigId();
        if (explosionParticleEmitterSequenceConfigId == null) {
            logger.warning("No explosionParticleEmitterSequenceConfigId configured for baseItemType: " + baseItemType.getId());
            return;
        }
        playParticle(timeStamp, new Vertex(nativeSimpleSyncBaseItemTickInfo.x, nativeSimpleSyncBaseItemTickInfo.y, nativeSimpleSyncBaseItemTickInfo.z), null, explosionParticleEmitterSequenceConfigId);
    }

    private void playParticle(long timeStamp, Vertex position, Vertex direction, Integer muzzleFlashParticleEmitterSequenceConfigId) {
        ParticleEmitterSequenceConfig particleEmitterSequenceConfig = particleService.getParticleEmitterSequenceConfig(muzzleFlashParticleEmitterSequenceConfigId);
        playSound(particleEmitterSequenceConfig.getAudioIds(), position);
        particleService.start(timeStamp, position, direction, particleEmitterSequenceConfig);
    }

    private void playSound(List<Integer> audioIs, Vertex position) {
        if (!viewService.getCurrentViewField().isInside(position.toXY())) {
            return;
        }

        if (audioIs != null && !audioIs.isEmpty()) {
            if (audioIs.size() == 1) {
                audioService.onParticle(audioIs.get(0));
            } else {
                int index = (int) (audioIs.size() * Math.random());
                audioService.onParticle(audioIs.get(index));
            }
        }

    }

    public void updateBuildingDemolitionEffect(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo, Vertex position3d, BaseItemType baseItemType) {
        DemolitionBaseItemEntry demolitionBaseItemEntry = demolitionBaseItemEntries.computeIfAbsent(nativeSyncBaseItemTickInfo.id, key -> new DemolitionBaseItemEntry());
        int step = baseItemType.getDemolitionStep(nativeSyncBaseItemTickInfo.health);
        if (demolitionBaseItemEntry.getDemolitionStep() == step) {
            return;
        }

        demolitionBaseItemEntry.disposeParticles();
        DemolitionStepEffect demolitionStepEffect = baseItemType.getDemolitionStepEffect(step);

        for (DemolitionParticleConfig demolitionParticleConfig : demolitionStepEffect.getDemolitionParticleConfigs()) {
            ParticleEmitterSequenceConfig particleEmitterSequenceConfig = particleService.getParticleEmitterSequenceConfig(demolitionParticleConfig.getParticleConfigId());
            demolitionBaseItemEntry.addParticleHandler(particleService.start(System.currentTimeMillis(), position3d.add(demolitionParticleConfig.getPosition()), null, particleEmitterSequenceConfig));
        }

        demolitionBaseItemEntry.setDemolitionStep(step);
    }

    public void updateBuildupParticle(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo, Vertex muzzlePosition, BaseItemType baseItemType, NativeVertexDto direction) {
        ParticleEmitterSequenceHandler particleEmitterSequenceHandler = buildupBaseItemEntries.get(nativeSyncBaseItemTickInfo.id);
        if (particleEmitterSequenceHandler != null) {
            return;
        }
        ParticleEmitterSequenceConfig particleEmitterSequenceConfig = particleService.getParticleEmitterSequenceConfig(baseItemType.getBuilderType().getAnimationParticleId());
        buildupBaseItemEntries.put(
                nativeSyncBaseItemTickInfo.id,
                particleService.start(System.currentTimeMillis(), muzzlePosition, toVertex(direction), particleEmitterSequenceConfig));
    }

    private void removeBuildingDemolitionEffect(int syncBaseItemId) {
        DemolitionBaseItemEntry demolitionBaseItemEntry = demolitionBaseItemEntries.remove(syncBaseItemId);
        if (demolitionBaseItemEntry != null) {
            demolitionBaseItemEntry.disposeParticles();
        }
    }

    public void removeBuildupParticle(Set<Integer> baseItemIds) {
        baseItemIds.forEach(baseItemId -> {
            ParticleEmitterSequenceHandler particleEmitterSequenceHandler = buildupBaseItemEntries.remove(baseItemId);
            if (particleEmitterSequenceHandler != null) {
                particleEmitterSequenceHandler.dispose();
            }
        });
    }
}
