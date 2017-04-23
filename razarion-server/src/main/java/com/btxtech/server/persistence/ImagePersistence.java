package com.btxtech.server.persistence;

import com.btxtech.server.DataUrlDecoder;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.dto.ImageGalleryItem;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 21.10.2016.
 */
@Singleton
public class ImagePersistence {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public byte[] getImage(int id) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
        Root<ImageLibraryEntity> root = cq.from(ImageLibraryEntity.class);
        cq.where(criteriaBuilder.equal(root.get(ImageLibraryEntity_.id), id));
        cq.multiselect(root.get(ImageLibraryEntity_.data));
        Tuple tupleResult = entityManager.createQuery(cq).getSingleResult();
        return (byte[]) tupleResult.get(0);
    }

    @Transactional
    public List<ImageGalleryItem> getImageGalleryItems() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ImageLibraryEntity> userQuery = criteriaBuilder.createQuery(ImageLibraryEntity.class);
        Root<ImageLibraryEntity> from = userQuery.from(ImageLibraryEntity.class);
        CriteriaQuery<ImageLibraryEntity> userSelect = userQuery.select(from);
        List<ImageLibraryEntity> images = entityManager.createQuery(userSelect).getResultList();
        List<ImageGalleryItem> items = new ArrayList<>();
        for (ImageLibraryEntity image : images) {
            items.add(image.toImageGalleryItem());
        }
        return items;
    }

    @Transactional
    public ImageGalleryItem getImageGalleryItem(int id) {
        ImageLibraryEntity imageLibraryEntity = entityManager.find(ImageLibraryEntity.class, id);
        return imageLibraryEntity.toImageGalleryItem();
    }

    @Transactional
    @SecurityCheck
    public void createImage(DataUrlDecoder dataUrlDecoder) {
        ImageLibraryEntity imageLibraryEntity = new ImageLibraryEntity();
        imageLibraryEntity.setType(dataUrlDecoder.getType());
        imageLibraryEntity.setData(dataUrlDecoder.getData());
        imageLibraryEntity.setSize(dataUrlDecoder.getDataLength());
        entityManager.persist(imageLibraryEntity);
    }

    @Transactional
    @SecurityCheck
    public void save(int id, DataUrlDecoder dataUrlDecoder) {
        ImageLibraryEntity imageLibraryEntity = entityManager.find(ImageLibraryEntity.class, id);
        imageLibraryEntity.setType(dataUrlDecoder.getType());
        imageLibraryEntity.setData(dataUrlDecoder.getData());
        imageLibraryEntity.setSize(dataUrlDecoder.getDataLength());
        entityManager.persist(imageLibraryEntity);
    }

    @Transactional
    public ImageLibraryEntity getImageLibraryEntity(Integer id) {
        if (id != null) {
            return entityManager.find(ImageLibraryEntity.class, id.longValue());
        } else {
            return null;
        }
    }

}
