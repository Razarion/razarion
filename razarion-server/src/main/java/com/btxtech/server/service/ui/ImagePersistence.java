package com.btxtech.server.service.ui;

import com.btxtech.server.model.ui.ImageLibraryEntity;
import com.btxtech.server.repository.ui.ImageRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by Beat
 * 21.10.2016.
 */
@Service
public class ImagePersistence {

    @Inject
    private ImageRepository imageRepository;

//    @Transactional
//    public Image getImage(int id) {
//        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
//        Root<ImageLibraryEntity> root = cq.from(ImageLibraryEntity.class);
//        cq.where(criteriaBuilder.equal(root.get(ImageLibraryEntity_.id), id));
//        cq.multiselect(root.get(ImageLibraryEntity_.data), root.get(ImageLibraryEntity_.type));
//        Tuple tupleResult = entityManager.createQuery(cq).getSingleResult();
//        return new Image((byte[]) tupleResult.get(0), (String) tupleResult.get(1));
//    }
//
//    @Transactional
//    public List<ImageGalleryItem> getImageGalleryItems() {
//        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//        CriteriaQuery<ImageLibraryEntity> userQuery = criteriaBuilder.createQuery(ImageLibraryEntity.class);
//        Root<ImageLibraryEntity> from = userQuery.from(ImageLibraryEntity.class);
//        CriteriaQuery<ImageLibraryEntity> userSelect = userQuery.select(from);
//        List<ImageLibraryEntity> images = entityManager.createQuery(userSelect).getResultList();
//        List<ImageGalleryItem> items = new ArrayList<>();
//        for (ImageLibraryEntity image : images) {
//            items.add(image.toImageGalleryItem());
//        }
//        return items;
//    }
//
//    @Transactional
//    public ImageGalleryItem getImageGalleryItem(int id) {
//        ImageLibraryEntity imageLibraryEntity = entityManager.find(ImageLibraryEntity.class, id);
//        return imageLibraryEntity.toImageGalleryItem();
//    }
//
//    @Transactional
//    @SecurityCheck
//    public ImageGalleryItem createImage(byte[] imageData, String type) {
//        ImageLibraryEntity imageLibraryEntity = new ImageLibraryEntity();
//        imageLibraryEntity.setType(type);
//        imageLibraryEntity.setData(imageData);
//        imageLibraryEntity.setSize(imageData.length);
//        entityManager.persist(imageLibraryEntity);
//        return imageLibraryEntity.toImageGalleryItem();
//    }
//
//    @Transactional
//    @SecurityCheck
//    public void save(int id, byte[] imageData, String type) {
//        ImageLibraryEntity imageLibraryEntity = entityManager.find(ImageLibraryEntity.class, id);
//        imageLibraryEntity.setType(type);
//        imageLibraryEntity.setData(imageData);
//        imageLibraryEntity.setSize(imageData.length);
//        entityManager.persist(imageLibraryEntity);
//    }
//
//    @Transactional
//    @SecurityCheck
//    public void saveInternalName(int id, String internalName) {
//        ImageLibraryEntity imageLibraryEntity = entityManager.find(ImageLibraryEntity.class, id);
//        imageLibraryEntity.setInternalName(internalName);
//        entityManager.persist(imageLibraryEntity);
//    }
//
//    @Transactional
//    @SecurityCheck
//    public void delete(int id) {
//        ImageLibraryEntity imageLibraryEntity = entityManager.find(ImageLibraryEntity.class, id);
//        if (imageLibraryEntity == null) {
//            throw new IllegalArgumentException("No image for id: " + id);
//        }
//        entityManager.remove(imageLibraryEntity);
//    }

    public static Integer idOrNull(ImageLibraryEntity libraryEntity) {
        if (libraryEntity != null) {
            return libraryEntity.getId();
        } else {
            return null;
        }
    }

    @Transactional
    public ImageLibraryEntity getImageLibraryEntity(Integer id) {
        if (id == null) {
            return null;
        }
        return imageRepository.getReferenceById(id);
    }

//    @Transactional
//    public Integer getImageId4InternalName(String internalName) {
//        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//        CriteriaQuery<Tuple> userQuery = criteriaBuilder.createTupleQuery();
//        Root<ImageLibraryEntity> from = userQuery.from(ImageLibraryEntity.class);
//        userQuery.multiselect(from.get(ImageLibraryEntity_.id));
//        userQuery.where(criteriaBuilder.equal(from.get(ImageLibraryEntity_.internalName), internalName));
//
//        List<Tuple> tuples = entityManager.createQuery(userQuery).getResultList();
//        if (!tuples.isEmpty()) {
//            return (Integer) tuples.get(0).get(0);
//        } else {
//            return null;
//        }
//    }

    public static class Image {
        private final byte[] data;
        private final String type;

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