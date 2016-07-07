package com.btxtech.server.rest;

import com.btxtech.server.DataUrlDecoder;
import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ImageLibraryEntity_;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.ImageService;
import com.btxtech.shared.dto.ImageGalleryItem;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 15.06.2016.
 */
public class ImageServiceImpl implements ImageService {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    @Transactional
    public Response getImage(int id) throws Exception {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
            Root<ImageLibraryEntity> root = cq.from(ImageLibraryEntity.class);
            cq.where(criteriaBuilder.equal(root.get(ImageLibraryEntity_.id), id));
            cq.multiselect(root.get(ImageLibraryEntity_.data));
            Tuple tupleResult = entityManager.createQuery(cq).getSingleResult();
            return Response.ok(tupleResult.get(0)).lastModified(new Date()).build();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public List<ImageGalleryItem> getImageGalleryItem() {
        try {
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
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ImageGalleryItem getImageGalleryItem(int id) {
        try {
            ImageLibraryEntity imageLibraryEntity = entityManager.find(ImageLibraryEntity.class, (long) id);
            return imageLibraryEntity.toImageGalleryItem();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void uploadImage(String dataUrl) {
        try {
            DataUrlDecoder dataUrlDecoder = new DataUrlDecoder(dataUrl);
            ImageLibraryEntity imageLibraryEntity = new ImageLibraryEntity();
            imageLibraryEntity.setType(dataUrlDecoder.getType());
            imageLibraryEntity.setData(dataUrlDecoder.getData());
            imageLibraryEntity.setSize(dataUrlDecoder.getDataLength());
            entityManager.persist(imageLibraryEntity);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void save(Map<Integer, String> dataUrls) {
        try {
            for (Map.Entry<Integer, String> entry : dataUrls.entrySet()) {
                ImageLibraryEntity imageLibraryEntity = entityManager.find(ImageLibraryEntity.class, entry.getKey().longValue());
                DataUrlDecoder dataUrlDecoder = new DataUrlDecoder(entry.getValue());
                imageLibraryEntity.setType(dataUrlDecoder.getType());
                imageLibraryEntity.setData(dataUrlDecoder.getData());
                imageLibraryEntity.setSize(dataUrlDecoder.getDataLength());
                entityManager.persist(imageLibraryEntity);
            }
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
