package com.btxtech.server.persistence;

import com.btxtech.server.DataUrlDecoder;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.dto.AudioItemConfig;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 24.12.2016.
 */
@Singleton
public class AudioPersistence {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public byte[] getAudio(int id) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
        Root<AudioLibraryEntity> root = cq.from(AudioLibraryEntity.class);
        cq.where(criteriaBuilder.equal(root.get(AudioLibraryEntity_.id), id));
        cq.multiselect(root.get(AudioLibraryEntity_.data));
        Tuple tupleResult = entityManager.createQuery(cq).getSingleResult();
        return (byte[]) tupleResult.get(0);
    }

    @Transactional
    public AudioItemConfig getAudioItemConfig(int id) {
        return getAudioLibraryEntity(id).toAudioConfig();
    }

    @Transactional
    public AudioLibraryEntity getAudioLibraryEntity(Integer id) {
        if (id == null) {
            return null;
        }
        AudioLibraryEntity audioLibraryEntity = entityManager.find(AudioLibraryEntity.class, id);
        if (audioLibraryEntity == null) {
            throw new IllegalArgumentException("No AudioLibraryEntity for id: " + id);
        }
        return audioLibraryEntity;
    }

    @Transactional
    public List<AudioItemConfig> getAllAudios() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AudioLibraryEntity> userQuery = criteriaBuilder.createQuery(AudioLibraryEntity.class);
        Root<AudioLibraryEntity> from = userQuery.from(AudioLibraryEntity.class);
        CriteriaQuery<AudioLibraryEntity> userSelect = userQuery.select(from);
        List<AudioLibraryEntity> images = entityManager.createQuery(userSelect).getResultList();
        List<AudioItemConfig> items = new ArrayList<>();
        for (AudioLibraryEntity image : images) {
            items.add(image.toAudioConfig());
        }
        return items;
    }

    @Transactional
    @SecurityCheck
    public void createAudio(DataUrlDecoder dataUrlDecoder) {
        AudioLibraryEntity audioLibraryEntity = new AudioLibraryEntity();
        audioLibraryEntity.setType(dataUrlDecoder.getType());
        audioLibraryEntity.setData(dataUrlDecoder.getData());
        audioLibraryEntity.setSize(dataUrlDecoder.getDataLength());
        entityManager.persist(audioLibraryEntity);
    }

    @Transactional
    @SecurityCheck
    public void save(List<AudioItemConfig> audioItemConfigs) {
        for (AudioItemConfig audioItemConfig : audioItemConfigs) {
            AudioLibraryEntity audioLibraryEntity = entityManager.find(AudioLibraryEntity.class, audioItemConfig.getId());
            if (audioItemConfig.getInternalName() != null) {
                audioLibraryEntity.setInternalName(audioItemConfig.getInternalName());
            }
            if (audioItemConfig.getDataUrl() != null) {
                DataUrlDecoder dataUrlDecoder = new DataUrlDecoder(audioItemConfig.getDataUrl());
                audioLibraryEntity.setType(dataUrlDecoder.getType());
                audioLibraryEntity.setData(dataUrlDecoder.getData());
                audioLibraryEntity.setSize(dataUrlDecoder.getDataLength());
            }
            entityManager.merge(audioLibraryEntity);
        }
    }

    public List<AudioLibraryEntity> toAudioLibraryEntities(List<Integer> audioIds) {
        List<AudioLibraryEntity> audioLibraryEntities = new ArrayList<>();
        if (audioIds != null) {
            for (Integer audioId : audioIds) {
                audioLibraryEntities.add(entityManager.find(AudioLibraryEntity.class, audioId));
            }
        }
        return audioLibraryEntities;
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

}
