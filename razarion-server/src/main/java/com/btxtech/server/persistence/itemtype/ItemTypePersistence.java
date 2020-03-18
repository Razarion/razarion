package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.AudioPersistence;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.Shape3DPersistence;
import com.btxtech.server.persistence.bot.BotConfigEntity;
import com.btxtech.server.persistence.inventory.InventoryPersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.system.ExceptionHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 15.08.2015.
 */
@ApplicationScoped
public class ItemTypePersistence {
    @Inject
    private ExceptionHandler exceptionHandler;
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private Shape3DPersistence shape3DPersistence;
    @Inject
    private ImagePersistence imagePersistence;
    @Inject
    private AudioPersistence audioPersistence;
    @Inject
    private InventoryPersistence inventoryPersistence;

    @Transactional
    @SecurityCheck
    public BaseItemType createBaseItemType() {
        BaseItemTypeEntity baseItemTypeEntity = new BaseItemTypeEntity();
        entityManager.persist(baseItemTypeEntity);
        return baseItemTypeEntity.toBaseItemType();
    }

    @Transactional
    public List<BaseItemType> readBaseItemTypes() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<BaseItemTypeEntity> userQuery = criteriaBuilder.createQuery(BaseItemTypeEntity.class);
        Root<BaseItemTypeEntity> from = userQuery.from(BaseItemTypeEntity.class);
        CriteriaQuery<BaseItemTypeEntity> userSelect = userQuery.select(from);
        List<BaseItemTypeEntity> itemTypeEntities = entityManager.createQuery(userSelect).getResultList();

        return itemTypeEntities.stream().map(BaseItemTypeEntity::toBaseItemType).collect(Collectors.toList());
    }

    @Transactional
    public BaseItemTypeEntity readBaseItemTypeEntity(Integer id) {
        if (id == null) {
            return null;
        }
        BaseItemTypeEntity baseItemTypeEntity = entityManager.find(BaseItemTypeEntity.class, id);
        if (baseItemTypeEntity == null) {
            throw new IllegalArgumentException("No BaseItemTypeEntity for id: " + id);
        }
        return baseItemTypeEntity;
    }

    @Transactional
    @SecurityCheck
    public void updateBaseItemType(BaseItemType baseItemType) {
        BaseItemTypeEntity baseItemTypeEntity = entityManager.find(BaseItemTypeEntity.class, baseItemType.getId());
        baseItemTypeEntity.fromBaseItemType(baseItemType, this, shape3DPersistence);
        baseItemTypeEntity.setShape3DId(shape3DPersistence.getColladaEntity(baseItemType.getShape3DId()));
        baseItemTypeEntity.setSpawnShape3DId(shape3DPersistence.getColladaEntity(baseItemType.getSpawnShape3DId()));
        baseItemTypeEntity.setBuildupTexture(imagePersistence.getImageLibraryEntity(baseItemType.getBuildupTextureId()));
        baseItemTypeEntity.setDemolitionImage(imagePersistence.getImageLibraryEntity(baseItemType.getDemolitionImageId()));
        baseItemTypeEntity.setWreckageShape3D(shape3DPersistence.getColladaEntity(baseItemType.getWreckageShape3DId()));
        baseItemTypeEntity.setSpawnAudio(audioPersistence.getAudioLibraryEntity(baseItemType.getSpawnAudioId()));
        baseItemTypeEntity.setThumbnail(imagePersistence.getImageLibraryEntity(baseItemType.getThumbnail()));
        entityManager.merge(baseItemTypeEntity);
    }

    @Transactional
    @SecurityCheck
    public void deleteBaseItemType(int id) {
        entityManager.remove(readBaseItemTypeEntity(id));
    }

    @Transactional
    @SecurityCheck
    public ResourceItemType createResourceItemType() {
        ResourceItemTypeEntity resourceItemTypeEntity = new ResourceItemTypeEntity();
        entityManager.persist(resourceItemTypeEntity);
        return resourceItemTypeEntity.toResourceItemType();
    }

    @Transactional
    public ResourceItemTypeEntity readResourceItemTypeEntity(Integer id) {
        if (id == null) {
            return null;
        }
        ResourceItemTypeEntity resourceItemTypeEntity = entityManager.find(ResourceItemTypeEntity.class, id);
        if (resourceItemTypeEntity == null) {
            throw new IllegalArgumentException("No ResourceItemTypeEntity for id: " + id);
        }
        return resourceItemTypeEntity;

    }

    @Transactional
    public List<ResourceItemType> readResourceItemTypes() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ResourceItemTypeEntity> userQuery = criteriaBuilder.createQuery(ResourceItemTypeEntity.class);
        Root<ResourceItemTypeEntity> from = userQuery.from(ResourceItemTypeEntity.class);
        CriteriaQuery<ResourceItemTypeEntity> userSelect = userQuery.select(from);
        List<ResourceItemTypeEntity> itemTypeEntities = entityManager.createQuery(userSelect).getResultList();

        return itemTypeEntities.stream().map(ResourceItemTypeEntity::toResourceItemType).collect(Collectors.toList());
    }

    @Transactional
    @SecurityCheck
    public void updateResourceItemType(ResourceItemType resourceItemType) {
        ResourceItemTypeEntity resourceItemTypeEntity = entityManager.find(ResourceItemTypeEntity.class, resourceItemType.getId());
        resourceItemTypeEntity.fromResourceItemType(resourceItemType);
        resourceItemTypeEntity.setShape3DId(shape3DPersistence.getColladaEntity(resourceItemType.getShape3DId()));
        resourceItemTypeEntity.setThumbnail(imagePersistence.getImageLibraryEntity(resourceItemType.getThumbnail()));
        entityManager.merge(resourceItemTypeEntity);
    }

    @Transactional
    @SecurityCheck
    public void deleteResourceItemType(int id) {
        entityManager.remove(entityManager.find(ResourceItemTypeEntity.class, id));
    }

    @Transactional
    @SecurityCheck
    public BoxItemType createBoxItemType() {
        BoxItemTypeEntity boxItemTypeEntity = new BoxItemTypeEntity();
        entityManager.persist(boxItemTypeEntity);
        return boxItemTypeEntity.toBoxItemType();
    }

    @Transactional
    public BoxItemTypeEntity readBoxItemTypeEntity(Integer id) {
        if (id == null) {
            return null;
        }
        BoxItemTypeEntity boxItemTypeEntity = entityManager.find(BoxItemTypeEntity.class, id);
        if (boxItemTypeEntity == null) {
            throw new IllegalArgumentException("No BoxItemTypeEntity for id: " + id);
        }
        return boxItemTypeEntity;
    }

    @Transactional
    public List<BoxItemType> readBoxItemTypes() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<BoxItemTypeEntity> userQuery = criteriaBuilder.createQuery(BoxItemTypeEntity.class);
        Root<BoxItemTypeEntity> from = userQuery.from(BoxItemTypeEntity.class);
        CriteriaQuery<BoxItemTypeEntity> userSelect = userQuery.select(from);
        List<BoxItemTypeEntity> itemTypeEntities = entityManager.createQuery(userSelect).getResultList();

        return itemTypeEntities.stream().map(BoxItemTypeEntity::toBoxItemType).collect(Collectors.toList());
    }

    @Transactional
    @SecurityCheck
    public void deleteBoxItemType(int id) {
        entityManager.remove(entityManager.find(BoxItemTypeEntity.class, id));
    }

    @Transactional
    @SecurityCheck
    public void updateBoxItemType(BoxItemType boxItemType) {
        BoxItemTypeEntity boxItemTypeEntity = entityManager.find(BoxItemTypeEntity.class, boxItemType.getId());
        boxItemTypeEntity.fromBoxItemType(boxItemType, inventoryPersistence);
        boxItemTypeEntity.setShape3DId(shape3DPersistence.getColladaEntity(boxItemType.getShape3DId()));
        boxItemTypeEntity.setThumbnail(imagePersistence.getImageLibraryEntity(boxItemType.getThumbnail()));
        entityManager.merge(boxItemTypeEntity);
    }

    // This should not be here. But there is no BotPersistence...
    public BotConfigEntity readBotConfigEntity(Integer id) {
        if (id == null) {
            return null;
        }
        BotConfigEntity botConfigEntity = entityManager.find(BotConfigEntity.class, id);
        if (botConfigEntity == null) {
            throw new IllegalArgumentException("No BotConfigEntity for id: " + id);
        }
        return botConfigEntity;
    }

    public Map<BaseItemTypeEntity, Integer> baseItemTypeLimitation(Map<Integer, Integer> input) {
        if(input == null) {
            return Collections.emptyMap();
        }
        return input.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> readBaseItemTypeEntity(entry.getKey()), Map.Entry::getKey));
    }

}
