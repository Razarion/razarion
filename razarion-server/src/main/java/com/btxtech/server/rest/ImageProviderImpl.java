package com.btxtech.server.rest;

import com.btxtech.server.DataUrlDecoder;
import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.shared.dto.ImageGalleryItem;
import com.btxtech.shared.rest.ImageProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * 15.06.2016.
 */
public class ImageProviderImpl implements ImageProvider {
    @Inject
    private ImagePersistence imagePersistence;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    public Response getImage(int id) {
        try {
            return Response.ok(imagePersistence.getImage(id)).lastModified(new Date()).build();
        } catch (Throwable e) {
            exceptionHandler.handleException("Can not load image for id: " + id, e);
            throw e;
        }
    }

    @Override
    public List<ImageGalleryItem> getImageGalleryItems() {
        try {
            return imagePersistence.getImageGalleryItems();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ImageGalleryItem getImageGalleryItem(int id) {
        try {
            return imagePersistence.getImageGalleryItem(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void uploadImage(String dataUrl) {
        try {
            imagePersistence.createImage(new DataUrlDecoder(dataUrl));
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void save(int id, String dataUrl) {
        try {
            imagePersistence.save(id, new DataUrlDecoder(dataUrl));
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
