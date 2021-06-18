package com.btxtech.server.persistence;

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
    public Image getImage(int id) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
        Root<ImageLibraryEntity> root = cq.from(ImageLibraryEntity.class);
        cq.where(criteriaBuilder.equal(root.get(ImageLibraryEntity_.id), id));
        cq.multiselect(root.get(ImageLibraryEntity_.data), root.get(ImageLibraryEntity_.type));
        Tuple tupleResult = entityManager.createQuery(cq).getSingleResult();
        return new Image((byte[]) tupleResult.get(0), (String) tupleResult.get(1));
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
    public void createImage(byte[] imageData, String type) {
        ImageLibraryEntity imageLibraryEntity = new ImageLibraryEntity();
        imageLibraryEntity.setType(type);
        imageLibraryEntity.setData(imageData);
        imageLibraryEntity.setSize(imageData.length);
        entityManager.persist(imageLibraryEntity);
    }

    @Transactional
    @SecurityCheck
    public void save(int id, byte[] imageData, String type) {
        ImageLibraryEntity imageLibraryEntity = entityManager.find(ImageLibraryEntity.class, id);
        imageLibraryEntity.setType(type);
        imageLibraryEntity.setData(imageData);
        imageLibraryEntity.setSize(imageData.length);
        entityManager.persist(imageLibraryEntity);
    }

    @Transactional
    @SecurityCheck
    public void delete(int id) {
        ImageLibraryEntity imageLibraryEntity = entityManager.find(ImageLibraryEntity.class, id);
        if (imageLibraryEntity == null) {
            throw new IllegalArgumentException("No image for id: " + id);
        }
        entityManager.remove(imageLibraryEntity);
    }

    @Transactional
    public ImageLibraryEntity getImageLibraryEntity(Integer id) {
        if (id == null) {
            return null;
        }
        ImageLibraryEntity imageLibraryEntity = entityManager.find(ImageLibraryEntity.class, id);
        if (imageLibraryEntity == null) {
            throw new IllegalArgumentException("No ImageLibraryEntity for id: " + id);
        }
        return imageLibraryEntity;
    }

    public static Integer idOrNull(ImageLibraryEntity libraryEntity) {
        if (libraryEntity != null) {
            return libraryEntity.getId();
        } else {
            return null;
        }
    }

    public static class Image {
        private byte[] data;
        private String type;

        public Image(byte[] data, String type) {
            this.data = data;
            this.type = type;
        }

        public byte[] getData() {
            return data;
        }

        public String getType() {
            return type;
        }
    }
}