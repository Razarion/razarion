package com.btxtech.uiservice.effects;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionParticleConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionStepEffect;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.particle.ParticleEmitterSequenceConfig;
import com.btxtech.uiservice.particle.ParticleService;
import com.btxtech.uiservice.renderer.ViewService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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

    public void onProjectileFired(BaseItemType baseItemType, Vertex muzzlePosition, Vertex target) {
        Integer muzzleFlashParticleEmitterSequenceConfigId = baseItemType.getWeaponType().getMuzzleFlashParticleEmitterSequenceConfigId();
        if (muzzleFlashParticleEmitterSequenceConfigId == null) {
            logger.warning("No muzzleFlashParticleEmitterSequenceConfigId configured for: " + baseItemType);
            return;
        }
        playParticle(System.currentTimeMillis(), muzzlePosition, target.sub(muzzlePosition), muzzleFlashParticleEmitterSequenceConfigId);
    }

    public void onProjectileDetonation(int baseItemTypeId, Vertex position) {
        BaseItemType baseItemType = itemTypeService.getBaseItemType(baseItemTypeId);
        Integer detonationParticleEmitterSequenceConfigId = baseItemType.getWeaponType().getDetonationParticleEmitterSequenceConfigId();
        if (detonationParticleEmitterSequenceConfigId == null) {
            logger.warning("No projectile detonationParticleEmitterSequenceConfigId configured for: " + baseItemType);
            return;
        }
        playParticle(System.currentTimeMillis(), position, null, detonationParticleEmitterSequenceConfigId);
    }

    public void onSyncBaseItemsExplode(Collection<SyncBaseItemSimpleDto> syncBaseItems) {
        long timeStamp = System.currentTimeMillis();
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            onSyncBaseItemExplode(syncBaseItem, timeStamp);
        }
    }

    public void baseItemRemoved(Collection<SyncBaseItemSimpleDto> syncBaseItems) {
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            removeBuildingDemolitionEffect(syncBaseItem);
        }
    }

    private void onSyncBaseItemExplode(SyncBaseItemSimpleDto syncBaseItem, long timeStamp) {
        removeBuildingDemolitionEffect(syncBaseItem);
        trailService.addWreckage(syncBaseItem);
        BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
        Integer explosionParticleEmitterSequenceConfigId = baseItemType.getExplosionParticleEmitterSequenceConfigId();
        if (explosionParticleEmitterSequenceConfigId == null) {
            logger.warning("No explosionParticleEmitterSequenceConfigId configured for: " + System.currentTimeMillis());
            return;
        }
        playParticle(timeStamp, syncBaseItem.getPosition3d(), null, explosionParticleEmitterSequenceConfigId);
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

    public void updateBuildingDemolitionEffect(SyncBaseItemSimpleDto syncBaseItem, BaseItemType baseItemType) {
        DemolitionBaseItemEntry demolitionBaseItemEntry = demolitionBaseItemEntries.computeIfAbsent(syncBaseItem.getId(), key -> new DemolitionBaseItemEntry());
        int step = baseItemType.getDemolitionStep(syncBaseItem.getHealth());
        if (demolitionBaseItemEntry.getDemolitionStep() == step) {
            return;
        }

        demolitionBaseItemEntry.disposePartices();
        DemolitionStepEffect demolitionStepEffect = baseItemType.getDemolitionStepEffect(step);

        for (DemolitionParticleConfig demolitionParticleConfig : demolitionStepEffect.getDemolitionParticleConfigs()) {
            ParticleEmitterSequenceConfig particleEmitterSequenceConfig = particleService.getParticleEmitterSequenceConfig(demolitionParticleConfig.getParticleEmitterSequenceConfigId());
            demolitionBaseItemEntry.addParticleHandler(particleService.start(System.currentTimeMillis(), syncBaseItem.getPosition3d().add(demolitionParticleConfig.getPosition()), null, particleEmitterSequenceConfig));
        }

        demolitionBaseItemEntry.setDemolitionStep(step);
    }

    private void removeBuildingDemolitionEffect(SyncBaseItemSimpleDto syncBaseItem) {
        DemolitionBaseItemEntry demolitionBaseItemEntry = demolitionBaseItemEntries.remove(syncBaseItem.getId());
        if (demolitionBaseItemEntry != null) {
            demolitionBaseItemEntry.disposePartices();
        }
    }
}
