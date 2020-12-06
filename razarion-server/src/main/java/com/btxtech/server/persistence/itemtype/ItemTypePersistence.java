package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.AudioPersistence;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.Shape3DCrudPersistence;
import com.btxtech.server.persistence.bot.BotConfigEntity;
import com.btxtech.server.persistence.inventory.InventoryPersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.system.ExceptionHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 15.08.2015.
 */
@Deprecated
@ApplicationScoped
public class ItemTypePersistence {
    @Inject
    private ExceptionHandler exceptionHandler;
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private Shape3DCrudPersistence shape3DPersistence;
    @Inject
    private ImagePersistence imagePersistence;
    @Inject
    private AudioPersistence audioPersistence;
    @Inject
    private InventoryPersistence inventoryPersistence;

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
        boxItemTypeEntity.setShape3DId(shape3DPersistence.getEntity(boxItemType.getShape3DId()));
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
}
