package com.btxtech.uiservice.clip;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.ClipConfig;
import com.btxtech.shared.dto.VisualConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.particle.ParticleEmitterSequenceConfig;
import com.btxtech.uiservice.particle.ParticleService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 14.10.2016.
 */
@ApplicationScoped
public class EffectService {
    private Logger logger = Logger.getLogger(EffectService.class.getName());
    @Inject
    private Shape3DUiService shape3DUiService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private AudioService audioService;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private ParticleService particleService;
    private Map<Integer, ClipConfig> clips = new HashMap<>();
    private final Collection<PlayingClip> playingClips = new ArrayList<>();

    public void onVisualConfig(@Observes VisualConfig visualConfig) {
        setShapes3Ds(visualConfig.getClipConfigs());
    }

    public void setShapes3Ds(List<ClipConfig> clipConfigs) {
        clips.clear();
        if (clipConfigs != null) {
            for (ClipConfig clipConfig : clipConfigs) {
                clips.put(clipConfig.getId(), clipConfig);
            }
        }
    }

    public Collection<ClipConfig> getClipConfigs() {
        return clips.values();
    }

    public ClipConfig getClipConfig(int clipId) {
        ClipConfig clipConfig = clips.get(clipId);
        if (clipConfig == null) {
            throw new IllegalArgumentException("No ClipConfig for id: " + clipId);
        }
        return clipConfig;
    }

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

    private void onSyncBaseItemExplode(SyncBaseItemSimpleDto syncBaseItem, long timeStamp) {
        BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
        Integer explosionParticleEmitterSequenceConfigId = baseItemType.getExplosionParticleEmitterSequenceConfigId();
        if (explosionParticleEmitterSequenceConfigId == null) {
            logger.warning("No explosionParticleEmitterSequenceConfigId ClipId configured for: " + System.currentTimeMillis());
            return;
        }
        playParticle(timeStamp, syncBaseItem.getPosition3d(), null, explosionParticleEmitterSequenceConfigId);
    }

    public void playClip(Vertex position, int clipId, long timeStamp) {
        ClipConfig clipConfig = getClipConfig(clipId);
        playSound(clipConfig.getAudioIds(), position);
        synchronized (playingClips) {
            playingClips.add(new PlayingClip(position, clipConfig, timeStamp));
        }
    }

    private void playParticle(long timeStamp, Vertex position, Vertex direction, Integer muzzleFlashParticleEmitterSequenceConfigId) {
        ParticleEmitterSequenceConfig particleEmitterSequenceConfig = particleService.getParticleEmitterSequenceConfig(muzzleFlashParticleEmitterSequenceConfigId);
        playSound(particleEmitterSequenceConfig.getAudioIds(), position);
        particleService.start(timeStamp, position, direction, particleEmitterSequenceConfig);
    }

    public List<ModelMatrices> provideModelMatrices(ClipConfig clipConfig, long timeStamp) {
        List<ModelMatrices> result = new ArrayList<>();
        synchronized (playingClips) {
            for (Iterator<PlayingClip> iterator = playingClips.iterator(); iterator.hasNext(); ) {
                PlayingClip playingClip = iterator.next();
                if (clipConfig.equals(playingClip.getClipConfig())) {
                    ModelMatrices modelMatrices = playingClip.provideModelMatrices(timeStamp);
                    if (modelMatrices != null) {
                        result.add(modelMatrices);
                    } else {
                        iterator.remove();
                    }
                }
            }
        }
        return result;
    }

    public void override(ClipConfig clipConfig) {
        clips.put(clipConfig.getId(), clipConfig);
    }

    public void remove(ClipConfig clipConfig) {
        clips.remove(clipConfig.getId());
    }

    private void playSound(List<Integer> audioIs, Vertex position) {
        if (!terrainScrollHandler.getCurrentViewField().isInside(position.toXY())) {
            return;
        }

        if (audioIs != null && !audioIs.isEmpty()) {
            if (audioIs.size() == 1) {
                audioService.onClip(audioIs.get(0));
            } else {
                int index = (int) (audioIs.size() * Math.random());
                audioService.onClip(audioIs.get(index));
            }
        }

    }
}
