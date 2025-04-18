package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.AudioLibraryEntity;
import com.btxtech.server.repository.engine.AudioLibraryRepository;
import com.btxtech.shared.dto.AudioItemConfig;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 24.12.2016.
 */
@Service
public class AudioPersistence extends AbstractConfigCrudPersistence<AudioItemConfig, AudioLibraryEntity> {
    public AudioPersistence(AudioLibraryRepository audioJpaRepository) {
        super(AudioLibraryEntity.class, audioJpaRepository);
    }

    public static Integer idOrNull(AudioLibraryEntity audioLibraryEntity) {
        if (audioLibraryEntity != null) {
            return audioLibraryEntity.getId();
        } else {
            return null;
        }
    }

    public static List<Integer> toIds(List<AudioLibraryEntity> audioLibraryEntities) {
        if (audioLibraryEntities != null) {
            return audioLibraryEntities.stream().map(AudioPersistence::idOrNull).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Transactional
    public byte[] getAudio(int id) {
        return getJpaRepository()
                .findById(id)
                .orElseThrow()
                .getData();
    }

    @Transactional
    public AudioItemConfig getAudioItemConfig(int id) {
        return getAudioLibraryEntity(id).toAudioItemConfig();
    }

    public AudioLibraryEntity getAudioLibraryEntity(Integer id) {
        if (id == null) {
            return null;
        }
        return getJpaRepository()
                .findById(id)
                .orElseThrow();
    }

    @Transactional
    public List<AudioItemConfig> getAllAudios() {
        return getJpaRepository()
                .findAll()
                .stream()
                .map(AudioLibraryEntity::toAudioItemConfig)
                .collect(Collectors.toList());
    }

    @Override
    protected AudioItemConfig toConfig(AudioLibraryEntity entity) {
        return entity.toAudioItemConfig();
    }

    @Override
    protected void fromConfig(AudioItemConfig config, AudioLibraryEntity entity) {
        throw new UnsupportedOperationException("This may does not make any sense");
    }
}
